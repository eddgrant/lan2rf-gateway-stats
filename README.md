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

Ensure that you set a valid hostname / IP address for the `LAN2RF_URL` environment variable.

## Check the logs

lan2rf-gateway-stats should start and begin to log its output to the console:

```
 __  __ _                                  _   
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_ 
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_ 
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
18:54:27.578 [main] INFO  i.m.l.PropertiesLoggingLevelsConfigurer - Setting log level 'ERROR' for logger: 'com'
18:54:27.579 [main] INFO  i.m.l.PropertiesLoggingLevelsConfigurer - Setting log level 'INFO' for logger: 'com.eddgrant'
18:54:27.580 [main] INFO  i.m.l.PropertiesLoggingLevelsConfigurer - Setting log level 'ERROR' for logger: 'io'
18:54:27.463 [main] INFO  c.e.l.LAN2RFGatewayStatsApp - Starting subscription to LAN2RF data.
18:54:27.567 [main] INFO  c.e.l.LAN2RFGatewayStatsApp - Subscription to LAN2RF data created.
```

By default lan2rf-gateway-stats will check the status data every minute.

This can be altered by setting the `LAN2RF_CHECK_INTERVAL` environment variable e.g.

```shell
CHECKS_SCHEDULE_EXPRESSION=30s`
````

`LAN2RF_CHECK_INTERVAL` uses a duration format, so the following are all valid values:

```shell
LAN2RF_CHECK_INTERVAL=30s # Check every 30 seconds
LAN2RF_CHECK_INTERVAL=2m # Check every 2 minutes
LAN2RF_CHECK_INTERVAL=4h # Check every 4 hours
```

Each time the status data is sent to InfluxDB an `INFO` level log entry is written e.g.

```shell
19:00:25.478 [DefaultDispatcher-worker-1] INFO  c.e.l.p.influxdb.StatusDataPublisher - Status Data measurements sent at: 2025-11-04T19:00:25.443115704Z
```

## The Data

<!-- TODO: --> Document which data gets sent and how it is represented in InfluxDB.