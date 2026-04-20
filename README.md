# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W – Client-Server Architectures
**Author:** Chamethya Yasodie
**GitHub:** [chamethyaY/smart-campus-api](https://github.com/chamethyaY/smart-campus-api)

---

## Overview

This is a fully RESTful API built using **JAX-RS (Jersey 2.41)** for the University of Westminster's **Smart Campus** initiative. The API manages Rooms, Sensors, and Sensor Readings across the campus, providing endpoints to create, retrieve, update, and delete resources. The system runs on an embedded **Grizzly HTTP server** and uses **in-memory data structures** (`ConcurrentHashMap`, `ArrayList`) for all data storage — no database is used.

The API follows REST architectural principles including a versioned base path (`/api/v1`), proper HTTP status codes, meaningful JSON responses, resource nesting via sub-resource locators, and a HATEOAS-compliant discovery endpoint.

---

## Technology Stack

| Technology | Version |
|---|---|
| Java | 17 |
| JAX-RS (Jersey) | 2.41 |
| Grizzly HTTP Server | 2.41 |
| Jackson (JSON) | 2.15 |
| Maven | 3.x |

---

## Project Structure

```
src/main/java/com/smartcampus/
├── Main.java                          # Entry point, starts embedded Grizzly server
├── SmartCampusApp.java                # JAX-RS Application subclass (@ApplicationPath)
├── model/
│   ├── Room.java                      # Room POJO (id, name, capacity, sensorIds)
│   ├── Sensor.java                    # Sensor POJO (id, type, status, currentValue, roomId)
│   └── SensorReading.java             # SensorReading POJO (id, timestamp, value)
├── store/
│   └── DataStore.java                 # Singleton in-memory store (ConcurrentHashMap)
├── resource/
│   ├── DiscoveryResource.java         # GET /api/v1 – HATEOAS discovery endpoint
│   ├── RoomResource.java              # /api/v1/rooms – Room CRUD operations
│   ├── SensorResource.java            # /api/v1/sensors – Sensor operations + sub-resource locator
│   └── SensorReadingResource.java     # /api/v1/sensors/{sensorId}/readings – Reading history
├── exception/
│   ├── RoomNotEmptyException.java
│   ├── LinkedResourceNotFoundException.java
│   ├── SensorUnavailableException.java
│   └── mappers/
│       ├── RoomNotEmptyExceptionMapper.java          # 409 Conflict
│       ├── LinkedResourceNotFoundExceptionMapper.java # 422 Unprocessable Entity
│       ├── SensorUnavailableExceptionMapper.java      # 403 Forbidden
│       └── GlobalExceptionMapper.java                 # 500 Internal Server Error (catch-all)
└── filter/
    └── LoggingFilter.java             # ContainerRequestFilter + ContainerResponseFilter
```

---

## How to Build and Run

### Prerequisites

- Java 17 installed and `JAVA_HOME` configured
- Apache Maven 3.x installed
- Git installed

### Step 1 — Clone the Repository

```bash
git clone https://github.com/chamethyaY/smart-campus-api.git
cd smart-campus-api
```

### Step 2 — Build the Project

```bash
mvn clean install
```

This compiles the code, runs tests, and packages the application as a runnable JAR.

### Step 3 — Run the Server

```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

### Step 4 — Access the API

The server starts on **port 8082**. The base URL is:

```
http://localhost:8082/api/v1
```

---

## API Design Overview

The API is organised into a logical resource hierarchy that mirrors the physical structure of the Smart Campus:

```
/api/v1                              → Discovery / HATEOAS root
/api/v1/rooms                        → Room collection
/api/v1/rooms/{roomId}               → Individual room
/api/v1/sensors                      → Sensor collection
/api/v1/sensors?type={type}          → Filtered sensor collection
/api/v1/sensors/{sensorId}           → Individual sensor
/api/v1/sensors/{sensorId}/readings  → Reading sub-resource (history log)
```

Sensors are linked to Rooms via a `roomId` foreign key. Readings are nested under Sensors using the **Sub-Resource Locator** pattern. All responses use `application/json`.

---

## Full Endpoint Reference

| Method | Endpoint | Description | Success Code |
|--------|----------|-------------|--------------|
| GET | `/api/v1` | HATEOAS discovery endpoint | 200 |
| GET | `/api/v1/rooms` | Retrieve all rooms | 200 |
| POST | `/api/v1/rooms` | Create a new room | 201 |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room by ID | 200 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors assigned) | 200 |
| GET | `/api/v1/sensors` | Retrieve all sensors | 200 |
| GET | `/api/v1/sensors?type={type}` | Filter sensors by type | 200 |
| POST | `/api/v1/sensors` | Register a new sensor (validates roomId) | 201 |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor by ID | 200 |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get all readings for a sensor | 200 |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading (updates sensor currentValue) | 201 |

---

## Error Handling

The API uses custom Exception Mappers to ensure clean, structured JSON error responses are always returned. No raw Java stack traces are ever exposed.

| Exception | HTTP Status | Trigger Scenario |
|---|---|---|
| `RoomNotEmptyException` | 409 Conflict | Attempting to delete a room that still has sensors assigned to it |
| `LinkedResourceNotFoundException` | 422 Unprocessable Entity | Creating a sensor with a `roomId` that does not exist |
| `SensorUnavailableException` | 403 Forbidden | POSTing a reading to a sensor with status `MAINTENANCE` |
| `GlobalExceptionMapper` | 500 Internal Server Error | Any unexpected runtime exception (catch-all safety net) |

---

## Sample curl Commands

### 1. Discovery Endpoint
```bash
curl -X GET http://localhost:8082/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8082/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":50}"
```

### 3. Get All Rooms
```bash
curl -X GET http://localhost:8082/api/v1/rooms
```

### 4. Get a Specific Room
```bash
curl -X GET http://localhost:8082/api/v1/rooms/LIB-301
```

### 5. Create a Sensor (links to Room LIB-301)
```bash
curl -X POST http://localhost:8082/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":22.5,\"roomId\":\"LIB-301\"}"
```

### 6. Get All Sensors
```bash
curl -X GET http://localhost:8082/api/v1/sensors
```

### 7. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8082/api/v1/sensors?type=Temperature"
```

### 8. Add a Sensor Reading (also updates sensor's currentValue)
```bash
curl -X POST http://localhost:8082/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":25.5}"
```

### 9. Get All Readings for a Sensor
```bash
curl -X GET http://localhost:8082/api/v1/sensors/TEMP-001/readings
```

### 10. Delete a Room That Has No Sensors (success)
```bash
curl -X DELETE http://localhost:8082/api/v1/rooms/EMPTY-ROOM-001
```

### 11. Attempt to Delete a Room with Sensors (returns 409 Conflict)
```bash
curl -X DELETE http://localhost:8082/api/v1/rooms/LIB-301
```

### 12. Attempt to Create a Sensor with Invalid roomId (returns 422)
```bash
curl -X POST http://localhost:8082/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-999\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":400.0,\"roomId\":\"NONEXISTENT-ROOM\"}"
```

---

## Report — Answers to Coursework Questions

### Part 1.1 — JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance of a resource class for every incoming HTTP request**. This is known as the **per-request lifecycle**. Each request gets its own fresh object, which means instance variables are not shared between requests.

This design has a critical impact on in-memory data management. Since each resource instance is created fresh, shared application data such as rooms and sensors **cannot** be stored as instance variables inside resource classes. Instead, a **shared singleton data store** must be used. In this project, `DataStore.getInstance()` returns a single shared instance backed by `ConcurrentHashMap`. This ensures all requests read from and write to the same data, preventing data loss between requests. The use of `ConcurrentHashMap` also prevents race conditions when multiple requests concurrently access or modify data, as it provides thread-safe operations without requiring explicit synchronisation blocks.

---

### Part 1.2 — What is HATEOAS and Why is it Useful?

**HATEOAS** (Hypermedia As The Engine Of Application State) is a constraint of REST architecture where API responses include **hyperlinks to related resources and available actions**, rather than requiring clients to know all URLs in advance.

For example, the `GET /api/v1` discovery endpoint in this API returns links to `/api/v1/rooms` and `/api/v1/sensors`, telling clients exactly where to navigate next. This benefits client developers in several important ways. First, clients do not need to hardcode URLs or constantly consult static documentation — the server guides navigation dynamically. Second, if the server changes a URL structure, clients that follow hypermedia links adapt automatically without breaking. Third, it reduces tight coupling between client and server, allowing the API to evolve independently. HATEOAS transforms a REST API from a collection of static addresses into a self-describing, navigable system, which is considered the highest level of REST maturity (Richardson Maturity Model Level 3).

---

### Part 2.1 — Full Room Objects vs Returning Only IDs

Returning **full room objects** in a list provides all information in a single network round trip, reducing the total number of API calls a client needs to make. However, it increases response payload size and network bandwidth consumption, particularly when there are thousands of rooms. Clients receive fields they may not require.

Returning **only IDs** significantly reduces response size and transmission time, but forces the client to make additional `GET /rooms/{id}` requests for each room it needs details for. This increases client-side processing complexity and multiplies the number of HTTP round trips — a pattern known as the N+1 problem. In this implementation, **full room objects are returned** to provide a better developer experience, minimise round trips, and reduce client-side complexity, accepting the trade-off of a larger initial payload.

---

### Part 2.2 — Is DELETE Idempotent?

**Yes, the DELETE operation is idempotent** in this implementation. Idempotency means that making the same request multiple times produces the same final server state as making it once.

In this API, if a client sends `DELETE /api/v1/rooms/LIB-301` and the room exists with no sensors, it is deleted and a `200 OK` response is returned. If the exact same request is sent again, the room no longer exists, and the API returns a `404 Not Found` response. Crucially, the **server state** remains identical after both attempts — the room is absent in both cases. The HTTP response code differs between the first and second call, but this does not violate idempotency, which is defined in terms of server-side state change, not response code. Therefore, the outcome is consistent regardless of how many times the request is repeated, fully confirming idempotency as required by the HTTP specification.

---

### Part 3.1 — `@Consumes(APPLICATION_JSON)` Media Type Mismatch

When `@Consumes(MediaType.APPLICATION_JSON)` is declared on a POST method, JAX-RS expects the incoming request to carry a `Content-Type: application/json` header. If a client sends data with a different format such as `text/plain` or `application/xml`, JAX-RS attempts to find a resource method that declares it can consume that media type. Finding no match, the JAX-RS runtime **automatically returns an HTTP `415 Unsupported Media Type`** response without invoking the resource method at all.

This is handled entirely by the framework before any application code executes. It protects the API from receiving malformed or unexpected data formats and ensures that only properly structured JSON payloads are processed by business logic. The client receives a clear signal that they must correct their `Content-Type` header.

---

### Part 3.2 — `@QueryParam` vs Path-Based Filtering

Using `@QueryParam` for filtering (e.g. `GET /api/v1/sensors?type=CO2`) is considered architecturally superior to embedding the filter in the URL path (e.g. `/api/v1/sensors/type/CO2`) for several reasons.

**Semantic correctness:** Query parameters are designed for optional filtering, searching, and sorting of collections. They do not change the *identity* of the resource. The path `/api/v1/sensors` always refers to the sensors collection; the `type` parameter simply narrows the result set. Path segments, by contrast, are meant to identify a specific resource (e.g. `/api/v1/sensors/TEMP-001`).

**Avoiding ambiguity:** A path such as `/api/v1/sensors/CO2` is ambiguous — is `CO2` a sensor ID or a type category? This makes the API harder to reason about and maintain.

**Composability:** Query parameters are naturally combinable, for example `?type=CO2&status=ACTIVE`, making multi-criteria filtering trivial. Achieving the same with path parameters requires complex and unreadable URL structures.

**Optionality:** Query parameters are optional by nature — omitting `?type=` returns all sensors, which is the intuitive default behaviour. Path-based filtering has no clean equivalent for the "unfiltered" case.

---

### Part 4.1 — Sub-Resource Locator Pattern Benefits

The **Sub-Resource Locator** pattern allows a parent resource class to delegate handling of a nested sub-path to a dedicated child class at runtime. In this API, `SensorResource` declares a locator method for `{sensorId}/readings` that returns an instance of `SensorReadingResource`, which then handles all reading-related operations for that specific sensor.

This pattern provides significant architectural benefits. **Separation of concerns** is the primary advantage — each class is responsible for exactly one resource, making the code substantially easier to read, understand, and maintain. **Complexity management** is also improved: instead of one massive controller containing dozens of methods for every possible nested path, logic is cleanly distributed across focused, single-purpose classes. **Testability** improves as each resource class can be unit-tested in isolation. **Scalability** is also enhanced — in a large campus API with deep nesting (e.g. buildings → floors → rooms → sensors → readings), adding new sub-resources requires only creating a new class and adding a locator, rather than modifying a monolithic controller. This pattern is essential for keeping large JAX-RS APIs manageable and extensible over time.

---

### Part 5.2 — HTTP 422 vs 404 for Missing Referenced Resource

**HTTP 404 Not Found** signals that the requested **URL or endpoint** does not exist on the server. **HTTP 422 Unprocessable Entity** signals that the server fully understands the request and the endpoint is valid, but cannot process it because the **semantic content** of the request body is incorrect.

When a client POSTs a new sensor with a `roomId` that does not exist, the URL `/api/v1/sensors` is perfectly valid — the problem lies *inside* the JSON payload: the referenced room ID is absent from the system. Returning a `404` here would be misleading, implying the `/api/v1/sensors` endpoint itself could not be found. **HTTP 422 is semantically more accurate** because it communicates precisely: the request was received, the endpoint was found, the JSON was well-formed, but the data references a non-existent resource, making it semantically impossible to fulfil. This gives client developers actionable, precise feedback that the issue is a referential integrity problem within the request body, not a routing problem.

---

### Part 5.4 — Security Risks of Exposing Java Stack Traces

Exposing Java stack traces to external API consumers presents serious cybersecurity risks. Stack traces reveal **internal implementation details** including class names, method names, package structures, file paths, and line numbers. An attacker can exploit this information in several targeted ways.

**Framework and dependency fingerprinting:** Stack traces often expose which libraries and frameworks are in use, along with version identifiers. The attacker can then cross-reference these against public vulnerability databases (CVE, NVD) to identify known exploits for those exact versions.

**Application structure mapping:** Class names and package hierarchies reveal the internal architecture of the application, making it significantly easier to craft targeted attacks such as SQL injection, path traversal, or remote code execution.

**Business logic exposure:** Stack traces from unexpected exceptions can reveal the order of operations, conditional branches, and internal validation logic, helping attackers craft inputs that bypass security checks.

**Attack surface reduction:** By implementing a `GlobalExceptionMapper<Throwable>` that intercepts all unhandled exceptions and returns only a generic `HTTP 500 Internal Server Error` message with no internal detail, this API ensures that no sensitive implementation information is ever leaked to a potential attacker.

---

### Part 5.5 — Why Use JAX-RS Filters for Logging

Using JAX-RS filters for cross-cutting concerns such as logging is architecturally far superior to manually inserting `Logger.info()` statements inside every resource method, for several compelling reasons.

**DRY Principle (Don't Repeat Yourself):** The logging logic is written once in a single `LoggingFilter` class and is automatically applied to every incoming request and outgoing response without touching any resource class. There is no code duplication.

**Maintainability:** If the logging format, log level, or destination needs to change, only one file — the filter — needs to be updated. With manual logging, the same change must be applied to every method across every resource class, introducing a high risk of inconsistency.

**Consistency and completeness:** Every request is guaranteed to be logged uniformly. With manual logging, a developer adding a new endpoint might forget to include a log statement, creating silent gaps in observability.

**Separation of concerns:** Resource methods remain focused purely on business logic. The technical concern of observability — recording what happened, when, and with what result — is cleanly isolated in the filter layer.

**Interceptor-based design:** JAX-RS filters implement the interceptor pattern, a standard approach to handling cross-cutting concerns (logging, authentication, compression, CORS) at the framework level. This is a recognised industry best practice that scales cleanly as the API grows.

---

## License

This project was developed as coursework for **5COSC022W Client-Server Architectures** at the **University of Westminster**.
