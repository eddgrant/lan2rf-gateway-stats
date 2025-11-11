# TASKS

1. ~~Implement something that sends the status data to InfluxDB~~
2. ~~Implement something which periodically obtains the LAN2RF data.~~
3. ~~StatusDataPublisher: Make it possible to configure the publish interval via @ConfigurationProperties~~
4. ~~StatusDataPublisher: Update unit tests to use @ConfigurationProperties~~
5. ~~StatusDataPublisherTest: Mock StatusDataPublisherTest so we're not reliant on being on-network to run the tests.~~
6. ~~influxdb.StatusDataEmitter: Update to consume the Flux<StatusData> returned by the StatusDataPublisher~~
7. ~~influxdb.StatusDataEmitter: Rename?~~
8. ~~Fix failing tests~~
9. On startup, log the configured check interval.
10. Add a DEBUG level log in StatusDataPublisher, logging the data that is sent to InfluxDB.
11. Support consumption of data from LAN2RF devices which have basic auth enabled
12. Identify areas where tests are missing and write them
13. ~~Build the app as a Docker image and test that it works~~
14. Can the built Docker image be tested conveniently as a Gradle task? What did I do for influxdb-weather-ingestor?


**Note:** Do not upgrade to jvm-test-suite plugin yet. It's in incubating and doesn't work very well! When we tried to use it IntelliJ was unable to resolve classpath dependencies

# Decisions

## InfluxDB Data Design

### Options

1. A single measurement, perhaps "status" or "lan2rf"
2. Separate measurements: Pressure, Temperature, Status etc