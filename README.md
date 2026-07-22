# FlashEngine: High-Concurrency Flash Sale Processing System

## Executive Summary

**FlashEngine** is a zero-dependency, lightweight, high-throughput in-memory flash sale processing engine built using **Core Java (JDK 17)**. The system simulates high-concurrency e-commerce events where hundreds of concurrent threads compete for limited stock. By utilizing non-blocking atomic operations, real-time rate limiting, and an embedded custom web server, FlashEngine guarantees zero inventory overselling and sub-millisecond transaction processing.

---
## Live High-Concurrency Demo

![FlashEngine Live Simulation](assets/flashengine-demo.gif)

---

## System Architecture & Design Principles

```
                       [ 100 Concurrent Threads ]
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       Token Bucket Rate Limiter                     │
│               (Caps traffic to 3 req/sec per user)                   │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Lock-Free Stock Allocator                        │
│             (AtomicInteger CAS Operations - No Deadlocks)           │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   Concurrent Duplicate Guard                        │
│           (ConcurrentHashMap KeySet - Prevents Double Orders)       │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   Java Stream Analytics Engine                      │
│        (Calculates Min/Max/Avg Latency & Revenue via System.nanoTime)│
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                 Embedded HTTP Server & Visual Dashboard             │
│            (com.sun.net.httpserver.HttpServer / Custom UI)          │
└─────────────────────────────────────────────────────────────────────┘

```

---

## Key Engineering Modules

### 1. Lock-Free Inventory Management (CAS)

* **Problem:** Traditional synchronized blocks or relational database locks create execution bottlenecks and thread starvation under heavy load.
* **Solution:** FlashEngine leverages **Compare-And-Swap (CAS)** primitives provided by `java.util.concurrent.atomic.AtomicInteger`. Stock deduction happens in a non-blocking loop, guaranteeing **atomic safety** and **0% overselling** without locking threads.

### 2. Token Bucket Rate Limiter

* **Problem:** Automated bots and user spam can overload system resources and block legitimate buyers.
* **Solution:** A custom, thread-safe `RateLimiter` class tracks request frequency using `ConcurrentHashMap`. Requests exceeding **3 requests per second per user** are immediately rejected with a `RATE_LIMITED` status.

### 3. Duplicate Transaction Prevention

* **Problem:** Network retries or rapid double-clicking can cause duplicate purchases for a single account.
* **Solution:** Utilizes `ConcurrentHashMap.newKeySet()` to store processed `userId`s atomically. Subsequent attempts by the same ID within the sale window throw a custom `DuplicatePurchaseException`.

### 4. Nanosecond Metric Pipeline & Stream Analytics

* **Problem:** High-performance systems require fine-grained visibility into system latency and financial metrics.
* **Solution:** Each transaction captures execution durations using `System.nanoTime()`. Upon sale completion, the **Java Stream API** (`DoubleSummaryStatistics`) aggregates:
* Minimum, Maximum, and Average Execution Latency (ms)
* Total Revenue Generated
* Distribution of Success vs. Failure Badges



### 5. Native HTTP Server & Visual Web Dashboard

* **Problem:** Relying on external web frameworks (e.g., Spring Boot) introduces heavy dependencies and startup overhead.
* **Solution:** Built using JDK’s `com.sun.net.httpserver.HttpServer`. It serves RESTful API responses (`/api/sale`), static SVG assets (`/flashengine.svg`), and an interactive dark-themed web dashboard with live inventory progress bars and real-time transaction feeds.

---

## Technical Specifications & Tech Stack

| Component | Technology / Implementation |
| --- | --- |
| **Language** | Java 17 (Core JDK Standard Library) |
| **Concurrency Core** | `AtomicInteger`, `ConcurrentHashMap`, `ExecutorService`, `CountDownLatch` |
| **Data Aggregation** | Java Stream API (`summaryStatistics`, `mapToDouble`) |
| **Networking & HTTP** | `com.sun.net.httpserver.HttpServer` |
| **Frontend UI** | HTML5, CSS3 (Custom Twilight Palette), Vanilla JavaScript (`Fetch API`) |
| **Dependencies** | **0 External Libraries / Frameworks** |

---

## Performance & Test Simulation Parameters

* **Total Concurrent Threads:** `100`
* **Total Available Stock:** `10 items`
* **Item Unit Price:** `$499.99`
* **Rate Limit Ceiling:** `3 requests / second / user`
* **Execution Hardware:** Multithreaded JVM Environment

### Sample System Execution Analytics Output

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

## How to Run the Project

1. **Clone & Navigate to the Directory:**
```bash
cd FlashEngine

```

2. **Compile All Java Files:**
```bash
javac *.FlashEngine-Java

```

3. **Start the Web Server:**
```bash
java WebServer

```

4. **Access the Dashboard:**
Open your browser and navigate to `http://localhost:8080/` to launch the interactive dashboard and trigger the multithreaded simulation.

---