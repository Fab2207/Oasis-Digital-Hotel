package com.gestion.hotelera.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int REQUEST_LIMIT = 1000;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public RateLimitingFilter() {
        scheduler.scheduleAtFixedRate(counters::clear, WINDOW.toMillis(), WINDOW.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        AtomicInteger count = counters.computeIfAbsent(ip, k -> new AtomicInteger(0));
        if (count.incrementAndGet() <= REQUEST_LIMIT) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.getWriter().write("Too many requests - rate limit exceeded");
        }
    }
}
