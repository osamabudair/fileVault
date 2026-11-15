package com.filevault.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final long WINDOW_DURATION_SECONDS = 60L;

    private final Map<String, RateWindow> clientBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (isRateLimitedAuthEndpoint(path) && "POST".equalsIgnoreCase(request.getMethod())) {
            String clientIp = request.getRemoteAddr();
            if (!allowRequest(clientIp)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("""
                    {"status":429,"error":"Too Many Requests","message":"Too many authentication attempts. Please wait a minute and try again."}
                    """.strip());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitedAuthEndpoint(String path) {
        return path != null && (path.endsWith("/api/v1/auth/login") || path.endsWith("/api/v1/auth/register"));
    }

    private boolean allowRequest(String clientIp) {
        long now = Instant.now().getEpochSecond();
        if (clientBuckets.size() > 10000) {
            clientBuckets.entrySet().removeIf(
                entry -> now - entry.getValue().windowStartEpochSec() >= WINDOW_DURATION_SECONDS
            );
        }
        RateWindow window = clientBuckets.compute(clientIp, (ip, existing) -> {
            if (existing == null || now - existing.windowStartEpochSec() >= WINDOW_DURATION_SECONDS) {
                return new RateWindow(now, new AtomicInteger(1));
            }
            existing.counter().incrementAndGet();
            return existing;
        });
        return window.counter().get() <= MAX_REQUESTS_PER_MINUTE;
    }

    private record RateWindow(long windowStartEpochSec, AtomicInteger counter) {}
}
