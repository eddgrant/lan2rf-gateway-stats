# Support LAN2RF devices with Basic Auth

Make it possible to disable sending of different measurements, to save data storage costs.

Depending on their use case, many LAN2RF owners won't be interested in some of the LAN2RF measurements.

In order to help users minimise the data they have to store in InfluxDB, we should allow them to configure which measurements are published by lan2rf-gateway-stats.

## Measurement Groups

1. Boiler related measurements
    - Central heating and Tap Temperatures
    - Status Display Code (TextStatus)
    - All OperationalStatus measurements
2. Room 1 measurements
3. Room 2 measurements

## Acceptance Criteria

1. The application defaults to sending all measurements.
2. It is possible to selectively disable any combination of the above measurement groups via configuration.
3. The README file explains the measurement configuration options.
4. On startup the application logs which measurement groups are configured to be sent.

## Implementation Requirements
1. Any existing tests are updated, as required.
2. Any new tests are written, as required.
3. All code changes follow the existing coding standards.
4. Consider whether it would be better to use the existing `LAN2RFConfiguration` class or create a new one specifically for measurement configuration.