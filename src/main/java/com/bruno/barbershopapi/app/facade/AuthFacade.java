package com.bruno.barbershopapi.app.facade;

import com.bruno.barbershopapi.app.service.AuthService;
import com.bruno.barbershopapi.app.web.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;

    public ApiResponse<LoginResponse> login(LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            log.info("Login exitoso: {}", request.email());
            return ApiResponse.ok("Bienvenido, " + response.fullName(), response);
        } catch (RuntimeException e) {
            log.warn("Intento de login fallido para: {}", request.email());
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<LoginResponse> register(RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            log.info("Nueva barbería registrada: {} (slug: {})",
                    request.barbershopName(), request.slug());
            return ApiResponse.ok("Barbería registrada exitosamente. Tienes 14 días de prueba.", response);
        } catch (RuntimeException e) {
            log.warn("Error al registrar barbería: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}