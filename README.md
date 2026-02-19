# Eye in the Sky - Geocoding Map Generator

An intelligent mapping application that creates interactive maps from CSV contact data with advanced features including geocoding cache, address preprocessing, and rate limiting management. Now supports multiple data sources through JSON column mapping configuration.

## Features

### 🚀 **Core Improvements**
- **Geocoding Cache**: Stores previously geocoded addresses to avoid redundant API calls
- **Address Preprocessing**: Cleans and standardizes addresses for better geocoding accuracy
- **Rate Limiting**: Configurable delays to respect API limitations
- **Incremental Updates**: Only geocodes new/changed addresses
- **Error Handling**: Robust error handling with retry logic
- **Comprehensive Logging**: Detailed logs of all operations
- **JSON Column Mapping**: Process multiple CSV sources with different column structures
- **Multi-Source Support**: Automatically processes Contacts and Leads into a unified map
- **File Processing Summary**: Displays summary of all files processed at completion

### 📊 **Smart Address Processing**
- Standardizes street abbreviations (St. → Street, Ave. → Avenue)
- Expands directional abbreviations (N → North, SW → Southwest)
- Removes apartment/unit information that can confuse geocoding
- Normalizes zip codes to 5-digit format
- Handles missing or incomplete address data gracefully

### 🎯 **Enhanced Mapping**
- Color-coded markers by contact status
- Detailed popups with contact information
- Interactive legend
- Configurable map center and zoom
- Support for multiple contact types

## Quick Start

### 1. Install Dependencies
```bash
pip install pandas folium geopy
```

### 2. Run with Default Settings
```bash
# Run the main geocoder (processes both Contacts and Leads)
python geocoder.py

# Or use the CLI interface
python map_cli.py
```

### 3. View Your Map
Open `enhanced_map.html` in your web browser

## Advanced Usage

### Command Line Options
```bash
# Basic usage
python map_cli.py Contacts_2025_05_29.csv

# Custom output file
python map_cli.py -o my_map.html

# Clear cache and regenerate all
python map_cli.py --clear-cache

# Use only cached data (no new API calls)
python map_cli.py --cache-only

# Custom API delay
python map_cli.py --delay 2.0

# Show cache statistics
python map_cli.py --stats-only

# Verbose logging
python map_cli.py -v
```

### Configuration File
Edit `config.json` to customize behavior:

```json
{
  "api_settings": {
    "delay_between_calls": 1.5,
    "timeout_seconds": 15,
    "max_retries": 2
  },
  "map_settings": {
    "center_latitude": 40,
    "center_longitude": -95,
    "zoom_start": 4
  }
}
```

## File Structure

```
eye-in-the-sky/
├── geocoder.py              # Main geocoding and mapping logic
├── column_mappings.json     # JSON column mapping configuration
├── map_cli.py               # Command-line interface
├── config.json              # Configuration settings
├── Contacts_2025_05_29.csv  # Contact data
├── Leads_2025_05_29.csv     # Lead data (optional, auto-detected)
├── geocode_cache.json       # Cached geocoding results (auto-generated)
├── geocoding.log            # Processing logs (auto-generated)
└── enhanced_map.html        # Generated map (auto-generated)
```

## Understanding the Cache System

The geocoding cache (`geocode_cache.json`) stores previously geocoded addresses with:
- **Normalized address keys**: Ensures consistent caching
- **Coordinates**: Latitude and longitude
- **Full address**: Complete address returned by geocoding service
- **Cache date**: When the address was first geocoded

### Cache Benefits
- **Faster processing**: No API calls for previously geocoded addresses
- **Cost savings**: Reduces API usage for paid geocoding services
- **Reliability**: Works even when geocoding service is unavailable

## Marker Color Legend

| Color | Icon | Status Type |
|-------|------|-------------|
| 🔴 Red | ⭐ Star | Workers Comp |
| 🟠 Orange | 💲 Dollar | WC Client |
| 🟢 Green | 💲 Dollar | Client |
| 🟣 Purple | ⚕️ Medkit | Medical Professional |
| 🟤 Dark Purple | 👍 Thumbs Up | Good Progress |
| 🔵 Light Blue | ❓ Question | Amputee |
| 🔵 Blue | 🔧 Wrench | Prosthetist |
| ⚪ Gray | ℹ️ Info | Other |

## Troubleshooting

### Common Issues

**"No results for address"**
- Check if address has sufficient information (city is minimum requirement)
- Verify address format in CSV
- Some addresses may be too specific or contain errors

**"API rate limit exceeded"**
- Increase `delay_between_calls` in config.json
- Use `--cache-only` mode to work with existing cached data

**"Cache file corrupt"**
- Delete `geocode_cache.json` and re-run
- Use `--clear-cache` flag

### Performance Tips

1. **First run**: Will be slow as all addresses are geocoded
2. **Subsequent runs**: Much faster due to caching
3. **Large datasets**: Consider processing in batches
4. **API limits**: Adjust delay settings for your geocoding service

## CSV File Requirements

The system supports multiple CSV formats and automatically detects and processes them based on the `column_mappings.json` configuration.

### Supported File Types

**Contacts CSV** (e.g., `Contacts_2026_01_10.csv`):
- `First Name`, `Last Name`
- `Email`, `Phone`
- `ID/Status`
- `Mailing Street`, `Mailing City`, `Mailing State`, `Mailing Zip`, `Mailing Country`

**Leads CSV** (e.g., `Leads_2026_01_10.csv`):
- `First Name`, `Last Name`
- `Email`, `Phone`
- `ID/Status`
- `Street`, `City`, `State`, `Country`

### Required Fields
At minimum, each record must have:
- `first_name` or `last_name`
- `city` (used for geocoding)
- `status` (for marker categorization)

All other fields are optional.

## Statistics and Monitoring

The application provides detailed statistics:
- Total records processed
- Cache hit rate
- API calls made
- Success/failure rates
- Processing time

Check `geocoding.log` for detailed operation logs.

## Advanced Configuration

### Custom Marker Styles
Edit the `marker_styles` section in `config.json` to customize colors and icons for different status types.

### Address Cleaning Rules
Modify `address_cleaning` settings to adjust how addresses are preprocessed.

### API Settings
Adjust `api_settings` to work with different geocoding services or account limits.

## Column Mapping Configuration

The `column_mappings.json` file allows you to map different CSV column names to a standardized internal format. This enables the system to process multiple CSV sources with different column structures into a single unified map.

### Configuration Format
```json
{
  "sources": {
    "contacts": {
      "file_pattern": "Contacts_*.csv",
      "columns": {
        "first_name": "First Name",
        "last_name": "Last Name",
        "email": "Email",
        "phone": "Phone",
        "status": "ID/Status",
        "street": "Mailing Street",
        "city": "Mailing City",
        "state": "Mailing State",
        "zip": "Mailing Zip",
        "country": "Mailing Country"
      }
    },
    "leads": {
      "file_pattern": "Leads_*.csv",
      "columns": {
        "first_name": "First Name",
        "last_name": "Last Name",
        "email": "Email",
        "phone": "Phone",
        "status": "ID/Status",
        "street": "Street",
        "city": "City",
        "state": "State",
        "zip": null,
        "country": "Country"
      }
    }
  }
}
```

### Adding New Data Sources
To add a new CSV source type:
1. Add a new entry under `sources` in `column_mappings.json`
2. Specify the `file_pattern` to match your CSV files (e.g., "Customers_*.csv")
3. Map each standard field to your CSV's column names
4. Set any unused fields to `null`

### File Processing Summary
At the end of execution, a summary shows all files processed:
```
============================================================
FILES PROCESSED SUMMARY
============================================================
  Contacts_2026_01_10.csv                  (contacts  ) -   150 records
  Leads_2026_01_10.csv                     (leads     ) -   892 records
------------------------------------------------------------
  TOTAL                                                 -  1042 records
============================================================
```

## License

This project is provided as-is for educational and business use.
