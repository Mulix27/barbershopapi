package com.bruno.barbershopapi.configuration;

import com.bruno.barbershopapi.util.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    // Rutas públicas — no requieren token JWT
    private static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/api/public/**",
            "/api/health",
            "/api/health/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/webjars/**",
            "/api/public/upload/**",
            "/api/qr/validate/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_URLS).permitAll()

                        // Staff: solo owner
                        .requestMatchers("/api/barbershop/staff/**")
                        .hasAuthority("owner")

                        // Reportes: owner y secretary
                        .requestMatchers("/api/reports/**")
                        .hasAnyAuthority("owner", "secretary")

                        // Ventas: owner, secretary, cashier
                        .requestMatchers("/api/sales/**")
                        .hasAnyAuthority("owner", "secretary", "cashier")

                        // Catálogo: lectura todos, escritura solo owner
                        .requestMatchers(HttpMethod.GET,    "/api/catalog/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/catalog/**").hasAuthority("owner")
                        .requestMatchers(HttpMethod.PUT,    "/api/catalog/**").hasAuthority("owner")
                        .requestMatchers(HttpMethod.DELETE, "/api/catalog/**").hasAuthority("owner")

                        // Configuración barbería: solo owner puede modificar
                        .requestMatchers(HttpMethod.GET,    "/api/barbershop/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/barbershop/**").hasAuthority("owner")
                        .requestMatchers(HttpMethod.PATCH,  "/api/barbershop/**").hasAuthority("owner")
                        .requestMatchers(HttpMethod.DELETE, "/api/barbershop/**").hasAuthority("owner")

                        // Agenda y barberos: todos los roles (el service filtra internamente)
                        .requestMatchers("/api/appointments/**").authenticated()
                        .requestMatchers("/api/barbers/**").authenticated()

                        // Clientes: owner, secretary, cashier (barber no necesita)
                        .requestMatchers("/api/clients/**")
                        .hasAnyAuthority("owner", "secretary", "cashier")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*"));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}