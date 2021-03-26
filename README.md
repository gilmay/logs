# Logs

**Logs** is a simple GUI application written in Kotlin to visualize application logs.

Its main purpose was to experiment with Kotlin and the [TornadoFX](https://tornadofx.io/) and to provide a GUI
for [log-explorer](https://gitlab.com/tinubu-public/log-explorer).

## Build

Dependencies unavailable in public repositories (i.e. `log-explorer`) must be installed in your local Maven repository
first.

Minimum Java version is `11`

The project is built with a *Gradle* wrapper:

```shell
./gradlew build
```

To generate a single executable *-all.jar* file with all dependencies included:

```shell
./gradlew shadowJar
```

When this command succeeds the jar file will be found in `logs-app/build/lib`.

## Run

The *Main Class* of application is `gma.logs.app.LogsAppKt` located in the `logs-app` module.

The application can be launched from an IDE, with the executable Jar file or with the Gradle wrapper:

```shell
./gradlew run
```

## Project structure

### Main modules

* `logs-core` core Kotlin module
* `logs-app` application module used to launch the application

### Adapters modules

Adapters are plug-in modules which provide features to open and parse logs source (through SPI implementations):

* `logs-adapaters/file-pattern` for log files using a regex pattern to extract information
* `logs-adapaters/log-explorer` using [log-explorer](https://gitlab.com/tinubu-public/log-explorer profiles

## Configurations example

### Files

Load files on file system and parse them using a regex pattern to extract log entries.

Example:

- *Base directory*: `/var/logs/my-logs`
- *Files pattern*: `**/*.log`
- *Extractor regex*: `(?<timestamp>[ \d-:]+) \[(?<level>\w+)\] (?<message>.+)\R`
- *Timestamp format*: `yyyy-MM-dd HH:mm:ssXX`
- *File charset*: `ISO-8859-1`

### Log Explorer

Specify a *log-explorer* configuration file and profiles to use.

Example, local Elasticsearch backend with Logstash log entries:

- *Configuration file*: `etc/example.yml`
- *Profiles*: `local-elasticsearch`

#### example.yml

```yaml
profiles:

  local-elasticsearch:
    backend: elasticsearch-v7
    parameters:
      elasticsearch.backend.sync: true
      elasticsearch.backend.verify-ssl: false
      elasticsearch.backend.index: logstash-*
    backendUris:
      - "http://localhost:9200"
```
