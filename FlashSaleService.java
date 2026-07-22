
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import OutOfStockException;
import Product;

public class FlashSaleService {
    @SuppressWarnings("unused")
    private final Product product;
    private final AtomicInteger stock;
    // Set derived from ConcurrentHashMap for thread-safe uniqueness check
    private final Set successfulUsers = ConcurrentHashMap.newKeySet();

    public FlashSaleService(Product product, int initialStock) {
        this.product = product;
        this.stock = new AtomicInteger(initialStock);
    }

    public boolean purchase(String userId) throws OutOfStockException, DuplicatePurchaseException {
        // 1. Check for duplicate purchase atomically
        if (!successfulUsers.add(userId)) {
            throw new DuplicatePurchaseException("User " + userId + " has already purchased this item!");
        }

        // 2. Decrement stock atomically only if stock > 0
        while (true) {
            int currentStock = stock.get();
            if (currentStock <= 0) {
                // Rollback user eligibility if stock is empty
                successfulUsers.remove(userId);
                throw new OutOfStockException("Sale ended! Item is out of stock.");
            }

            // Atomic Compare-And-Swap (CAS)
            if (stock.compareAndSet(currentStock, currentStock - 1)) {
                return true; // Purchase successful!
            }
            // If compareAndSet fails due to another thread modifying stock, loop retries automatically
        }
    }

    public int getRemainingStock() {
        return stock.get();
    }

    public int getTotalSuccessfulPurchases() {
        return successfulUsers.size();
    }
}