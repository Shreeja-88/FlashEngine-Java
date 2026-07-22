# FlashEngine

## High-Concurrency Flash Sale Processing System

FlashEngine is a lightweight, high-throughput flash sale processing engine built using Core Java. It simulates real-world e-commerce flash sale events where multiple concurrent users compete for limited inventory. The system leverages Java concurrency primitives to ensure thread safety, prevent overselling, eliminate duplicate purchases, and provide real-time analytics through an embedded HTTP server.

---

## Features

- Lock-free inventory management using `AtomicInteger`
- Supports concurrent purchase processing
- Duplicate purchase prevention
- Token bucket rate limiting
- Real-time transaction analytics
- Embedded HTTP server with interactive dashboard
- Zero external dependencies

---

## Demo

![FlashEngine Demo](assets/flashengine-demo.gif)

---

## System Architecture

```text
                    Concurrent Client Requests
                              │
                              ▼
                  Token Bucket Rate Limiter
                              │
                              ▼
               Lock-Free Inventory Allocation
             (AtomicInteger Compare-And-Swap)
                              │
                              ▼
               Duplicate Purchase Prevention
                (ConcurrentHashMap Key Set)
                              │
                              ▼
                 Analytics & Metrics Engine
             (Java Streams + System.nanoTime)
                              │
                              ▼
                 Embedded HTTP Server Dashboard
```

---

## Technologies Used

| Component | Technology |
|-----------|------------|
| Language | Java 17+ |
| Concurrency | AtomicInteger, ConcurrentHashMap, ExecutorService, CountDownLatch |
| Analytics | Java Stream API |
| Networking | com.sun.net.httpserver.HttpServer |
| Frontend | HTML, CSS, JavaScript |
| Dependencies | None |

---

## Project Structure

```text
FlashEngine-Java/
│
├── Main.java
├── WebServer.java
├── FlashSaleService.java
├── Product.java
├── PurchaseRecord.java
├── PurchaseResult.java
├── RateLimiter.java
├── SaleAnalytics.java
├── DuplicatePurchaseException.java
├── OutOfStockException.java
├── RateLimitExceededException.java
├── README.md
├── LICENSE
└── assets/
    └── flashengine-demo.gif
```

---

## Core Components

### Lock-Free Inventory Management

Inventory updates are performed using `AtomicInteger` and Compare-And-Swap (CAS) operations, ensuring thread-safe stock allocation without explicit locks.

### Rate Limiting

A token bucket rate limiter restricts each user to a fixed number of requests per second, protecting the system from excessive traffic.

### Duplicate Purchase Prevention

A thread-safe `ConcurrentHashMap.newKeySet()` is used to ensure that each user can complete only one successful purchase during a flash sale.

### Analytics Engine

Execution metrics are collected using `System.nanoTime()` and aggregated using the Java Stream API to calculate:

- Total successful purchases
- Failed transactions
- Average latency
- Minimum latency
- Maximum latency
- Total revenue

### Embedded HTTP Server

The application includes a lightweight web server built using `com.sun.net.httpserver.HttpServer` that serves an interactive dashboard and REST endpoints.

---

## Sample Analytics Output

```json
{
  "analytics": {
    "totalAttempts": 100,
    "successfulOrders": 10,
    "outOfStockCount": 85,
    "rateLimitedCount": 5,
    "totalRevenue": 4999.90,
    "avgLatencyMs": 0.412,
    "minLatencyMs": 0.085,
    "maxLatencyMs": 1.840
  }
}
```

---

## Performance Configuration

| Parameter | Value |
|-----------|------:|
| Concurrent Threads | 100 |
| Initial Stock | 10 Items |
| Product Price | $499.99 |
| Rate Limit | 3 Requests/Second/User |
| Inventory Overselling | Prevented |

---

## How to Run

### Prerequisites

- Java 17 or later
- Git

### Clone the Repository

```bash
git clone https://github.com/Shreeja-88/FlashEngine-Java.git
cd FlashEngine-Java
```

### Compile

```bash
javac *.java
```

### Run the Web Server

```bash
java WebServer
```

or run the simulation directly

```bash
java Main
```

### Open the Dashboard

```
http://localhost:8080
```

---

## Java Concepts Demonstrated

- Multithreading
- ExecutorService
- Atomic Operations
- Compare-And-Swap (CAS)
- ConcurrentHashMap
- CountDownLatch
- Java Stream API
- Custom Exceptions
- Embedded HTTP Server
- RESTful API Design
- Thread-Safe Programming

---

