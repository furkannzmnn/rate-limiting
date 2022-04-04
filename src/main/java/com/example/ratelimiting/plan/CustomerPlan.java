package com.example.ratelimiting.plan;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import java.time.Duration;

public enum CustomerPlan {

    FREE(1),
    BASIC(3),
    PRO(4);

    private final int bucketCapacity;

    private CustomerPlan(int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
    }

    public Bandwidth getLimit() {
        return Bandwidth.classic(bucketCapacity, Refill.intervally(bucketCapacity, Duration.ofDays(1)));
    }

    public int bucketCapacity() {
        return bucketCapacity;
    }


    public static CustomerPlan resolvePlan(String apiKey) {
        if (apiKey.equals("free")) {
            return FREE;
        }
        if (apiKey.equals("basic")) {
            return BASIC;
        }
        if (apiKey.equals("pro")) {
            return PRO;
        }
        throw new IllegalArgumentException("Invalid api key");
    }
}
