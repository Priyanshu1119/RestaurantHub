package com.restaurant.hub.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A minimal in-memory sliding-window limiter for the chatbot endpoint, since
 * LLM calls cost money per request. Fine for a single backend instance;
 * swap for a Redis-backed limiter if this ever runs on more than one node.
 */
@Component
public class SimpleRateLimiter {

    private final Map<Long, CopyOnWriteArrayList<Instant>> hits = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_SECONDS = 60;

    public boolean allow(Long userId) {
        Instant now = Instant.now();
        CopyOnWriteArrayList<Instant> timestamps = hits.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>());
        timestamps.removeIf(t -> t.isBefore(now.minusSeconds(WINDOW_SECONDS)));
        if (timestamps.size() >= MAX_REQUESTS) {
            return false;
        }
        timestamps.add(now);
        return true;
    }
}
