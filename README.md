# Curtain Plugin

## Overview
The Curtain Plugin allows administrators to temporarily restrict access to specific apps by displaying a "404 Not Found" page to non-admin users. This can be useful during maintenance periods or when you need to temporarily disable access to certain applications.

## Features
- Selectively restrict access to specific Joget applications
- Only users with `ROLE_ADMIN` can access "curtained" applications
- Non-admin users will see a "404 Not Found" page when attempting to access restricted apps
- Easy configuration through "System Settings" > "Manage Plugin"

## Requirements
- Joget DX 8.2.0 or above
- Admin user with `ROLE_ADMIN` access to configure the plugin

## Installation
1. Download the plugin JAR file
2. Login to Joget with `ROLE_ADMIN`
3. Go to "System Settings" > "Manage Plugins"
4. Upload and install the plugin JAR file
5. The plugin will be available in the "System Settings" > "Manage Plugin" > "Installed Plugin" tab

## Configuration
1. Navigate to "System Settings" > "Manage Plugin" > "Configurable Plugins" tab
2. Look for "Curtain Plugin" in the list
3. Click on the plugin to configure:
   - Enable/disable the curtain functionality
   - Add new apps selection using the "PLUS" (+) button
   - Choose which app to restrict access to non-admin users
   - Click "Submit" to save

## Usage
Once configured:
- Users with `ROLE_ADMIN` can still access all applications normally
- Non-admin users will see a "404 Not Found" page when trying to access curtained applications
- The curtain can be lifted by disabling the plugin or removing applications from the restricted list

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## License
[MIT](https://opensource.org/licenses/MIT)

## Support
For bug reports and feature requests, please create an issue in the repository.