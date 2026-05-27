package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.facade.AuthFacade;
import com.bruno.barbershopapi.app.web.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registro e inicio de sesión. Endpoints públicos — no requieren token JWT.")
public class AuthController {

    private final AuthFacade authFacade;

    // ── POST /api/auth/login ───────────────────────────────────

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica a un usuario y retorna un token JWT. " +
                    "Incluye el token en las demás peticiones como: `Authorization: Bearer <token>`"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Bienvenido, Carlos Mendoza",
                                      "data": {
                                        "token": "eyJhbGci...",
                                        "tokenType": "Bearer",
                                        "userId": "uuid",
                                        "fullName": "Carlos Mendoza",
                                        "role": "owner",
                                        "barbershopId": "uuid",
                                        "barbershopName": "Barbería El Clásico",
                                        "singleBarber": true
                                      }
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        ApiResponse<LoginResponse> response = authFacade.login(request);
        HttpStatus status = response.success() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(response);
    }

    // ── POST /api/auth/register ────────────────────────────────

    @Operation(
            summary = "Registrar nueva barbería",
            description = "Crea una nueva barbería con su usuario owner y una suscripción de prueba de 14 días. " +
                    "Retorna el token JWT listo para usar."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Barbería creada exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o slug ya en uso"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        ApiResponse<LoginResponse> response = authFacade.register(request);
        HttpStatus status = response.success() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    // ── GET /api/auth/me ───────────────────────────────────────

    @Operation(
            summary = "Obtener usuario autenticado",
            description = "Retorna los datos del usuario propietario del token JWT actual. " +
                    "Útil para que Angular verifique la sesión al recargar la app."
    )
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> me(
            @RequestHeader("Authorization") String authHeader) {
        // Por ahora retorna confirmación — en el siguiente paso
        // extraeremos el usuario completo desde el token
        return ResponseEntity.ok(ApiResponse.ok("Token válido", authHeader.substring(7, 30) + "..."));
    }
}