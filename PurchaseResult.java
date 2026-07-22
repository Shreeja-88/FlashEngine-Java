public class PurchaseResult {
    private final String userId;
    private final String status; // SUCCESS, OUT_OF_STOCK, DUPLICATE, RATE_LIMITED
    private final double latencyMs;
    private final double pricePaid;

    public PurchaseResult(String userId, String status, double latencyMs, double pricePaid) {
        this.userId = userId;
        this.status = status;
        this.latencyMs = latencyMs;
        this.pricePaid = pricePaid;
    }

    public String getUserId() { return userId; }
    public String getStatus() { return status; }
    public double getLatencyMs() { return latencyMs; }
    public double getPricePaid() { return pricePaid; }
}