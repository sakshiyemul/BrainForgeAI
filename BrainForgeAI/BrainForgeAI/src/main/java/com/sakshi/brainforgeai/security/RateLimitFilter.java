package com.sakshi.brainforgeai.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private final Map<String, UserRateRecord> requestCounts = new ConcurrentHashMap<>();

    private static class UserRateRecord {
        long windowStart;
        AtomicInteger count;

        UserRateRecord(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Apply rate limit specifically to resource-intensive AI and document endpoints
        if (path.startsWith("/chat") || path.startsWith("/ai") || path.startsWith("/documents")) {
            String clientKey = getClientIdentifier(request);
            long currentTime = System.currentTimeMillis();

            UserRateRecord record = requestCounts.compute(clientKey, (key, existing) -> {
                if (existing == null || (currentTime - existing.windowStart) > 60_000) {
                    return new UserRateRecord(currentTime);
                } else {
                    existing.count.incrementAndGet();
                    return existing;
                }
            });

            if (record.count.get() > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"status\": 429, \"message\": \"Rate limit exceeded. Please wait a moment before sending more requests.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Group by token
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
