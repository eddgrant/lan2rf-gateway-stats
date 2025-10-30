# TASKS

1. ~~Implement something that sends the status data to InfluxDB~~
2. ~~Implement something which periodically obtains the LAN2RF data.~~
3. ~~StatusDataPublisher: Make it possible to configure the publish interval via @ConfigurationProperties~~
4. ~~StatusDataPublisher: Update unit tests to use @ConfigurationProperties~~
5. ~~StatusDataPublisherTest: Mock StatusDataPublisherTest so we're not reliant on being on-network to run the tests.~~
6. ~~influxdb.StatusDataEmitter: Update to consume the Flux<StatusData> returned by the StatusDataPublisher~~
7. ~~influxdb.StatusDataEmitter: Rename?~~
8. ~~Fix failing tests~~
9. Identify areas where tests are missing and write them
10. Fix Gradle warnings
    * Do not upgrade to jvm-test-suite plugin yet. It's in incubating and doesn't work very well!
      * IntelliJ was unable to resolve classpath dependencies
9. Build the app as a native image and test that it works
  * Can this be tested conveniently as a Gradle task? What did I do for influxdb-weather-ingestor?
10. Support consumption of data from LAN2RF devices which have basic auth enabled

# Decisions

## InfluxDB Data Design

### Options

1. A single measurement, perhaps "status" or "lan2rf"
2. Separate measurements: Pressure, Temperature, Status etc