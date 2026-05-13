# Flight Intelligence Platform

> Every time you change a config file, you must `git commit` in config-repo.
> Config Server reads from git history, not the working directory.

---

### 2. Set your OpenSky credentials (optional but recommended)
Without credentials you get anonymous access: rate-limited but functional for testing.

```bash
export OPENSKY_CLIENT_ID=your_client_id
export OPENSKY_CLIENT_SECRET=your_client_secret

set for Windows
```

---

### 3. Start infrastructure

**dev.ps1 usage(only for Windows):**
- .\dev.ps1                        -> start all services
- .\dev.ps1 -Skip ingestion-service -> start all except one
- .\dev.ps1 -Skip ingestion-service,flight-tracker-service -> skip multiple
- .\dev.ps1 -TestOnly              -> run all tests, no services started
- .\dev.ps1 -Build                 -> mvn clean install only

```bash
cd docker
docker-compose up -d
```

Wait ~30 seconds for Kafka and MongoDB to be healthy:
```bash
docker-compose ps   # all should show "healthy"
```

Useful UIs once running:

| UI              | URL                        | Credentials |
|-----------------|----------------------------|-------------|
| Kafka UI        | http://localhost:8090      | none        |
| Mongo Express   | http://localhost:8091      | none        |
| RabbitMQ UI     | http://localhost:15672     | guest/guest |
| Eureka Dashboard| http://localhost:8761      | none        |
| PostgreSQL Admin| http://localhost:5050      | none        |

---

### 4. Start services IN ORDER

Open a terminal tab for each. Wait for each to print "Started ... in X seconds" before moving to the next.

**Tab 0 build root project install shared dependencies**
```
# From flight-platform/ root
mvn clean install -DskipTests

# Or just install common so services can depend on it
mvn install -pl common
```

**Tab 1 — Config Server (port 8888)**
```bash
cd config-server
mvn spring-boot:run
```
Verify: `curl http://localhost:8888/ingestion-service/default`
You should see your ingestion-service.yml config returned as JSON.

**Tab 2 — Discovery Server / Eureka (port 8761)**
```bash
cd discovery-server
mvn spring-boot:run
```
Verify: Open http://localhost:8761 — Eureka dashboard loads.

**Tab 3 — Flight Tracker Service (port 8081)**
```bash
cd flight-tracker-service
mvn spring-boot:run
```
Verify: `curl http://localhost:8081/api/flights/stats`
Returns `{"totalTracked":0,...}` — no flights yet, that's expected.

**Tab 4 — Ingestion Service (port 8084)**
```bash
cd ingestion-service
mvn spring-boot:run
```

---

## Verifying data flows end-to-end

### After first poll — check Kafka
Open Kafka UI: http://localhost:8090
- Topics → `flight-positions` → Messages tab
- You should see JSON messages appearing every 30 seconds

### After a minute — check MongoDB
Open Mongo Express: http://localhost:8091
- Database: `flightintel` → Collection: `flights`
- Documents should be appearing and updating

Or via curl:
```bash
# How many flights are we tracking?
curl http://localhost:8081/api/flights/stats

# Get all currently active flights
curl http://localhost:8081/api/flights/active

# Get all airborne flights
curl http://localhost:8081/api/flights/airborne

# Look up a specific flight (grab an icao24 from the stats output)
curl http://localhost:8081/api/flights/3c4b26
```

### Check Eureka registrations
Open http://localhost:8761
Both `INGESTION-SERVICE` and `FLIGHT-TRACKER-SERVICE` should appear as registered.

---

## Build commands

```bash
# Build everything from root (run this after pulling changes)
mvn clean install -DskipTests

# Build only common (required before building any service)
mvn install -pl common

# Build one service and everything it depends on
mvn clean install -pl ingestion-service -am -DskipTests

# Run a specific service from root (no need to cd)
mvn spring-boot:run -pl ingestion-service -am

# Check what version of a dependency is being used
mvn dependency:tree -pl ingestion-service | grep kafka
```

---

## Project structure

```
flight-intel/
├── pom.xml                      ← parent pom, version management
├── common/                      ← shared events, no Spring Boot main class
├── config-server/               ← reads config-repo/, serves config to all services
├── discovery-server/            ← Eureka service registry
├── ingestion-service/           ← polls OpenSky, publishes to Kafka
├── flight-tracker-service/      ← consumes Kafka, writes to MongoDB
├── docker/
│   └── docker-compose.yml       ← Kafka, MongoDB, RabbitMQ
└── config-repo/                 ← git repo with all service config files
    ├── application.yml          ← shared by ALL services
    ├── ingestion-service.yml    ← overrides for ingestion-service
    └── flight-tracker-service.yml
```
## Port map

| Service / Tool           | Port  | Status                 |
|--------------------------|-------|------------------------|
| Config Server            | 8888  | ✅ Running              |
| Eureka (Discovery)       | 8761  | ✅ Running              |
| GraphQL Gateway          | 8080  | ❌ Not created yet      |
| Flight Tracker Service   | 8081  | ✅ Running              |
| Weather Service          | 8085  |  ✅ Running        |
| Anomaly Detector Service | 8082  | ❌ Not created yet      |
| Alert Service            | 8083  | ❌ Not created yet      |
| Ingestion Service        | 8084  | ✅ Running              |
| Zookeeper                | 2181  | ✅ Docker               |
| Kafka                    | 9092  | ✅ Docker               |
| Kafka UI                 | 8090  | ✅ Docker               |
| MongoDB                  | 27017 | ✅ Docker               |
| Mongo Express            | 8091  | ✅ Docker               |
| PostgreSQL               | 5433  | ✅ Docker(host→5432)    |
| pgAdmin                  | 5050  | ✅ Docker               |
| Elasticsearch            | 9200  | ✅ Docker(ELK)          |
| Logstash                 | 5000  | ✅ Docker (TCP input)   |
| Logstash                 | 5044  | ✅ Docker (Beats input) |
| Kibana                   | 5601  | ✅  Docker (ELK)        |
| RabbitMQ                 | 5672  | ❌ Not started yet      |
| RabbitMQ UI              | 15672 | ❌ Not started yet      |


