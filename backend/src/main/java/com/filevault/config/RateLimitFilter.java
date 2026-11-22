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

    private static final int MAX_AUTH_REQUESTS_PER_MINUTE = 5;
    private static final int MAX_PUBLIC_SHARE_REQUESTS_PER_MINUTE = 30;
    private static final long WINDOW_DURATION_SECONDS = 60L;

    private final Map<String, RateWindow> clientBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = request.getRemoteAddr();

        if (isRateLimitedAuthEndpoint(path) && "POST".equalsIgnoreCase(method)) {
            if (!allowRequest("auth:" + clientIp, MAX_AUTH_REQUESTS_PER_MINUTE)) {
                writeRateLimitResponse(
                    response,
                    "Too many authentication attempts. Please wait a minute and try again."
                );
                return;
            }
        } else if (isRateLimitedPublicShareEndpoint(path) && "GET".equalsIgnoreCase(method)) {
            if (!allowRequest("share:" + clientIp, MAX_PUBLIC_SHARE_REQUESTS_PER_MINUTE)) {
                writeRateLimitResponse(
                    response,
                    "Too many public share requests. Please wait a minute and try again."
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitedAuthEndpoint(String path) {
        return path != null && (
            path.endsWith("/api/v1/auth/login")
            || path.endsWith("/api/v1/auth/register")
            || path.endsWith("/api/v1/vault/unlock")
        );
    }

    private boolean isRateLimitedPublicShareEndpoint(String path) {
        return path != null && path.contains("/api/v1/public/share/");
    }

    private boolean allowRequest(String bucketKey, int maxRequestsPerMinute) {
        long now = Instant.now().getEpochSecond();
        if (clientBuckets.size() > 10000) {
            clientBuckets.entrySet().removeIf(
                entry -> now - entry.getValue().windowStartEpochSec() >= WINDOW_DURATION_SECONDS
            );
        }
        RateWindow window = clientBuckets.compute(bucketKey, (key, existing) -> {
            if (existing == null || now - existing.windowStartEpochSec() >= WINDOW_DURATION_SECONDS) {
                return new RateWindow(now, new AtomicInteger(1));
            }
            existing.counter().incrementAndGet();
            return existing;
        });
        return window.counter().get() <= maxRequestsPerMinute;
    }

    private void writeRateLimitResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
            "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"" + message + "\"}"
        );
    }

    private record RateWindow(long windowStartEpochSec, AtomicInteger counter) {}
}
