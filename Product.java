public class Product {
    private final String productId;
    private final String name;

    public Product(String productId, String name) {
        this.productId = productId;
        this.name = name;
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
}