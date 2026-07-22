import java.time.LocalTime;

public class PurchaseRecord {
    private final String userId;
    private final String productId;
    private final double pricePaid;
    private final LocalTime timestamp;

    public PurchaseRecord(String userId, String productId, double pricePaid) {
        this.userId = userId;
        this.productId = productId;
        this.pricePaid = pricePaid;
        this.timestamp = LocalTime.now();
    }

    public String getUserId() { return userId; }
    public String getProductId() { return productId; }
    public double getPricePaid() { return pricePaid; }
    public LocalTime getTimestamp() { return timestamp; }
}