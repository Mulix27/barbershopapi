package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.app.domain.entity.*;
import com.bruno.barbershopapi.app.domain.repository.*;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.staff.*;
import com.bruno.barbershopapi.util.CloudinaryService;
import com.bruno.barbershopapi.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BarberStaffService {

    private final BarberProfileRepository barberProfileRepository;
    private final UserRepository          userRepository;
    private final BarbershopRepository    barbershopRepository;
    private final PasswordEncoder         passwordEncoder;
    private final CloudinaryService       cloudinaryService;

    // ── Listar todos los barberos de la barbería ───────────────

    @Transactional(readOnly = true)
    public ApiResponse<List<BarberResponse>> getAll() {
        UUID shopId = TenantContext.get();
        List<BarberResponse> list = barberProfileRepository
                .findAllByBarbershopId(shopId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ApiResponse.ok(list);
    }

    // ── Listar opciones para dropdowns ─────────────────────────

    @Transactional(readOnly = true)
    public ApiResponse<List<BarberOptionResponse>> getOptions() {
        UUID shopId = TenantContext.get();
        List<BarberOptionResponse> list = barberProfileRepository
                .findActiveByBarbershopId(shopId)
                .stream()
                .map(bp -> new BarberOptionResponse(
                        bp.getUser().getId(),
                        bp.getUser().getFullName(),
                        bp.getSpecialty(),
                        bp.getPhotoUrl(),
                        bp.getStatus()
                ))
                .toList();
        return ApiResponse.ok(list);
    }

    // ── Obtener uno por id de perfil ───────────────────────────

    @Transactional(readOnly = true)
    public ApiResponse<BarberResponse> getOne(UUID profileId) {
        BarberProfile bp = findProfile(profileId);
        return ApiResponse.ok(toResponse(bp));
    }

    // ── Crear barbero ──────────────────────────────────────────

    @Transactional
    public ApiResponse<BarberResponse> create(CreateBarberRequest req) {
        UUID shopId = TenantContext.get();

        // Verificar que el email no esté en uso
        if (userRepository.findAll().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(req.email()))) {
            return ApiResponse.error("El email ya está registrado en el sistema");
        }

        Barbershop shop = barbershopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Barbería no encontrada"));

        // 1. Crear el usuario con rol 'barber'
        User user = User.builder()
                .barbershop(shop)
                .fullName(req.fullName())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role("barber")
                .isActive(true)
                .build();
        user = userRepository.save(user);

        // 2. Crear el perfil extendido
        BarberProfile profile = BarberProfile.builder()
                .user(user)
                .barbershop(shop)
                .specialty(req.specialty())
                .bio(req.bio())
                .status("active")
                .build();
        profile = barberProfileRepository.save(profile);

        log.info("Barbero creado: {} (shopId={})", user.getEmail(), shopId);
        return ApiResponse.ok(toResponse(profile));
    }

    // ── Actualizar perfil ──────────────────────────────────────

    @Transactional
    public ApiResponse<BarberResponse> update(UUID profileId, UpdateBarberRequest req) {
        BarberProfile bp = findProfile(profileId);

        // Actualizar nombre en users
        bp.getUser().setFullName(req.fullName());
        userRepository.save(bp.getUser());

        // Actualizar perfil
        bp.setSpecialty(req.specialty());
        bp.setBio(req.bio());
        barberProfileRepository.save(bp);

        return ApiResponse.ok(toResponse(bp));
    }

    // ── Cambiar estado (active / on_break / inactive) ──────────

    @Transactional
    public ApiResponse<BarberResponse> updateStatus(UUID profileId,
                                                    UpdateBarberStatusRequest req) {
        BarberProfile bp = findProfile(profileId);
        bp.setStatus(req.status());

        // Si se desactiva, también desactivar el usuario
        if ("inactive".equals(req.status())) {
            bp.getUser().setIsActive(false);
            userRepository.save(bp.getUser());
        } else if ("active".equals(req.status())) {
            bp.getUser().setIsActive(true);
            userRepository.save(bp.getUser());
        }

        barberProfileRepository.save(bp);
        return ApiResponse.ok(toResponse(bp));
    }

    // ── Subir / cambiar foto de perfil ─────────────────────────

    @Transactional
    public ApiResponse<BarberResponse> uploadPhoto(UUID profileId, MultipartFile file) {
        BarberProfile bp = findProfile(profileId);

        try {
            // Si ya tiene foto → borrar la anterior en Cloudinary
            if (bp.getPhotoPublicId() != null) {
                cloudinaryService.delete(bp.getPhotoPublicId());
            }

            // Subir nueva foto
            var result = cloudinaryService.upload(file, "barber_profiles");
            bp.setPhotoUrl((String) result.get("secure_url"));
            bp.setPhotoPublicId((String) result.get("public_id"));
            barberProfileRepository.save(bp);

            return ApiResponse.ok(toResponse(bp));
        } catch (Exception e) {
            log.error("Error subiendo foto del barbero {}: {}", profileId, e.getMessage());
            return ApiResponse.error("Error al subir la foto: " + e.getMessage());
        }
    }

    // ── Eliminar foto ──────────────────────────────────────────

    @Transactional
    public ApiResponse<BarberResponse> deletePhoto(UUID profileId) {
        BarberProfile bp = findProfile(profileId);

        if (bp.getPhotoPublicId() != null) {
            try {
                cloudinaryService.delete(bp.getPhotoPublicId());
            } catch (Exception e) {
                log.warn("No se pudo borrar foto en Cloudinary: {}", e.getMessage());
            }
            bp.setPhotoUrl(null);
            bp.setPhotoPublicId(null);
            barberProfileRepository.save(bp);
        }

        return ApiResponse.ok(toResponse(bp));
    }

    // ── Eliminar barbero (soft: desactivar; hard: borrar) ──────

    @Transactional
    public ApiResponse<Void> delete(UUID profileId) {
        BarberProfile bp = findProfile(profileId);

        // Borrar foto de Cloudinary si existe
        if (bp.getPhotoPublicId() != null) {
            try { cloudinaryService.delete(bp.getPhotoPublicId()); }
            catch (Exception e) { log.warn("No se pudo borrar foto: {}", e.getMessage()); }
        }

        // Desactivar usuario en vez de borrar (preserva historial de citas/ventas)
        bp.getUser().setIsActive(false);
        userRepository.save(bp.getUser());
        bp.setStatus("inactive");
        barberProfileRepository.save(bp);

        return ApiResponse.ok(null);
    }

    // ── Helpers ────────────────────────────────────────────────

    private BarberProfile findProfile(UUID profileId) {
        UUID shopId = TenantContext.get();
        BarberProfile bp = barberProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));

        // Verificar que pertenece a esta barbería (seguridad multi-tenant)
        if (!bp.getBarbershop().getId().equals(shopId)) {
            throw new RuntimeException("Acceso denegado");
        }
        return bp;
    }

    private BarberResponse toResponse(BarberProfile bp) {
        return new BarberResponse(
                bp.getId(),
                bp.getUser().getId(),
                bp.getUser().getFullName(),
                bp.getUser().getEmail(),
                bp.getSpecialty(),
                bp.getBio(),
                bp.getPhotoUrl(),
                bp.getStatus(),
                bp.getRating(),
                bp.getTotalCuts(),
                bp.getUser().getIsActive(),
                bp.getCreatedAt()
        );
    }
}