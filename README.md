# lan2rf-gateway-stats

A simple utility which obtains data from an Intergas LAN2RF device, and sends it to an InfluxDB endpoint.

# How to use it

## Run InfluxDB locally

An InfluxDB endpoint is required to collect temperature data. You might have an InfluxDB setup already, but if not one can be easily created by running the following commands:

```shell
docker network create -d bridge lan2rf-gateway-stats
```

```shell
docker run --rm \
  --net=lan2rf-gateway-stats \
  -p 8086:8086 \
  -e DOCKER_INFLUXDB_INIT_MODE="setup" \
  -e DOCKER_INFLUXDB_INIT_USERNAME="my-very-secure-influxdb-username" \
  -e DOCKER_INFLUXDB_INIT_PASSWORD="my-very-secure-influxdb-password" \
  -e DOCKER_INFLUXDB_INIT_ORG="my-influxdb-org" \
  -e DOCKER_INFLUXDB_INIT_BUCKET="intergas" \
  -e DOCKER_INFLUXDB_INIT_RETENTION="1w" \
  -e DOCKER_INFLUXDB_INIT_ADMIN_TOKEN="my-very-secure-influxdb-token" \
  --name influxdb \
  influxdb:2.0
```

## Run lan2rf-gateway-stats

To run the lan2rf-gateway-stats Docker image run the following command:

```shell
docker run --rm \
  --net=lan2rf-gateway-stats \
  --env LAN2RF_CHECK_INTERVAL=30s \
  --env LAN2RF_URL="http://192.168.2.58" \
  --env INFLUXDB_ORG="my-influxdb-org" \
  --env INFLUXDB_BUCKET="intergas" \
  --env INFLUXDB_TOKEN="my-very-secure-influxdb-token" \
  --env INFLUXDB_URL="http://influxdb:8086?connectTimeout=5S&readTimeout=5S&writeTimeout=5S" \
   eddgrant/lan2rf-gateway-stats:local
```

<!-- TODO: Need to update the README from hereon down... -->

Ensure that the InfluxDB variables match the ones used when setting up InfluxDB.

Ensure that you set a valid UK postcode for the `CHECKS_POSTCODE` environment variable.

Ensure that you set your Meteomatics username and password for the `METEOMATICS_USERNAME` and `METEOMATICS_PASSWORD` environment variables.

## Check the logs

lan2rf-gateway-stats should start and begin to log its output to the console:

```
 __  __ _                                  _   
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_ 
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_ 
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
18:09:08.145 [main] INFO  i.m.l.PropertiesLoggingLevelsConfigurer - Setting log level 'INFO' for logger: 'io.http.client'
18:09:08.147 [main] INFO  i.m.l.PropertiesLoggingLevelsConfigurer - Setting log level 'DEBUG' for logger: 'io.retry'
18:09:08.147 [main] INFO  i.m.l.PropertiesLoggingLevelsConfigurer - Setting log level 'INFO' for logger: 'com.eddgrant'
18:09:08.147 [main] INFO  i.m.l.PropertiesLoggingLevelsConfigurer - Setting log level 'INFO' for logger: 'io.micronaut'
18:09:08.556 [main] INFO  c.e.i.checks.RegisterChecksAction - Temperature checks scheduled to run on schedule: * * * * *
18:09:08.560 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 606ms. Server Running: http://4294397d97fa:8080
```

By default lan2rf-gateway-stats will check the temperature every minute.

This can be altered by setting the `CHECKS_SCHEDULE_EXPRESSION` environment variable e.g.

```shell
CHECKS_SCHEDULE_EXPRESSION=*/10 * * * *`
````

Each time a temperature measurement is sent to InfluxDB an `INFO` level log entry is written e.g.

```shell
17:23:01.780 [scheduled-executor-thread-1] INFO  c.e.i.checks.TemperatureEmitter - Temperature measurement sent: Postcode: AB12 3CD, Temperature: 13.6
```

Development related information can be found in [docs/development.md](docs/development.md)