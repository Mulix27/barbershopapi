package com.bruno.barbershopapi.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain ) throws ServletException, IOException {

        String path = request.getServletPath();

        if (HttpMethod.OPTIONS.matches(request.getMethod()) ||
                path.startsWith("/api/auth/") ||
                path.startsWith("/api/public/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs")) {

            chain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtUtil.isTokenValid(token)) {
                    String email       = jwtUtil.extractEmail(token);
                    String role        = jwtUtil.extractRole(token);
                    UUID barbershopId  = jwtUtil.extractBarbershopId(token);
                    UUID userId       = jwtUtil.extractUserId(token);

                    // Cargar el barbershopId en TenantContext para filtrar datos en servicios
                    TenantContext.set(barbershopId);

                    if (userId != null) UserContext.setUserId(userId);
                    if (role   != null) UserContext.setRole(role);

                    var auth = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

            chain.doFilter(request, response);

        } finally {
            // CRÍTICO: limpiar el ThreadLocal al terminar el request
            // para evitar que el barbershopId se filtre a otro request
            TenantContext.clear();
            UserContext.clear();
        }
    }
}