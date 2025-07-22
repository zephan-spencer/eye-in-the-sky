#!/usr/bin/env python3
"""
Command-line interface for the Enhanced Geo Mapper
"""

import argparse
import json
import sys
import os
from enhanced_parser import EnhancedGeoMapper

def load_config(config_file='config.json'):
    """Load configuration from JSON file"""
    try:
        with open(config_file, 'r') as f:
            return json.load(f)
    except FileNotFoundError:
        print(f"Config file {config_file} not found, using defaults")
        return {}
    except json.JSONDecodeError:
        print(f"Invalid JSON in config file {config_file}, using defaults")
        return {}

def main():
    parser = argparse.ArgumentParser(
        description='Enhanced Geo Mapper - Create maps from CSV data with geocoding cache'
    )
    
    parser.add_argument(
        'csv_files', 
        nargs='*', 
        default=None,
        help='CSV files to process (supports multiple files). If none specified, uses config file settings.'
    )
    
    parser.add_argument(
        '-o', '--output',
        default=None,
        help='Output HTML file name (default: from config or enhanced_map.html)'
    )
    
    parser.add_argument(
        '-c', '--config',
        default='config.json',
        help='Configuration file (default: config.json)'
    )
    
    parser.add_argument(
        '--clear-cache',
        action='store_true',
        help='Clear the geocoding cache before processing'
    )
    
    parser.add_argument(
        '--cache-only',
        action='store_true',
        help='Only use cached results, skip new geocoding'
    )
    
    parser.add_argument(
        '--delay',
        type=float,
        help='Override API delay between calls (seconds)'
    )
    
    parser.add_argument(
        '--stats-only',
        action='store_true',
        help='Show cache statistics and exit'
    )
    
    parser.add_argument(
        '-v', '--verbose',
        action='store_true',
        help='Enable verbose logging'
    )
    
    parser.add_argument(
        '--records',
        type=int,
        help='Limit the number of records to process (for testing)'
    )
    
    args = parser.parse_args()
    
    # Load configuration
    config_data = load_config(args.config)
    
    # Extract config for mapper
    mapper_config = {}
    
    if 'api_settings' in config_data:
        api_settings = config_data['api_settings']
        mapper_config['api_delay'] = api_settings.get('delay_between_calls', 1.5)
        mapper_config['timeout'] = api_settings.get('timeout_seconds', 15)
        mapper_config['max_retries'] = api_settings.get('max_retries', 2)
    
    if 'map_settings' in config_data:
        map_settings = config_data['map_settings']
        mapper_config['center_lat'] = map_settings.get('center_latitude', 40)
        mapper_config['center_lon'] = map_settings.get('center_longitude', -95)
        mapper_config['zoom_start'] = map_settings.get('zoom_start', 4)
        mapper_config['output_file'] = map_settings.get('output_file', 'enhanced_map.html')
    
    if 'file_settings' in config_data:
        file_settings = config_data['file_settings']
        mapper_config['log_file'] = file_settings.get('log_file', 'geocoding.log')
    
    # Check for processing settings
    test_limit = None
    if 'processing_settings' in config_data:
        processing_settings = config_data['processing_settings']
        test_limit = processing_settings.get('test_limit')
    
    # Apply command line overrides
    if args.delay:
        mapper_config['api_delay'] = args.delay
    
    if args.output:
        mapper_config['output_file'] = args.output
    
    if args.records:
        test_limit = args.records
    
    # Handle cache operations
    cache_file = config_data.get('file_settings', {}).get('cache_file', 'geocode_cache.json')
    
    if args.clear_cache:
        if os.path.exists(cache_file):
            os.remove(cache_file)
            print(f"Cleared cache file: {cache_file}")
        else:
            print(f"Cache file {cache_file} does not exist")
    
    if args.stats_only:
        # Show cache statistics
        from enhanced_parser import GeocodeCache
        cache = GeocodeCache(cache_file)
        print(f"Cache file: {cache_file}")
        print(f"Cached addresses: {len(cache.cache)}")
        if cache.cache:
            # Show some example addresses
            print("\\nSample cached addresses:")
            for i, (key, value) in enumerate(cache.cache.items()):
                if i >= 5:  # Show only first 5
                    break
                print(f"  {value['address']} (cached: {value['cached_date'][:10]})")
        return
    
    # Determine which files to process
    files_to_process = []
    
    if args.csv_files:
        # Use files from command line
        files_to_process = args.csv_files
        print(f"Processing {len(files_to_process)} files from command line:")
        for f in files_to_process:
            print(f"  - {f}")
    else:
        # Use files from config
        if 'file_settings' in config_data and 'input_files' in config_data['file_settings']:
            files_to_process = config_data['file_settings']['input_files']
            print(f"Processing {len(files_to_process)} files from config:")
            for f in files_to_process:
                print(f"  - {f}")
        else:
            # Fallback to single file from config or default
            default_file = (config_data.get('file_settings', {}).get('contacts_csv') or 
                          'Contacts_2025_05_29.csv')
            files_to_process = [default_file]
            print(f"Processing single file: {default_file}")

    if args.cache_only:
        mapper_config['api_delay'] = 0  # No delay needed
        mapper_config['max_retries'] = 0  # Don't retry
        print("Running in cache-only mode - no new geocoding will be performed")
    
    # Create and run mapper
    print(f"Output file: {mapper_config['output_file']}")
    print(f"API delay: {mapper_config['api_delay']} seconds")
    
    mapper = EnhancedGeoMapper(mapper_config)
    
    # Override geocoding if cache-only mode
    if args.cache_only:
        original_geocode = mapper.geocode_address
        def cache_only_geocode(address_dict):
            cached_result = mapper.cache.get(address_dict)
            if cached_result:
                mapper.stats['cache_hits'] += 1
                return cached_result['latitude'], cached_result['longitude'], cached_result['address']
            else:
                mapper.stats['failed_geocodes'] += 1
                return None, None, None
        mapper.geocode_address = cache_only_geocode
    
    # Process all files
    total_processed = 0
    for i, csv_file in enumerate(files_to_process, 1):
        print(f"\n--- Processing file {i}/{len(files_to_process)}: {csv_file} ---")
        
        # Check if file exists
        if not os.path.exists(csv_file):
            print(f"Warning: File {csv_file} not found, skipping...")
            continue
            
        try:
            mapper.process_contacts(csv_file, test_limit=test_limit)
            total_processed += 1
        except Exception as e:
            print(f"Error processing {csv_file}: {e}")
            continue
    
    # Add legend and save
    if total_processed > 0:
        mapper.add_legend()
        mapper.save_map()
        
        print(f"\n=== Mapping Complete! ===")
        print(f"Files processed: {total_processed}/{len(files_to_process)}")
        print(f"Map saved to: {mapper_config['output_file']}")
        print(f"Log file: {mapper_config['log_file']}")
        print(f"Cache file: {cache_file}")
        
        # Print combined statistics
        print("\n=== Combined Statistics ===")
        for key, value in mapper.stats.items():
            print(f"{key}: {value}")
    else:
        print("\nNo files were successfully processed!")
        sys.exit(1)

if __name__ == "__main__":
    main()
