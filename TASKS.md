# TASKS

1. ~~Implement something that sends the status data to InfluxDB~~
2. ~~Implement something which periodically obtains the LAN2RF data.~~
3. ~~StatusDataPublisher: Make it possible to configure the publish interval via @ConfigurationProperties~~
4. ~~StatusDataPublisher: Update unit tests to use @ConfigurationProperties~~
5. ~~StatusDataPublisherTest: Mock StatusDataPublisherTest so we're not reliant on being on-network to run the tests.~~
6. influxdb.StatusDataEmitter: Update to consume the Flux<StatusData> returned by the StatusDataPublisher
7. influxdb.StatusDataEmitter: Rename?

# Decisions

## InfluxDB Data Design

### Options

1. A single measurement, perhaps "status" or "lan2rf"
2. Separate measurements: Pressure, Temperature, Status etc