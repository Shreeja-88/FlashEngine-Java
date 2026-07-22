import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

public class WebServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/sale", new FlashSaleApiHandler());
        server.createContext("/", new DashboardUiHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        System.out.println("\nFlashEngine Web Dashboard running at: http://localhost:8080/\n");
        server.start();
    }

    static class DashboardUiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = getDashboardHTML();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html.getBytes());
            }
        }
    }

    static class FlashSaleApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int totalItems = 10;
            int totalConcurrentUsers = 100;
            double itemPrice = 499.99;

            Product phone = new Product("P100", "Flagship Smartphone");
            FlashSaleService saleService = new FlashSaleService(phone, totalItems);
            RateLimiter rateLimiter = new RateLimiter(3); // Max 3 requests/sec per user

            ExecutorService executor = Executors.newFixedThreadPool(totalConcurrentUsers);
            CountDownLatch readyLatch = new CountDownLatch(totalConcurrentUsers);
            CountDownLatch startLatch = new CountDownLatch(1);

            List<PurchaseResult> results = new CopyOnWriteArrayList<>();

            for (int i = 1; i <= totalConcurrentUsers; i++) {
                // Simulate some duplicate user IDs to test rate limiter & duplicate prevention
                final String userId = "User_" + (i % 80 == 0 ? 1 : i);

                executor.submit(() -> {
                    readyLatch.countDown();
                    long startTime = 0;
                    try {
                        startLatch.await();

                        startTime = System.nanoTime();

                        // 1. Rate Limiter Check
                        if (!rateLimiter.allowRequest(userId)) {
                            double latency = (System.nanoTime() - startTime) / 1_000_000.0;
                            results.add(new PurchaseResult(userId, "RATE_LIMITED", latency, 0));
                            return;
                        }

                        // 2. Purchase Attempt
                        saleService.purchase(userId);
                        double latency = (System.nanoTime() - startTime) / 1_000_000.0;
                        results.add(new PurchaseResult(userId, "SUCCESS", latency, itemPrice));

                    } catch (OutOfStockException e) {
                        double latency = (System.nanoTime() - startTime) / 1_000_000.0;
                        results.add(new PurchaseResult(userId, "OUT_OF_STOCK", latency, 0));
                    } catch (DuplicatePurchaseException e) {
                        double latency = (System.nanoTime() - startTime) / 1_000_000.0;
                        results.add(new PurchaseResult(userId, "DUPLICATE", latency, 0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            try {
                readyLatch.await();
                startLatch.countDown(); // Start all threads at once
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Generate Analytics & Response
            String analyticsJson = SaleAnalytics.generateAnalyticsJson(results, totalConcurrentUsers);

            // Construct Full JSON
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"analytics\":").append(analyticsJson).append(", \"logs\":[");

            for (int i = 0; i < results.size(); i++) {
                PurchaseResult r = results.get(i);
                jsonBuilder.append(String.format(
                    "{\"userId\":\"%s\",\"status\":\"%s\",\"latency\":%.2f}",
                    r.getUserId(), r.getStatus(), r.getLatencyMs()
                ));
                if (i < results.size() - 1) jsonBuilder.append(",");
            }
            jsonBuilder.append("]}");

            String response = jsonBuilder.toString();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private static String getDashboardHTML() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>FlashEngine Dashboard</title>
                <style>
    body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    background: linear-gradient(135deg, #192231 0%, #494E6B 100%);
background-attachment: fixed;
    color: #F4F4F4;
    margin: 0;
    padding: 30px;
}

.container {
    max-width: 1000px;
    margin: 0 auto;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

h1 {
    margin: 0;
    color: #985E6D;
    font-size: 30px;
    font-weight: 700;
}

button {
    background: #985E6D;
    color: white;
    border: none;
    padding: 12px 24px;
    border-radius: 10px;
    font-weight: 600;
    cursor: pointer;
    transition: .25s;
}

button:hover {
    background: #7D4A58;
}

.grid {
    display: grid;
    grid-template-columns: repeat(4,1fr);
    gap:16px;
    margin-bottom:24px;
}

.card {
    background:#2A3044;
    border:1px solid #494E6B;
    border-radius:12px;
    padding:18px;
}

.card h3{
    color:#98878F;
    margin:0 0 10px;
    font-size:12px;
    letter-spacing:1px;
    text-transform:uppercase;
}

.card .value{
    color:#F4F4F4;
    font-size:24px;
    font-weight:700;
}

.progress-container{
    background:#2A3044;
    border:1px solid #494E6B;
    border-radius:12px;
    padding:20px;
    margin-bottom:24px;
}

.progress-bar-bg{
    background:#494E6B;
    height:16px;
    border-radius:8px;
    overflow:hidden;
    margin-top:10px;
}

.progress-bar-fill{
    background:#985E6D;
    height:100%;
    transition:.4s;
}

.feed{
    background:#2A3044;
    border:1px solid #494E6B;
    border-radius:12px;
    padding:20px;
    height:300px;
    overflow-y:auto;
    font-family:monospace;
}

.SUCCESS{
    background:#355C4A;
    color:#8FE3B0;
}

.OUT_OF_STOCK{
    background:#6A2F3C;
    color:#FFC1C1;
}

.RATE_LIMITED{
    background:#6B5B2A;
    color:#FFE78C;
}

.DUPLICATE{
    background:#494E6B;
    color:#D0D4E4;
}

.log-item{
    padding:8px 0;
    border-bottom:1px solid #3A4258;
    display:flex;
    justify-content:space-between;
}
</style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>FlashEngine Concurrency Dashboard</h1>
                        <button onclick="runSimulation()">Trigger Flash Sale (100 Threads)</button>
                    </div>

                    <div class="progress-container">
                        <div style="display: flex; justify-content: space-between;">
                            <span>Inventory Allocation</span>
                            <span id="stockText">10 / 10 Items Remaining</span>
                        </div>
                        <div class="progress-bar-bg">
                            <div class="progress-bar-fill" id="progressBar"></div>
                        </div>
                    </div>

                    <div class="grid">
                        <div class="card"><h3>Total Revenue</h3><div class="value" id="revenue">$0.00</div></div>
                        <div class="card"><h3>Success Rate</h3><div class="value" id="successRate">0%</div></div>
                        <div class="card"><h3>Avg Latency</h3><div class="value" id="avgLatency">0.00 ms</div></div>
                        <div class="card"><h3>Rate Limited</h3><div class="value" id="rateLimited">0</div></div>
                    </div>

                    <div class="feed" id="feed">
                        <div style="color: #64748b; text-align: center; margin-top: 100px;">Click 'Trigger Flash Sale' to simulate concurrent traffic...</div>
                    </div>
                </div>

                <script>
                    async function runSimulation() {
                        const feed = document.getElementById('feed');
                        feed.innerHTML = '<div style="color: #38bdf8;">Simulating 100 concurrent threads hitting FlashSaleService...</div>';

                        const res = await fetch('/api/sale');
                        const data = await res.json();
                        const a = data.analytics;

                        // Update Metrics
                        document.getElementById('revenue').innerText = '$' + a.totalRevenue.toFixed(2);
                        document.getElementById('successRate').innerText = ((a.successfulOrders / a.totalAttempts) * 100).toFixed(0) + '%';
                        document.getElementById('avgLatency').innerText = a.avgLatencyMs.toFixed(2) + ' ms';
                        document.getElementById('rateLimited').innerText = a.rateLimitedCount;

                        // Update Stock Bar
                        const remaining = 10 - a.successfulOrders;
                        document.getElementById('stockText').innerText = remaining + ' / 10 Items Remaining';
                        document.getElementById('progressBar').style.width = (remaining / 10 * 100) + '%';

                        // Render Live Feed
                        feed.innerHTML = '';
                        data.logs.forEach(log => {
                            const item = document.createElement('div');
                            item.className = 'log-item';
                            item.innerHTML = `
                                <span><span class="badge ${log.status}">${log.status}</span> ${log.userId}</span>
                                <span style="color: #64748b;">${log.latency.toFixed(2)} ms</span>
                            `;
                            feed.appendChild(item);
                        });
                    }
                </script>
            </body>
            </html>
        """;
    }
}