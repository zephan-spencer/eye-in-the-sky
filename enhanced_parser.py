# Enhanced geocoding parser with caching and address preprocessing
import pandas as pd
import time
import folium
from folium.plugins import MarkerCluster
import json
import os
import re
import logging
import math
from datetime import datetime
from geopy.geocoders import Nominatim
from geopy.exc import GeocoderTimedOut, GeocoderUnavailable
import hashlib
import random
from collections import defaultdict

def safe_str(value, default='None'):
    """Convert value to string, handling NaN/None values properly"""
    if pd.isna(value) or value is None:
        return default
    return str(value).strip()

class GeocodeCache:
    """Manages geocoding cache to avoid redundant API calls"""
    
    def __init__(self, cache_file='geocode_cache.json'):
        self.cache_file = cache_file
        self.cache = self._load_cache()
    
    def _load_cache(self):
        """Load existing cache from file"""
        if os.path.exists(self.cache_file):
            try:
                with open(self.cache_file, 'r') as f:
                    return json.load(f)
            except (json.JSONDecodeError, IOError):
                logging.warning(f"Could not load cache from {self.cache_file}, starting fresh")
                return {}
        return {}
    
    def save_cache(self):
        """Save cache to file"""
        try:
            with open(self.cache_file, 'w') as f:
                json.dump(self.cache, f, indent=2)
        except IOError:
            logging.error(f"Could not save cache to {self.cache_file}")
    
    def get_cache_key(self, address_dict):
        """Generate a consistent cache key from address components"""
        # Normalize address components for consistent caching
        normalized = {
            'street': str(address_dict.get('street', '')).strip().lower(),
            'city': str(address_dict.get('city', '')).strip().lower(),
            'state': str(address_dict.get('state', '')).strip().lower(),
            'zip': str(address_dict.get('postalcode', '')).strip()
        }
        # Create hash of normalized address
        address_str = f"{normalized['street']}|{normalized['city']}|{normalized['state']}|{normalized['zip']}"
        return hashlib.md5(address_str.encode()).hexdigest()
    
    def get(self, address_dict):
        """Get cached geocoding result"""
        key = self.get_cache_key(address_dict)
        return self.cache.get(key)
    
    def is_failed(self, address_dict):
        """Check if address has previously failed geocoding"""
        cached_result = self.get(address_dict)
        return cached_result and cached_result.get('failed', False)
    
    def set(self, address_dict, lat, lon, full_address):
        """Cache successful geocoding result"""
        key = self.get_cache_key(address_dict)
        self.cache[key] = {
            'latitude': lat,
            'longitude': lon,
            'address': full_address,
            'cached_date': datetime.now().isoformat(),
            'failed': False
        }
    
    def set_failed(self, address_dict, reason="All strategies failed"):
        """Cache failed geocoding attempt to avoid repeated API calls"""
        key = self.get_cache_key(address_dict)
        self.cache[key] = {
            'latitude': None,
            'longitude': None,
            'address': None,
            'cached_date': datetime.now().isoformat(),
            'failed': True,
            'failure_reason': reason
        }

class AddressPreprocessor:
    """Cleans and standardizes addresses for better geocoding accuracy"""
    
    def __init__(self):
        # Common abbreviations and their full forms
        self.street_abbrev = {
            'st': 'street', 'st.': 'street',
            'ave': 'avenue', 'ave.': 'avenue',
            'rd': 'road', 'rd.': 'road',
            'dr': 'drive', 'dr.': 'drive',
            'ln': 'lane', 'ln.': 'lane',
            'ct': 'court', 'ct.': 'court',
            'cir': 'circle', 'cir.': 'circle',
            'blvd': 'boulevard', 'blvd.': 'boulevard',
            'pkwy': 'parkway', 'pkwy.': 'parkway',
            'pl': 'place', 'pl.': 'place'
        }
        
        self.directional_abbrev = {
            'n': 'north', 'n.': 'north',
            's': 'south', 's.': 'south',
            'e': 'east', 'e.': 'east',
            'w': 'west', 'w.': 'west',
            'ne': 'northeast', 'nw': 'northwest',
            'se': 'southeast', 'sw': 'southwest'
        }
    
    def clean_street_address(self, address):
        """Clean and standardize street address"""
        if pd.isna(address) or address == '':
            return ''
        
        address = str(address).strip()
        
        # Remove extra whitespace
        address = re.sub(r'\s+', ' ', address)
        
        # Remove common suffixes that might confuse geocoding
        address = re.sub(r',?\s*(apt|apartment|unit|suite|ste|#)\s*\w*$', '', address, flags=re.IGNORECASE)
        
        # Handle common road type abbreviations and errors
        road_corrections = {
            'sr ': 'state route ',
            'rt ': 'route ',
            'hwy ': 'highway ',
            'rd ': 'road ',
            'dr ': 'drive ',
            'st ': 'street ',
            'ave ': 'avenue ',
            'blvd ': 'boulevard ',
            'pkwy ': 'parkway ',
            'cir ': 'circle ',
            'ct ': 'court ',
            'ln ': 'lane ',
            'pl ': 'place ',
            'terr ': 'terrace '
        }
        
        # Apply road corrections (case insensitive)
        address_lower = address.lower()
        for abbrev, full in road_corrections.items():
            if abbrev in address_lower:
                # Use word boundaries to avoid partial matches
                pattern = r'\b' + re.escape(abbrev.strip()) + r'\b'
                address = re.sub(pattern, full.strip(), address, flags=re.IGNORECASE)
        
        # Expand common abbreviations
        words = address.lower().split()
        for i, word in enumerate(words):
            if word in self.street_abbrev:
                words[i] = self.street_abbrev[word]
            elif word in self.directional_abbrev:
                words[i] = self.directional_abbrev[word]
        
        return ' '.join(words).title()
    
    def clean_city(self, city):
        """Clean city name"""
        if pd.isna(city) or city == '':
            return ''
        return str(city).strip().title()
    
    def clean_state(self, state):
        """Clean state abbreviation"""
        if pd.isna(state) or state == '':
            return ''
        return str(state).strip().upper()
    
    def clean_zip(self, zip_code):
        """Clean zip code"""
        if pd.isna(zip_code) or zip_code == '':
            return ''
        # Extract 5-digit zip code
        zip_str = str(zip_code).strip()
        match = re.search(r'\b(\d{5})\b', zip_str)
        return match.group(1) if match else zip_str
    
    def preprocess_address(self, street, city, state, zip_code):
        """Preprocess all address components"""
        return {
            'street': self.clean_street_address(street),
            'city': self.clean_city(city),
            'state': self.clean_state(state),
            'postalcode': self.clean_zip(zip_code)
        }

class EnhancedGeoMapper:
    """Enhanced mapping application with caching and rate limiting"""
    
    def __init__(self, config=None):
        # Default configuration
        self.config = {
            'api_delay': 1.0,  # Seconds between API calls
            'timeout': 10,     # Geocoding timeout
            'max_retries': 3,  # Max retries for failed geocoding
            'output_file': 'enhanced_map.html',
            'log_file': 'geocoding.log',
            'center_lat': 40,
            'center_lon': -95,
            'zoom_start': 4
        }
        
        if config:
            self.config.update(config)
        
        # Setup logging
        logging.basicConfig(
            level=logging.INFO,
            format='%(asctime)s - %(levelname)s - %(message)s',
            handlers=[
                logging.FileHandler(self.config['log_file']),
                logging.StreamHandler()
            ]
        )
        
        self.cache = GeocodeCache()
        self.preprocessor = AddressPreprocessor()
        self.geolocator = Nominatim(user_agent="EnhancedGeoMapper", timeout=self.config['timeout'])
        self.map = folium.Map(
            location=[self.config['center_lat'], self.config['center_lon']], 
            zoom_start=self.config['zoom_start']
        )
        
        # Create separate feature groups for each category with clustering
        self.category_groups = {}
        categories = [
            'Workers Comp',
            'WC Client', 
            'Client',
            'Medical Professional',
            'Good Progress',
            'Amputee',
            'Prosthetist',
            'Multiple People',
            'Other'
        ]
        
        for category in categories:
            # Create a feature group for this category
            feature_group = folium.FeatureGroup(name=category, overlay=True, control=True)
            
            # Add marker clustering to each feature group
            marker_cluster = MarkerCluster(
                name=f"{category} Cluster",
                overlay=False,
                control=False,
                options={
                    'disableClusteringAtZoom': 10,
                    'maxClusterRadius': 80,
                    'spiderfyDistanceMultiplier': 2
                }
            )
            marker_cluster.add_to(feature_group)
            feature_group.add_to(self.map)
            
            self.category_groups[category] = marker_cluster
        
        # Add layer control to toggle categories
        folium.LayerControl(position='topright', collapsed=False).add_to(self.map)
        
        # Coordinate management for duplicate locations
        self.coordinate_groups = defaultdict(list)  # Groups people by coordinate
        self.coordinate_offsets = {}  # Tracks offset assignments
        
        # Statistics
        self.stats = {
            'total_records': 0,
            'cache_hits': 0,
            'api_calls': 0,
            'successful_geocodes': 0,
            'failed_geocodes': 0,
            'skipped_records': 0,
            'clustered_locations': 0
        }
    
    def geocode_address(self, address_dict):
        """Geocode an address with caching, error handling, and fallback strategies"""
        # Create original address string for fallback display
        original_address_parts = []
        if address_dict.get('street'):
            original_address_parts.append(address_dict['street'])
        if address_dict.get('city'):
            original_address_parts.append(address_dict['city'])
        if address_dict.get('state'):
            original_address_parts.append(address_dict['state'])
        if address_dict.get('postalcode'):
            original_address_parts.append(address_dict['postalcode'])
        original_address = ', '.join(original_address_parts) if original_address_parts else 'Unknown Address'
        
        # Check cache first
        cached_result = self.cache.get(address_dict)
        if cached_result:
            self.stats['cache_hits'] += 1
            if cached_result.get('failed', False):
                logging.debug(f"Cache hit - previously failed address: {address_dict} (reason: {cached_result.get('failure_reason', 'Unknown')})")
                self.stats['failed_geocodes'] += 1
                return None, None, None, "Cached Failure"
            else:
                logging.debug(f"Cache hit for address: {address_dict}")
                # For cached results, prefer original address for fallback strategies
                cached_address = cached_result['address']
                display_address = original_address if cached_result.get('strategy_used') in ['City Only', 'Full State Name', 'With USA'] else cached_address
                return cached_result['latitude'], cached_result['longitude'], display_address, "Cache"
        
        # Try multiple geocoding strategies
        strategies = self._build_geocoding_strategies(address_dict)
        
        for strategy_name, address_string in strategies:
            if not address_string:
                continue
                
            for attempt in range(self.config['max_retries']):
                try:
                    self.stats['api_calls'] += 1
                    location = self.geolocator.geocode(address_string)
                    
                    # Rate limiting
                    time.sleep(self.config['api_delay'])
                    
                    if location:
                        lat, lon, full_address = location.latitude, location.longitude, location.address
                        
                        # For fallback strategies, use original address for display
                        display_address = original_address if strategy_name in ['City Only', 'Full State Name', 'With USA'] else full_address
                        
                        # Cache the result using original address_dict as key
                        self.cache.set(address_dict, lat, lon, display_address)
                        self.stats['successful_geocodes'] += 1
                        logging.info(f"Successfully geocoded using {strategy_name}: {address_string} -> {full_address}")
                        return lat, lon, display_address, strategy_name
                    else:
                        logging.debug(f"No results for {strategy_name}: {address_string}")
                        break
                        
                except (GeocoderTimedOut, GeocoderUnavailable) as e:
                    logging.warning(f"Geocoding attempt {attempt + 1} failed for {address_string}: {e}")
                    if attempt < self.config['max_retries'] - 1:
                        time.sleep(self.config['api_delay'] * (attempt + 1))  # Exponential backoff
        
        # Cache the failure to avoid repeated attempts
        failure_reason = f"All {len(strategies)} strategies failed"
        self.cache.set_failed(address_dict, failure_reason)
        logging.warning(f"All geocoding strategies failed for address: {address_dict} - cached for future reference")
        self.stats['failed_geocodes'] += 1
        return None, None, None, None
    
    def _build_geocoding_strategies(self, address_dict):
        """Build multiple geocoding strategies for an address"""
        strategies = []
        street = address_dict.get('street', '')
        city = address_dict.get('city', '')
        state = address_dict.get('state', '')
        postalcode = address_dict.get('postalcode', '')
        
        # Skip P.O. Box addresses entirely - they don't geocode to useful locations
        if street and ('po box' in street.lower() or 'p.o. box' in street.lower() or street.lower().startswith('box ')):
            logging.info(f"Skipping P.O. Box address: {street}")
            return []
        
        # Correct common city misspellings
        city = self._correct_city_name(city)
        
        # Strategy 1: Full address as preprocessed
        if street and city and state:
            full_address = ', '.join(filter(None, [street, city, state, postalcode]))
            strategies.append(("Full Address", full_address))
        
        # Strategy 2: Enhanced street name with common corrections
        if street and city and state:
            enhanced_street = self._enhance_street_name(street)
            if enhanced_street != street:
                enhanced_address = ', '.join(filter(None, [enhanced_street, city, state, postalcode]))
                strategies.append(("Enhanced Street", enhanced_address))
        
        # Strategy 3: Without street number (for general area)
        if street and city and state:
            street_without_number = self._remove_street_number(street)
            if street_without_number != street:
                no_number_address = ', '.join(filter(None, [street_without_number, city, state, postalcode]))
                strategies.append(("No Street Number", no_number_address))
        
        # Strategy 4: Street name only with city/state (remove numbers and suffixes)
        if street and city and state:
            street_name_only = self._extract_street_name_only(street)
            if street_name_only and street_name_only != street:
                name_only_address = ', '.join(filter(None, [street_name_only, city, state]))
                strategies.append(("Street Name Only", name_only_address))
        
        # Strategy 5: City and state only (for general location)
        if city and state:
            city_only = ', '.join(filter(None, [city, state, postalcode]))
            strategies.append(("City Only", city_only))
        
        # Strategy 6: Add "Pennsylvania" or full state name if abbreviated
        if city and state:
            full_state = self._expand_state_name(state)
            if full_state != state:
                full_state_address = ', '.join(filter(None, [street, city, full_state, postalcode]))
                strategies.append(("Full State Name", full_state_address))
        
        # Strategy 7: Add "USA" to help with international geocoding
        if city and state:
            usa_address = ', '.join(filter(None, [street, city, state, postalcode, 'USA']))
            strategies.append(("With USA", usa_address))
        
        return strategies
    
    def _enhance_street_name(self, street):
        """Apply common street name corrections"""
        if not street:
            return street
            
        enhanced = street
        
        # Common misspellings and variations
        corrections = {
            'ottillia': 'ottilia',
            'sr ': 'state route ',
            'rt ': 'route ',
            'hwy ': 'highway ',
            'blvd': 'boulevard',
            'pkwy': 'parkway',
            'cir': 'circle',
            'ct': 'court',
            'ln': 'lane',
            'pl': 'place',
            'terr': 'terrace',
            'ave': 'avenue',
            'st ': 'street ',  # Add space to avoid replacing 'st' in middle of words
            'rd': 'road',
            'dr': 'drive'
        }
        
        enhanced_lower = enhanced.lower()
        for wrong, correct in corrections.items():
            if wrong in enhanced_lower:
                # Only replace whole words
                import re
                pattern = r'\b' + re.escape(wrong) + r'\b'
                enhanced = re.sub(pattern, correct, enhanced, flags=re.IGNORECASE)
        
        return enhanced
    
    def _remove_street_number(self, street):
        """Remove street number to get just the street name"""
        if not street:
            return street
            
        import re
        
        # Special handling for state routes (SR, RT, Route, etc.)
        if re.match(r'^\d+\s*(sr|rt|route)\s*\d+', street, re.IGNORECASE):
            # For addresses like "1222 SR 87", keep "SR 87" and enhance it
            match = re.search(r'(sr|rt|route)\s*\d+', street, re.IGNORECASE)
            if match:
                route_part = match.group(0)
                # Enhance the route part
                enhanced_route = self._enhance_street_name(route_part)
                return enhanced_route
        
        # Remove leading numbers and common prefixes for regular streets
        pattern = r'^\d+\s*[a-z]?\s*'
        result = re.sub(pattern, '', street, flags=re.IGNORECASE).strip()
        return result if result else street
    
    def _expand_state_name(self, state):
        """Expand state abbreviations to full names"""
        state_mapping = {
            'PA': 'Pennsylvania',
            'NC': 'North Carolina',
            'NY': 'New York',
            'NJ': 'New Jersey',
            'OH': 'Ohio',
            'MD': 'Maryland',
            'VA': 'Virginia',
            'WV': 'West Virginia',
            'DE': 'Delaware',
            'FL': 'Florida',
            'CA': 'California',
            'TX': 'Texas'
        }
        return state_mapping.get(state.upper(), state)
    
    def _correct_city_name(self, city):
        """Correct common city name misspellings"""
        if not city:
            return city
            
        city_corrections = {
            'philidalephia': 'philadelphia',
            'philidelphia': 'philadelphia',
            'pittsburg': 'pittsburgh',  # without 'h' is common mistake
            'harrisburg': 'harrisburg',
            'york': 'york'
        }
        
        return city_corrections.get(city.lower(), city)
    
    def _extract_street_name_only(self, street):
        """Extract just the street name without numbers, directions, or suffixes"""
        if not street:
            return street
            
        import re
        
        # Remove leading numbers and directionals
        cleaned = re.sub(r'^\d+\s*[a-z]?\s*', '', street, flags=re.IGNORECASE).strip()
        
        # Remove directional prefixes (North, South, East, West, N, S, E, W)
        cleaned = re.sub(r'^(north|south|east|west|n|s|e|w)\s+', '', cleaned, flags=re.IGNORECASE).strip()
        
        # Remove common suffixes but keep the main name
        suffixes = ['street', 'st', 'avenue', 'ave', 'road', 'rd', 'drive', 'dr', 'lane', 'ln', 
                   'court', 'ct', 'circle', 'cir', 'boulevard', 'blvd', 'place', 'pl', 'way']
        
        for suffix in suffixes:
            pattern = r'\s+' + re.escape(suffix) + r'$'
            cleaned = re.sub(pattern, '', cleaned, flags=re.IGNORECASE).strip()
        
        return cleaned if cleaned else street
    
    def get_marker_style(self, status):
        """Get marker color, icon, and category based on status"""
        icon_color = "white"
        
        if status == "Workers Comp":
            color = "red"
            icon = "star"
            category = "Workers Comp"
        elif status == "WC Client":
            color = "orange"
            icon = "dollar"
            category = "WC Client"
        elif "Client" in status:
            color = "green"
            icon = "dollar"
            category = "Client"
        elif status in ["Nurse Case Manager", "Doctor", "Therapist"]:
            color = "purple"
            icon = "medkit"
            category = "Medical Professional"
        elif status in ["Amputee - good progress", "Potential Client"]:
            color = "darkpurple"
            icon = "thumbs-up"
            category = "Good Progress"
        elif status == "Amputee":
            color = "red"
            icon = "user"
            icon_color = "white"
            category = "Amputee"
        elif status == "Prosthetist":
            color = "blue"
            icon = "wrench"
            category = "Prosthetist"
        else:
            color = "gray"
            icon = "info-sign"
            category = "Other"
        
        return color, icon, icon_color, category
    
    def _round_coordinates(self, lat, lon, precision=2):
        """Round coordinates to detect duplicates (city-level geocoding)"""
        return round(lat, precision), round(lon, precision)
    
    def _get_offset_coordinates(self, lat, lon, person_data):
        """Get offset coordinates for duplicate locations"""
        rounded_coords = self._round_coordinates(lat, lon)
        coord_key = f"{rounded_coords[0]},{rounded_coords[1]}"
        
        # Add person to coordinate group
        self.coordinate_groups[coord_key].append(person_data)
        
        # If this is the first person at this location, use exact coordinates
        if len(self.coordinate_groups[coord_key]) == 1:
            return lat, lon
        
        # Calculate offset for subsequent people at same location
        offset_index = len(self.coordinate_groups[coord_key]) - 1
        
        # Create a small circular offset pattern
        import math
        angle = (offset_index * 45) % 360  # 45-degree intervals
        radius = 0.001 + (offset_index // 8) * 0.001  # Increasing radius for more people
        
        offset_lat = lat + radius * math.sin(math.radians(angle))
        offset_lon = lon + radius * math.cos(math.radians(angle))
        
        return offset_lat, offset_lon
    
    def _create_grouped_popup(self, coord_key):
        """Create a popup that shows all people at the same location"""
        people = self.coordinate_groups[coord_key]
        if len(people) <= 1:
            return None
            
        popup_content = f"""
        <div style="max-width: 450px; font-size: 15px;">
            <h4 style="font-size: 18px; margin-bottom: 15px;">📍 {len(people)} People at this Location</h4>
            <div style="max-height: 300px; overflow-y: auto;">
        """
        
        for i, person in enumerate(people, 1):
            popup_content += f"""
                <div style="border-bottom: 1px solid #eee; padding: 10px 0;">
                    <b style="font-size: 16px;">{i}. {person['first_name']} {person['last_name']}</b><br>
                    <span style="color: #666; font-size: 14px;">Status:</span> <span style="font-size: 14px;">{person['status']}</span><br>
                    <span style="color: #666; font-size: 14px;">Email:</span> <span style="font-size: 14px;">{person['email']}</span><br>
                    <span style="color: #666; font-size: 14px;">Phone:</span> <span style="font-size: 14px;">{person['phone']}</span><br>
                    <span style="color: #666; font-size: 14px;">Address:</span> <span style="font-size: 14px;">{person['address']}</span>
                </div>
            """
        
        popup_content += "</div></div>"
        return popup_content
    
    def create_marker(self, lat, lon, first_name, last_name, email, phone, status, address, strategy_used=None):
        """Create a marker on the map with clustering support"""
        person_data = {
            'first_name': first_name,
            'last_name': last_name,
            'email': email,
            'phone': phone,
            'status': status,
            'address': address,
            'strategy_used': strategy_used
        }
        
        # Get offset coordinates for duplicate locations
        offset_lat, offset_lon = self._get_offset_coordinates(lat, lon, person_data)
        
        # Create popup content
        popup_content = f"""
        <div style="max-width: 350px; font-size: 15px;">
            <b style="font-size: 17px;">{first_name} {last_name}</b><br>
            <span style="color: #666; font-size: 14px;">Status:</span> <span style="font-size: 14px;">{status}</span><br>
            <span style="color: #666; font-size: 14px;">Email:</span> <span style="font-size: 14px;">{email}</span><br>
            <span style="color: #666; font-size: 14px;">Phone:</span> <span style="font-size: 14px;">{phone}</span><br>
            <span style="color: #666; font-size: 14px;">Address:</span> <span style="font-size: 14px;">{address}</span>
        """
        
        if strategy_used:
            popup_content += f"<br><span style='color: #888; font-size: 12px;'>Geocoded via: {strategy_used}</span>"
        
        popup_content += "</div>"
        
        color, icon, icon_color, category = self.get_marker_style(status)
        
        # Add marker to the appropriate category cluster
        target_cluster = self.category_groups.get(category, self.category_groups['Other'])
        
        folium.Marker(
            location=[offset_lat, offset_lon],
            popup=folium.Popup(popup_content, max_width=400),
            icon=folium.Icon(color=color, icon=icon, icon_color=icon_color, prefix='fa'),
            tooltip=f"{first_name} {last_name} ({status})"
        ).add_to(target_cluster)
    
    def process_contacts(self, csv_file='Contacts_2025_05_29.csv', test_limit=None):
        """Process contacts from CSV file"""
        logging.info(f"Starting to process contacts from {csv_file}")
        
        # Track file-specific stats
        file_stats = {
            'total_records': 0,
            'cache_hits': 0,
            'api_calls': 0,
            'successful_geocodes': 0,
            'failed_geocodes': 0,
            'skipped_records': 0,
            'clustered_locations': 0
        }
        
        try:
            df = pd.read_csv(csv_file)
        except FileNotFoundError:
            logging.error(f"CSV file {csv_file} not found")
            return
        except Exception as e:
            logging.error(f"Error reading CSV file: {e}")
            return
        
        required_columns = ['Contact Name', 'Email', 'Phone', 
                          'Mailing Street', 'Mailing City', 'Mailing State', 'Mailing Zip', 'ID/Status']
        
        # Check if required columns exist (some might have different names)
        available_columns = df.columns.tolist()
        logging.info(f"Available columns: {available_columns}")
        
        # Try to map columns if they have slightly different names
        column_mapping = {}
        
        # Define alternative column name mappings
        column_alternatives = {
            'Contact Name': ['Contact Name', 'Lead Name', 'Name', 'Full Name'],
            'Email': ['Email', 'Email Address', 'E-mail'],
            'Phone': ['Phone', 'Phone Number', 'Mobile', 'Cell Phone'],
            'Mailing Street': ['Mailing Street', 'Street', 'Address', 'Street Address'],
            'Mailing City': ['Mailing City', 'City'],
            'Mailing State': ['Mailing State', 'State'],
            'Mailing Zip': ['Mailing Zip', 'Zip Code', 'Zip', 'Postal Code'],
            'ID/Status': ['ID/Status', 'Status', 'ID', 'Lead Status']
        }
        
        for req_col in required_columns:
            # First try exact match
            if req_col in available_columns:
                column_mapping[req_col] = req_col
            else:
                # Try alternative names
                found = False
                for alt_name in column_alternatives.get(req_col, []):
                    if alt_name in available_columns:
                        column_mapping[req_col] = alt_name
                        found = True
                        break
                
                # If still not found, try fuzzy matching
                if not found:
                    for col in available_columns:
                        if req_col.lower().replace(' ', '').replace('mailing', '') in col.lower().replace(' ', ''):
                            column_mapping[req_col] = col
                            break
        
        logging.info(f"Column mapping: {column_mapping}")
        
        # Check if we have the minimum required columns
        if 'Mailing City' not in column_mapping or 'ID/Status' not in column_mapping:
            logging.error(f"Missing required columns. Found mapping: {column_mapping}")
            logging.error(f"Required: Mailing City (or City), ID/Status (or Status)")
            return
        
        # Filter out rows with missing critical data
        city_col = column_mapping.get('Mailing City')
        status_col = column_mapping.get('ID/Status')
        
        if city_col and status_col:
            df_filtered = df.dropna(subset=[city_col, status_col])
        else:
            logging.error(f"Could not find required columns for filtering")
            return
        
        # Apply test limit if specified
        if test_limit:
            df_filtered = df_filtered.head(test_limit)
            logging.info(f"Limiting processing to {test_limit} records for testing")
        
        file_stats['total_records'] = len(df_filtered)
        self.stats['total_records'] += file_stats['total_records']
        logging.info(f"Processing {file_stats['total_records']} records from {csv_file}")
        
        for index, row in df_filtered.iterrows():
            try:
                # Extract data with error handling for NaN values
                contact_name = safe_str(row.get(column_mapping.get('Contact Name', ''), ''), 'Unknown Contact')
                
                # Split contact name into first and last name
                if contact_name and contact_name != 'Unknown Contact':
                    name_parts = contact_name.split(' ', 1)  # Split on first space only
                    first_name = name_parts[0] if len(name_parts) > 0 else 'Unknown'
                    last_name = name_parts[1] if len(name_parts) > 1 else ''
                else:
                    first_name = 'Unknown'
                    last_name = 'Contact'
                
                email = safe_str(row.get(column_mapping.get('Email', ''), ''), 'None')
                phone = safe_str(row.get(column_mapping.get('Phone', ''), ''), 'None')
                status = safe_str(row.get(column_mapping.get('ID/Status', ''), ''), 'Unknown')
                
                # Skip if status is empty or certain inactive statuses
                if not status or status.upper() in ['INACTIVE', 'NAN', 'NONE', '']:
                    self.stats['skipped_records'] += 1
                    continue
                
                # Preprocess address
                street = safe_str(row.get(column_mapping.get('Mailing Street', ''), ''), '')
                city = safe_str(row.get(column_mapping.get('Mailing City', ''), ''), '')
                state = safe_str(row.get(column_mapping.get('Mailing State', ''), ''), '')
                zip_code = safe_str(row.get(column_mapping.get('Mailing Zip', ''), ''), '')
                
                address_dict = self.preprocessor.preprocess_address(street, city, state, zip_code)
                
                # Skip if no city (minimum requirement)
                if not address_dict['city']:
                    self.stats['skipped_records'] += 1
                    logging.debug(f"Skipping record with no city: {first_name} {last_name}")
                    continue
                
                # Geocode the address
                lat, lon, full_address, strategy_used = self.geocode_address(address_dict)
                
                if lat and lon:
                    self.create_marker(lat, lon, first_name, last_name, email, phone, status, full_address, strategy_used)
                    logging.debug(f"Added marker for {first_name} {last_name} at {lat}, {lon}")
                else:
                    logging.warning(f"Could not geocode address for {first_name} {last_name}: {address_dict}")
                
            except Exception as e:
                logging.error(f"Error processing record {index}: {e}")
                continue
        
        # Save cache after processing
        self.cache.save_cache()
        
        # Log file-specific statistics
        logging.info(f"=== File Summary for {csv_file} ===")
        logging.info(f"Records processed: {file_stats['total_records']}")
        logging.info(f"Records successfully added to map: {file_stats['total_records'] - self.stats['skipped_records'] + file_stats['skipped_records']}")
        
        # Create location summary markers for crowded areas
        self._create_location_summaries()
        
        # Log overall statistics
        self.log_statistics()
    
    def _create_location_summaries(self):
        """Create summary markers for locations with multiple people"""
        summary_count = 0
        
        for coord_key, people in self.coordinate_groups.items():
            if len(people) > 1:  # Only create summaries for multiple people
                # Parse coordinates from key
                lat_str, lon_str = coord_key.split(',')
                base_lat, base_lon = float(lat_str), float(lon_str)
                
                # Create a summary popup
                popup_content = self._create_grouped_popup(coord_key)
                
                # Create a special summary marker in the "Multiple People" category
                folium.Marker(
                    location=[base_lat, base_lon],
                    popup=folium.Popup(popup_content, max_width=500),
                    icon=folium.Icon(
                        color='darkred', 
                        icon='users', 
                        icon_color='white', 
                        prefix='fa'
                    ),
                    tooltip=f"📍 {len(people)} people at this location"
                ).add_to(self.category_groups['Multiple People'])
                
                summary_count += 1
        
        if summary_count > 0:
            self.stats['clustered_locations'] = summary_count
            logging.info(f"Created {summary_count} location summary markers for clustered areas")
    
    def log_statistics(self):
        """Log processing statistics"""
        logging.info("=== Processing Statistics ===")
        logging.info(f"Total records processed: {self.stats['total_records']}")
        logging.info(f"Cache hits: {self.stats['cache_hits']}")
        logging.info(f"API calls made: {self.stats['api_calls']}")
        logging.info(f"Successful geocodes: {self.stats['successful_geocodes']}")
        logging.info(f"Failed geocodes: {self.stats['failed_geocodes']}")
        logging.info(f"Skipped records: {self.stats['skipped_records']}")
        logging.info(f"Clustered locations: {self.stats.get('clustered_locations', 0)}")
        
        # Coordinate distribution analysis
        total_unique_locations = len(self.coordinate_groups)
        locations_with_multiple_people = sum(1 for people in self.coordinate_groups.values() if len(people) > 1)
        
        logging.info(f"Unique coordinate locations: {total_unique_locations}")
        logging.info(f"Locations with multiple people: {locations_with_multiple_people}")
        
        cache_hit_rate = (self.stats['cache_hits'] / (self.stats['cache_hits'] + self.stats['api_calls'])) * 100 if (self.stats['cache_hits'] + self.stats['api_calls']) > 0 else 0
        success_rate = (self.stats['successful_geocodes'] / self.stats['api_calls']) * 100 if self.stats['api_calls'] > 0 else 0
        
        logging.info(f"Cache hit rate: {cache_hit_rate:.1f}%")
        logging.info(f"Geocoding success rate: {success_rate:.1f}%")
    
    def save_map(self, filename=None):
        """Save the map to HTML file"""
        output_file = filename or self.config['output_file']
        self.map.save(output_file)
        logging.info(f"Map saved to {output_file}")
    
    def add_legend(self):
        """Add a legend to the map"""
        legend_html = '''
        <div style="position: fixed; 
                    bottom: 50px; left: 50px; width: 320px; height: auto; 
                    background-color: white; border:2px solid grey; z-index:9999; 
                    font-size:16px; padding: 20px; border-radius: 5px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
        <h4 style="margin-top: 0; margin-bottom: 15px; color: #333; font-size: 18px;">📍 Contact Legend</h4>
        <div style="margin-bottom: 8px; font-size: 15px;"><i class="fa fa-star" style="color:red; width: 20px; font-size: 16px;"></i> Workers Comp</div>
        <div style="margin-bottom: 8px; font-size: 15px;"><i class="fa fa-dollar" style="color:orange; width: 20px; font-size: 16px;"></i> WC Client</div>
        <div style="margin-bottom: 8px; font-size: 15px;"><i class="fa fa-dollar" style="color:green; width: 20px; font-size: 16px;"></i> Client</div>
        <div style="margin-bottom: 8px; font-size: 15px;"><i class="fa fa-medkit" style="color:purple; width: 20px; font-size: 16px;"></i> Medical Professional</div>
        <div style="margin-bottom: 8px; font-size: 15px;"><i class="fa fa-thumbs-up" style="color:darkpurple; width: 20px; font-size: 16px;"></i> Good Progress</div>
        <div style="margin-bottom: 8px; font-size: 15px;"><i class="fa fa-user" style="color:red; width: 20px; font-size: 16px;"></i> Amputee</div>
        <div style="margin-bottom: 8px; font-size: 15px;"><i class="fa fa-wrench" style="color:blue; width: 20px; font-size: 16px;"></i> Prosthetist</div>
        <div style="margin-bottom: 8px; font-size: 15px;"><i class="fa fa-info-circle" style="color:gray; width: 20px; font-size: 16px;"></i> Other</div>
        <hr style="margin: 15px 0;">
        <div style="margin-bottom: 8px; font-size: 15px;"><i class="fa fa-users" style="color:darkred; width: 20px; font-size: 16px;"></i> Multiple People</div>
        <hr style="margin: 15px 0;">
        <p style="margin: 0 0 10px 0; font-size: 14px; color: #2c5234; font-weight: bold;">
            💡 Use the layer control (top-right) to toggle categories on/off
        </p>
        <p style="margin: 0; font-size: 13px; color: #666;">
            Markers are clustered automatically. Individual markers are offset when multiple people share the same location.
        </p>
        </div>
        '''
        self.map.get_root().html.add_child(folium.Element(legend_html))

def main():
    """Main function to run the enhanced mapper"""
    # Configuration
    config = {
        'api_delay': 1.5,  # Slower for free API
        'timeout': 15,
        'max_retries': 2,
        'output_file': 'enhanced_map.html'
    }
    
    # Create mapper instance
    mapper = EnhancedGeoMapper(config)
    
    # Process contacts
    mapper.process_contacts()
    
    # Add legend
    mapper.add_legend()
    
    # Save map
    mapper.save_map()
    
    print("Enhanced mapping complete! Check enhanced_map.html and geocoding.log for details.")

if __name__ == "__main__":
    main()
