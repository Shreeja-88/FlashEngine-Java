import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private final int maxRequestsPerSecond;
    private final Map<String, UserRequestInfo> userRequests = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequestsPerSecond) {
        this.maxRequestsPerSecond = maxRequestsPerSecond;
    }

    public synchronized boolean allowRequest(String userId) {
        long currentTime = System.currentTimeMillis();
        userRequests.putIfAbsent(userId, new UserRequestInfo(currentTime, 0));

        UserRequestInfo info = userRequests.get(userId);

        // Reset counter if more than 1 second (1000ms) has passed
        if (currentTime - info.lastResetTime > 1000) {
            info.lastResetTime = currentTime;
            info.requestCount = 0;
        }

        if (info.requestCount < maxRequestsPerSecond) {
            info.requestCount++;
            return true; // Request allowed
        }

        return false; // Rate limit exceeded!
    }

    private static class UserRequestInfo {
        long lastResetTime;
        int requestCount;

        UserRequestInfo(long lastResetTime, int requestCount) {
            this.lastResetTime = lastResetTime;
            this.requestCount = requestCount;
        }
    }
}