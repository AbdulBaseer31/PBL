package com.vrs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (isPublicPath(method, path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login required");
            return;
        }

        String token = header.substring(7).trim();
        Claims claims;
        try {
            claims = jwtUtil.readToken(token);
        } catch (ExpiredJwtException e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Session expired, please login again");
            return;
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        String customerId = claims.getSubject();
        String role = claims.get("role", String.class);
        CurrentUser currentUser = new CurrentUser(customerId, role);
        request.setAttribute("currentUser", currentUser);

        if (isAdminOnlyRoute(method, path) && !currentUser.isAdmin()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Admin only");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String method, String path) {
        if ("POST".equalsIgnoreCase(method)) {
            if (path.startsWith("/auth/register") || path.startsWith("/auth/login")) {
                return true;
            }
        }
        if (path.startsWith("/auth/")) {
            return true;
        }

        // Static frontend files and assets must be public
        if (path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")
                || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")
                || path.endsWith(".ico") || "/".equals(path)) {
            return true;
        }

        // Protect REST endpoints: /vehicles, /reservations, /bills
        return !path.startsWith("/vehicles") && !path.startsWith("/reservations") && !path.startsWith("/bills");
    }

    private boolean isAdminOnlyRoute(String method, String path) {
        if ("POST".equalsIgnoreCase(method) && "/vehicles".equals(path)) return true;
        if ("DELETE".equalsIgnoreCase(method) && path.startsWith("/vehicles/")) return true;
        if ("GET".equalsIgnoreCase(method) && "/vehicles/stats".equals(path)) return true;
        return false;
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}

