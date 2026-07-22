
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class SaleAnalytics{

    public static void generateReport(List<PurchaseRecord> records, int totalAttempts) {
        System.out.println("\n==========================================");
        System.out.println("       POST-SALE ANALYTICS DASHBOARD      ");
        System.out.println("==========================================");

        // 1. Total Successful Orders
        long successfulOrders = records.stream().count();

        // 2. Stream Summaries: Total Revenue & Average Price
        DoubleSummaryStatistics stats = records.stream()
                .mapToDouble(PurchaseRecord::getPricePaid)
                .summaryStatistics();

        // 3. Success Rate Percentage
        double successRate = ((double) successfulOrders / totalAttempts) * 100;

        System.out.println("Total Simulated Attempts : " + totalAttempts);
        System.out.println("Successful Orders        : " + successfulOrders);
        System.out.println("Failed / Out of Stock    : " + (totalAttempts - successfulOrders));
        System.out.printf("System Conversion Rate   : %.2f%%\n", successRate);
        System.out.printf("Total Gross Revenue      : $%.2f\n", stats.getSum());
        System.out.printf("Average Order Value      : $%.2f\n", stats.getAverage());

        System.out.println("\nFirst 3 Successful Buyers (Stream Limit):");
        records.stream()
                .limit(3)
                .forEach(r -> System.out.println(" - " + r.getUserId() + " at " + r.getTimestamp()));
        System.out.println("==========================================");
    }


    public static String generateAnalyticsJson(List<PurchaseResult> results, int totalAttempts) {
        // Stream API to compute Latency Stats
        DoubleSummaryStatistics latencyStats = results.stream()
                .mapToDouble(PurchaseResult::getLatencyMs)
                .summaryStatistics();

        // Stream API to compute Total Revenue
        double totalRevenue = results.stream()
                .filter(r -> "SUCCESS".equals(r.getStatus()))
                .mapToDouble(PurchaseResult::getPricePaid)
                .sum();

        long successfulOrders = results.stream().filter(r -> "SUCCESS".equals(r.getStatus())).count();
        long outOfStockCount = results.stream().filter(r -> "OUT_OF_STOCK".equals(r.getStatus())).count();
        long rateLimitedCount = results.stream().filter(r -> "RATE_LIMITED".equals(r.getStatus())).count();

        // Format as JSON payload for the UI Dashboard
        return String.format("""
            {
                "totalAttempts": %d,
                "successfulOrders": %d,
                "outOfStockCount": %d,
                "rateLimitedCount": %d,
                "totalRevenue": %.2f,
                "avgLatencyMs": %.3f,
                "minLatencyMs": %.3f,
                "maxLatencyMs": %.3f
            }
            """,
            totalAttempts, successfulOrders, outOfStockCount, rateLimitedCount,
            totalRevenue, latencyStats.getAverage(), latencyStats.getMin(), latencyStats.getMax()
        );
    }
}