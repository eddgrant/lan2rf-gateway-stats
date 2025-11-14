# lan2rf-gateway-stats

A simple utility which obtains data from an Intergas LAN2RF device, and sends it to InfluxDB.

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
18:54:27.573 [main] INFO  c.e.l.intergas.LAN2RFConfiguration - LAN2RF Configuration: source='lan2rf', room1Name='room1', room2Name='room2', checkInterval='PT30S'
18:54:27.578 [main] INFO  i.m.l.PropertiesLoggingLevelsConfigurer - Setting log level 'ERROR' for logger: 'com'
18:54:27.579 [main] INFO  i.m.l.PropertiesLoggingLevelsConfigurer - Setting log level 'INFO' for logger: 'com.eddgrant'
18:54:27.580 [main] INFO  i.m.l.PropertiesLoggingLevelsConfigurer - Setting log level 'ERROR' for logger: 'io'
18:54:27.463 [main] INFO  c.e.l.LAN2RFGatewayStatsApp - Starting subscription to LAN2RF data.
18:54:27.567 [main] INFO  c.e.l.LAN2RFGatewayStatsApp - Subscription to LAN2RF data created.
18:54:27.623 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 644ms. Server Running: http://9cbacfcd290f:8080
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

The application publishes several types of measurements to InfluxDB, each representing a different aspect of the heating system's status.

### Measurements

All measurements have a `source` value which defaults to "lan2rf". This can be overridden by setting the value of the `LAN2RF_SOURCE` environment variable.

Below is a description of each measurement, its associated tags, and the data it holds.

**`Temperature`**

| Tag (`source`)               | Tag (`location`)                           | Tag (`type`)                                | Field (`value`)                     | Description                                                                                               |
|:-----------------------------|:-------------------------------------------|:--------------------------------------------|:------------------------------------|:----------------------------------------------------------------------------------------------------------|
| `${LAN2RF_SOURCE:-"lan2rf"}` | `central_heating`, `tap`, `room1`, `room2` | `RECORDED`, `SETPOINT`, `SETPOINT_OVERRIDE` | The temperature in degrees Celsius. | Stores temperature readings, target setpoints, and temporary overrides for different parts of the system. |

The `location` tags `room1` and `room2` use default names of `room1` and `room2`. These can be overridden by setting the `LAN2RF_ROOM1_NAME` and `LAN2RF_ROOM2_NAME` environment variables.

**`Pressure`**

| Tag (`source`)               | Tag (`location`)  | Field (`value`)      | Description                                               |
|:-----------------------------|:------------------|:---------------------|:----------------------------------------------------------|
| `${LAN2RF_SOURCE:-"lan2rf"}` | `central_heating` | The pressure in bar. | Stores the water pressure of the central heating circuit. |

**`OperationalStatus`**

| Tag (`source`)               | Tag (`name`)                                                        | Field (`value`)   | Description                                                      |
|:-----------------------------|:--------------------------------------------------------------------|:------------------|:-----------------------------------------------------------------|
| `${LAN2RF_SOURCE:-"lan2rf"}` | `LOCKED_OUT`, `PUMP_ACTIVE`, `TAP_FUNCTION_ACTIVE`, `BURNER_ACTIVE` | `true` or `false` | Stores the boolean on/off status for key operational components. |

**`TextStatus`**

| Tag (`source`)               | Tag (`subject`)       | Field (`value`)                                     | Description                                                       |
|:-----------------------------|:----------------------|:----------------------------------------------------|:------------------------------------------------------------------|
| `${LAN2RF_SOURCE:-"lan2rf"}` | `STATUS_DISPLAY_CODE` | A string representing the status (e.g., "Standby"). | Stores the human-readable status message displayed by the boiler. |

### Example Flux Queries

You can use the InfluxDB UI or API to query this data. Here are some example Flux queries to get you started.

**1. Get the last recorded central heating temperature:**

```flux
from(bucket: "intergas")
  |> range(start: -1h)
  |> filter(fn: (r) => r["source"] == "lan2rf")
  |> filter(fn: (r) => r["_measurement"] == "Temperature")
  |> filter(fn: (r) => r["location"] == "central_heating")
  |> filter(fn: (r) => r["type"] == "RECORDED")
  |> last()
```

**2. See when the burner has been active in the last hour:**

```flux
from(bucket: "intergas")
  |> range(start: -1h)
  |> filter(fn: (r) => r["source"] == "lan2rf")
  |> filter(fn: (r) => r["_measurement"] == "OperationalStatus")
  |> filter(fn: (r) => r["subject"] == "BURNER_ACTIVE")
  |> filter(fn: (r) => r["_value"] == true)
```

TODO: I don't know how the LAN2RF reports this data. Does it require the burner to be live exactly when the data request is made, or does it remember if the burner has been active within a given timeframe, instead reporting that back?
Need to understand this as this and the frequency at which data is collected could significantly influence reporting accuracy.

**3. Retrieve the boiler's text status over the last 24 hours:**

```flux
from(bucket: "intergas")
  |> range(start: -24h)
  |> filter(fn: (r) => r["source"] == "lan2rf")
  |> filter(fn: (r) => r["_measurement"] == "TextStatus")
  |> filter(fn: (r) => r["subject"] == "STATUS_DISPLAY_CODE")
  |> distinct(column: "_value")
```

**4. Graph the recorded room temperature vs. its setpoint and/or setpoint override for "room1":**

```flux
from(bucket: "intergas")
  |> range(start: -12h)
  |> filter(fn: (r) => r["source"] == "lan2rf")
  |> filter(fn: (r) => r["_measurement"] == "Temperature")
  |> filter(fn: (r) => r["location"] == "room1")
  |> filter(fn: (r) => r["type"] == "RECORDED" or r["type"] == "SETPOINT" or r["type"] == "SETPOINT_OVERRIDE")
  |> pivot(rowKey:["_time"], columnKey: ["type"], valueColumn: "_value")
```

**5. View all data points for each collection interval:**

This query groups all measurements by their timestamp, creating a wide table that shows the complete state of the system for each time the application published data.

```flux
from(bucket: "intergas")
  |> range(start: -10m)
  |> filter(fn: (r) => r["source"] == "lan2rf")
  |> pivot(
      rowKey:["_time"],
      columnKey: ["_measurement"],
      valueColumn: "_value"
  )
  |> sort(columns: ["_time"], desc: true)
```

## Debugging

If you want to see what data is being sent to InfluxDB, without having to query InfluxDB itself. You can configure `DEBUG` level logging on the `StatusDataPublisher` class logger. This will emit a log entry containing the measurement data, each time it is sent to InfluxDB e.g.

```shell
19:29:40.034 [DefaultDispatcher-worker-1] DEBUG c.e.l.p.influxdb.StatusDataPublisher - Measurements: [
Temperature(source=lan2rf, location=central_heating, value=52.25, type=RECORDED, time=2025-11-14T19:29:39.979889657Z),
Temperature(source=lan2rf, location=room1, value=20.0, type=RECORDED, time=2025-11-14T19:29:39.979889657Z),
Temperature(source=lan2rf, location=room2, value=327.67, type=RECORDED, time=2025-11-14T19:29:39.979889657Z),
Temperature(source=lan2rf, location=tap, value=36.37, type=RECORDED, time=2025-11-14T19:29:39.979889657Z),
Temperature(source=lan2rf, location=room1, value=20.0, type=SETPOINT, time=2025-11-14T19:29:39.979889657Z),
Temperature(source=lan2rf, location=room1, value=0.0, type=SETPOINT_OVERRIDE, time=2025-11-14T19:29:39.979889657Z),
Temperature(source=lan2rf, location=room2, value=327.67, type=SETPOINT, time=2025-11-14T19:29:39.979889657Z),
Temperature(source=lan2rf, location=room2, value=0.0, type=SETPOINT_OVERRIDE, time=2025-11-14T19:29:39.979889657Z),
Pressure(source=lan2rf, subject=central_heating, value=1.25, time=2025-11-14T19:29:39.979889657Z),
TextStatus(source=lan2rf, subject=STATUS_DISPLAY_CODE, value=Standby, time=2025-11-14T19:29:39.979889657Z),
OperationalStatus(source=lan2rf, subject=LOCKED_OUT, active=false, time=2025-11-14T19:29:39.979889657Z),
OperationalStatus(source=lan2rf, subject=PUMP_ACTIVE, active=false, time=2025-11-14T19:29:39.979889657Z),
OperationalStatus(source=lan2rf, subject=TAP_FUNCTION_ACTIVE, active=false, time=2025-11-14T19:29:39.979889657Z),
OperationalStatus(source=lan2rf, subject=BURNER_ACTIVE, active=false, time=2025-11-14T19:29:39.979889657Z)]
```

_Note:_ The above log entry excerpt has had carriage returns added to aid readability. The log entry is written on a single line in the app.