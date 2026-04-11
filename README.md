# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W Client-Server Architectures  
**Author:** Chamethya Yasodie  
**GitHub:** [chamethyaY/smart-campus-api](https://github.com/chamethyaY/smart-campus-api)

---

## Overview

This is a RESTful API built using JAX-RS (Jersey) for the University of Westminster's Smart Campus initiative. The API manages Rooms and Sensors across the campus, providing endpoints to create, retrieve, update, and delete rooms and sensors, as well as logging sensor readings. The system is built using an embedded Grizzly HTTP server and uses in-memory data structures (HashMap, ArrayList) for data storage.

### Key Features

- Room management with sensor safety checks
- Sensor management with room validation
- Historical sensor readings with real-time value updates
- Comprehensive error handling with custom exception mappers
- Request and response logging via JAX-RS filters
- HATEOAS-compliant discovery endpoint

---

## Technology Stack

- Java 17
- JAX-RS (Jersey 2.41)
- Grizzly HTTP Server
- Jackson (JSON)
- Maven

---

## Project Structure

    src/main/java/com/smartcampus/
    ├── Main.java
    ├── SmartCampusApp.java
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    ├── store/
    │   └── DataStore.java
    ├── resource/
    │   ├── DiscoveryResource.java
    │   ├── RoomResource.java
    │   ├── SensorResource.java
    │   └── SensorReadingResource.java
    ├── exception/mappers/
    │   ├── RoomNotEmptyException.java
    │   ├── RoomNotEmptyExceptionMapper.java
    │   ├── LinkedResourceNotFoundException.java
    │   ├── LinkedResourceNotFoundExceptionMapper.java
    │   ├── SensorUnavailableException.java
    │   ├── SensorUnavailableExceptionMapper.java
    │   └── GlobalExceptionMapper.java
    └── filter/
        └── LoggingFilter.java

---

## How to Build and Run

### Prerequisites

- Java 17 installed
- Maven installed
- Git installed

### Step 1 - Clone the repository

    git clone https://github.com/chamethyaY/smart-campus-api.git
    cd smart-campus-api

### Step 2 - Build the project

    mvn clean install

### Step 3 - Run the server

    java -jar target/smart-campus-api-1.0-SNAPSHOT.jar

### Step 4 - Access the API

The server will start on port 8082. Access it at:

    http://localhost:8082/api/v1

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1 | Discovery endpoint |
| GET | /api/v1/rooms | Get all rooms |
| POST | /api/v1/rooms | Create a new room |
| GET | /api/v1/rooms/{roomId} | Get a room by ID |
| DELETE | /api/v1/rooms/{roomId} | Delete a room |
| GET | /api/v1/sensors | Get all sensors |
| POST | /api/v1/sensors | Create a new sensor |
| GET | /api/v1/sensors/{sensorId} | Get a sensor by ID |
| GET | /api/v1/sensors?type=CO2 | Filter sensors by type |
| GET | /api/v1/sensors/{sensorId}/readings | Get all readings |
| POST | /api/v1/sensors/{sensorId}/readings | Add a new reading |

---

## Sample curl Commands

### 1. Discovery Endpoint

    curl -X GET http://localhost:8082/api/v1

### 2. Create a Room

    curl -X POST http://localhost:8082/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":50}"

### 3. Get All Rooms

    curl -X GET http://localhost:8082/api/v1/rooms

### 4. Create a Sensor

    curl -X POST http://localhost:8082/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":22.5,\"roomId\":\"LIB-301\"}"

### 5. Get All Sensors

    curl -X GET http://localhost:8082/api/v1/sensors

### 6. Filter Sensors by Type

    curl -X GET "http://localhost:8082/api/v1/sensors?type=Temperature"

### 7. Add a Sensor Reading

    curl -X POST http://localhost:8082/api/v1/sensors/TEMP-001/readings -H "Content-Type: application/json" -d "{\"value\":25.5}"

### 8. Get Sensor Readings

    curl -X GET http://localhost:8082/api/v1/sensors/TEMP-001/readings

### 9. Delete a Room with no sensors

    curl -X DELETE http://localhost:8082/api/v1/rooms/LIB-301

### 10. Try to Delete Room with Sensors - returns 409 error

    curl -X DELETE http://localhost:8082/api/v1/rooms/LIB-301

---

## Report - Question Answers

### Part 1.1 - JAX-RS Resource Lifecycle

By default, JAX-RS creates a new instance of a resource class for every incoming HTTP request. This is known as the per-request lifecycle. Each request gets its own fresh object, which means instance variables are not shared between requests.

This design decision has a significant impact on how in-memory data must be managed. Since each resource instance is created fresh, you cannot store shared data like rooms or sensors as instance variables inside the resource class. Instead, a shared singleton data store must be used. In this project, DataStore.getInstance() returns a single shared instance backed by ConcurrentHashMap. This ensures that all requests read from and write to the same data, preventing data loss between requests. The use of ConcurrentHashMap also prevents race conditions when multiple requests access or modify data simultaneously, as it provides thread-safe operations without requiring explicit synchronization.

### Part 1.2 - What is HATEOAS and Why is it Useful?

HATEOAS (Hypermedia As The Engine Of Application State) is a constraint of REST architecture where API responses include links to related resources and available actions. Instead of clients needing to know all URLs in advance, the server guides them dynamically through responses.

For example, a response to GET /api/v1 in this API includes links to /api/v1/rooms and /api/v1/sensors, telling clients where to navigate next. This benefits client developers because they do not need to hardcode URLs or constantly refer to static documentation. If the server changes a URL, clients that follow links automatically adapt. It makes APIs more self-discoverable, reduces tight coupling between client and server, and allows the API to evolve without breaking existing clients.

### Part 2.1 - Full Room Objects vs Just IDs

Returning full room objects in a list provides all information in a single request, reducing the number of API calls a client needs to make. However, it increases response size and network bandwidth usage, especially when there are thousands of rooms. Clients receive data they may not need.

Returning only IDs reduces the response size and is faster to transmit, but forces the client to make additional requests to fetch details for each room individually. This increases client-side processing and the number of HTTP round trips. In this implementation, full room objects are returned to provide a better developer experience and reduce the number of API calls needed.

### Part 2.2 - Is DELETE Idempotent?

Yes, the DELETE operation is idempotent in this implementation. Idempotency means that making the same request multiple times produces the same result as making it once.

In this API, if a client sends DELETE /api/v1/rooms/LIB-301 and the room exists and has no sensors, it will be deleted and a 200 OK response is returned. If the same request is sent again, the room no longer exists, and the API returns a 404 Not Found response. The server state remains the same — the room is still gone. Therefore, the outcome is consistent regardless of how many times the request is made, confirming idempotency.

### Part 3.1 - @Consumes(APPLICATION_JSON) Mismatch

When @Consumes(MediaType.APPLICATION_JSON) is declared on a POST method, JAX-RS expects the request body to have a Content-Type: application/json header. If a client sends data with a different format such as text/plain or application/xml, JAX-RS cannot find a matching method that consumes that media type.

In this case, JAX-RS automatically returns an HTTP 415 Unsupported Media Type response without even invoking the resource method. This protects the API from receiving malformed or unexpected data formats and ensures that only properly formatted JSON is processed.

### Part 3.2 - @QueryParam vs Path-Based Filtering

Using @QueryParam for filtering such as GET /api/v1/sensors?type=CO2 is considered superior to path-based filtering such as /api/v1/sensors/type/CO2 for several reasons.

Query parameters are semantically meant for optional filtering, searching, and sorting of collections. They do not change the identity of the resource. The path /api/v1/sensors always refers to the sensors collection, and the type parameter simply narrows the results. Path parameters on the other hand are meant to identify a specific resource such as /api/v1/sensors/TEMP-001.

Using path-based filtering creates ambiguity. Is CO2 a sensor ID or a type? Query parameters are also easier to combine such as ?type=CO2&status=ACTIVE and are optional by nature, making the API more flexible and intuitive.

### Part 4.1 - Sub-Resource Locator Pattern Benefits

The Sub-Resource Locator pattern allows a resource class to delegate handling of a sub-path to a separate dedicated class. In this API, SensorResource delegates /sensors/{sensorId}/readings to SensorReadingResource.

This approach provides several architectural benefits. First, it improves separation of concerns — each class handles one specific resource, making the code easier to understand and maintain. Second, it reduces complexity — instead of one massive controller class with dozens of methods handling every possible nested path, logic is distributed across focused classes. Third, it improves testability — each resource class can be tested independently. In large APIs with deep nesting, this pattern is essential for keeping the codebase manageable and scalable.

### Part 5.2 - HTTP 422 vs 404 for Missing Referenced Resource

HTTP 404 Not Found means the requested URL or resource does not exist on the server. HTTP 422 Unprocessable Entity means the server understands the request and the URL is valid, but cannot process it because the content of the request is semantically incorrect.

When a client POSTs a new sensor with a roomId that does not exist, the URL /api/v1/sensors is perfectly valid — the problem is inside the JSON payload. The referenced room ID does not exist. Using 404 here would be misleading because it suggests the endpoint itself was not found. HTTP 422 is more semantically accurate because it communicates that the request was well-formed and the endpoint was found, but the data inside the request references a non-existent resource, making it impossible to process.

### Part 5.4 - Security Risks of Exposing Stack Traces

Exposing Java stack traces to external API consumers presents serious cybersecurity risks. Stack traces reveal internal implementation details such as class names, method names, file paths, and line numbers. An attacker can use this information to understand the application's internal structure, identify which frameworks and libraries are being used and their versions, and look up known vulnerabilities for those specific versions.

Stack traces can also reveal database query structures, internal business logic, and configuration details. This information significantly reduces the effort required for targeted attacks such as SQL injection, remote code execution, or privilege escalation. By implementing a global exception mapper that returns only a generic 500 error message, the API prevents leaking any internal details to potential attackers.

### Part 5.5 - Why Use JAX-RS Filters for Logging

Using JAX-RS filters for cross-cutting concerns like logging is far superior to manually inserting Logger.info() statements inside every resource method for several reasons.

First, filters follow the DRY (Don't Repeat Yourself) principle — logging logic is written once in the filter and automatically applied to every request and response without modifying any resource class. Second, it improves maintainability — if the logging format needs to change, only the filter needs to be updated, not dozens of resource methods. Third, it ensures consistency — every request is logged uniformly without the risk of a developer forgetting to add a log statement to a new method. Fourth, it promotes separation of concerns — resource methods focus purely on business logic while the filter handles the technical concern of observability.

---

## Error Handling

| Exception | HTTP Status | Scenario |
|-----------|-------------|----------|
| RoomNotEmptyException | 409 Conflict | Deleting a room that has sensors |
| LinkedResourceNotFoundException | 422 Unprocessable Entity | Creating a sensor with invalid roomId |
| SensorUnavailableException | 403 Forbidden | Adding reading to MAINTENANCE sensor |
| GlobalExceptionMapper | 500 Internal Server Error | Any unexpected runtime error |

---

## License

This project was developed as coursework for 5COSC022W at the University of Westminster.
