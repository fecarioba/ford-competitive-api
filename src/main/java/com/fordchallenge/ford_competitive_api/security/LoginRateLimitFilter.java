package com.fordchallenge.ford_competitive_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/** Limite por IP para a rota de login; para múltiplas instâncias usar gateway/Redis. */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(LoginRateLimitFilter.class);
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = Duration.ofMinutes(1).toMillis();
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !"/auth/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        // RemoteAddr é controlado pelo servidor; não confiar em X-Forwarded-For enviado pelo cliente.
        String key = request.getRemoteAddr();
        Window window = windows.compute(key, (ip, current) -> {
            if (current == null || now - current.startedAt >= WINDOW_MS) return new Window(now, 1);
            return new Window(current.startedAt, current.count + 1);
        });
        if (window.count > MAX_REQUESTS) {
            log.warn("event=login_rate_limited clientIp={}", key);
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(Math.max(1, (WINDOW_MS - (now - window.startedAt)) / 1000)));
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Muitas tentativas de login\"}");
            return;
        }
        // Remove janelas vencidas para que o mapa não cresça indefinidamente.
        if (windows.size() > 10_000) windows.entrySet().removeIf(e -> now - e.getValue().startedAt >= WINDOW_MS);
        chain.doFilter(request, response);
    }

    private record Window(long startedAt, int count) {}
}
