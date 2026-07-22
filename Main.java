import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import DuplicatePurchaseException;
import FlashSaleService;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int totalItems = 10;
        int totalConcurrentUsers = 100;
        double itemPrice = 499.99;

        Product phone = new Product("P100", "Flagship Smartphone");
        FlashSaleService saleService = new FlashSaleService(phone, totalItems);

        // Thread pool with 20 worker threads
        ExecutorService executor = Executors.newFixedThreadPool(totalConcurrentUsers);
        CountDownLatch readyLatch = new CountDownLatch(totalConcurrentUsers);
        CountDownLatch startLatch = new CountDownLatch(1);

        // Thread-safe list to store successful purchase records for analytics
        List<PurchaseRecord> purchaseRecords = new CopyOnWriteArrayList<>();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger outOfStockCount = new AtomicInteger(0);

        System.out.println("=== FLASH SALE STARTED ===");
        System.out.println("Initial Stock: " + totalItems);
        System.out.println("Simulating " + totalConcurrentUsers + " buyers...\n");

        for (int i = 1; i <= totalConcurrentUsers; i++) {
            final String userId = "User_" + i;

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Signal that this thread is ready
                        readyLatch.countDown();
                        
                        // Wait until ALL 100 threads are ready at the starting line
                        startLatch.await();
                        
                        // Attempt the purchase
                        saleService.purchase(userId);
                        
                        // If successful, log the purchase
                        successCount.incrementAndGet();
                        purchaseRecords.add(new PurchaseRecord(userId, phone.getProductId(), itemPrice));
                        System.out.println(" SUCCESS: " + userId + " secured an item!");
                        
                    } catch (OutOfStockException e) {
                        outOfStockCount.incrementAndGet();
                    } catch (DuplicatePurchaseException e) {
                        System.out.println(" REJECTED: " + e.getMessage());
                    } catch (InterruptedException e) {
                    }
                }
            });
        }

        // Wait for all 100 threads to reach the starting line
        readyLatch.await(); 
        
        // BANG! Release all threads simultaneously
        startLatch.countDown(); 

        // Gracefully shutdown the executor and wait for execution to complete
        executor.shutdown();
        boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);

        if (finished) {
            System.out.println("\n=== SALE COMPLETED ===");
            
            // Run Stream API Analytics Report
            SaleAnalytics.generateReport(purchaseRecords, totalConcurrentUsers);
        } else {
            System.out.println("\n Error: Simulation timed out.");
        }
    }
}