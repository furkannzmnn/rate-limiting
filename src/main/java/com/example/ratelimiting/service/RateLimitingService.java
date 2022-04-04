package com.example.ratelimiting.service;

import com.example.ratelimiting.plan.CustomerPlan;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();


    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final int MAX_REQUESTS_PER_HOUR = 100;
    private static final int MAX_REQUESTS_PER_DAY = 1000;


    private static final String HEADER_API_KEY = "X-api-key";
    private static final String HEADER_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
    private static final String HEADER_RETRY_AFTER = "X-Rate-Limit-Retry-After-Seconds";


    @CacheEvict(cacheNames = "bucket", key = "#ip")
    public Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, this::newBucket);
    }

    private Bucket newBucket(String plan) {
        if (plan.contains(CustomerPlan.FREE.name())) {
            plan = CustomerPlan.FREE.name().toLowerCase(Locale.ROOT);
        }else if (plan.contains(CustomerPlan.BASIC.name())) {
            plan = CustomerPlan.BASIC.name().toLowerCase(Locale.ROOT);
        }else{
            plan = CustomerPlan.PRO.name().toLowerCase(Locale.ROOT);
        }
        CustomerPlan customerPlan = CustomerPlan.resolvePlan(plan);
        return bucket(customerPlan.getLimit());
    }


    private Bucket bucket(Bandwidth limit) {
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }



    public boolean isRateLimitExceeded(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String apiKey = String.join("/", request.getRequestURI().split("/"));


        if (apiKey.isEmpty()) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing Header: " + HEADER_API_KEY);
            return false;
        }

        Bucket tokenBucket = resolveBucket(apiKey);

        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {

            response.addHeader(HEADER_LIMIT_REMAINING, String.valueOf(probe.getRemainingTokens()));
            return true;

        } else {

            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader(HEADER_RETRY_AFTER, String.valueOf(waitForRefill));
            return false;
        }
    }
}
