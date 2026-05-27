package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.app.domain.entity.*;
import com.bruno.barbershopapi.app.domain.repository.*;
import com.bruno.barbershopapi.app.web.model.photo.haircut.HaircutPhotoResponse;
import com.bruno.barbershopapi.util.TenantContext;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {

    private final Cloudinary             cloudinary;
    private final HaircutPhotoRepository photoRepository;
    private final ClientHaircutRepository haircutRepository;
    private final UserRepository         userRepository;

    // ── Subir foto de corte (barbero desde el dashboard) ──────

    @Transactional
    public HaircutPhotoResponse uploadHaircutPhoto(
            UUID clientHaircutId,
            MultipartFile file,
            String notes) {

        // Validar que el corte pertenece a esta barbería
        ClientHaircut haircut = haircutRepository.findById(clientHaircutId)
                .orElseThrow(() -> new RuntimeException("Corte no encontrado"));

        if (!haircut.getBarbershop().getId().equals(TenantContext.get())) {
            throw new RuntimeException("Acceso denegado");
        }

        // Validar archivo
        validateImageFile(file);

        // Subir a Cloudinary en la carpeta de la barbería/cliente
        String folder = "barbershop/" + TenantContext.get() +
                "/clients/" + haircut.getClient().getId() +
                "/haircuts";

        Map<?, ?> uploadResult = uploadToCloudinary(file, folder);

        // Obtener usuario que sube la foto (barbero)
        User uploader = getCurrentUser();

        // Guardar en BD
        HaircutPhoto photo = HaircutPhoto.builder()
                .clientHaircut(haircut)
                .url(uploadResult.get("secure_url").toString())
                .publicId(uploadResult.get("public_id").toString())
                .storageProvider("cloudinary")
                .takenAt(OffsetDateTime.now())
                .notes(notes)
                .uploadedByUser(uploader)
                .build();

        return toResponse(photoRepository.save(photo));
    }

    // ── Listar fotos de un corte ───────────────────────────────

    @Transactional(readOnly = true)
    public List<HaircutPhotoResponse> getPhotos(UUID clientHaircutId) {
        ClientHaircut haircut = haircutRepository.findById(clientHaircutId)
                .orElseThrow(() -> new RuntimeException("Corte no encontrado"));

        if (!haircut.getBarbershop().getId().equals(TenantContext.get())) {
            throw new RuntimeException("Acceso denegado");
        }

        return photoRepository
                .findAllByClientHaircutIdOrderByTakenAtDesc(clientHaircutId)
                .stream().map(this::toResponse).toList();
    }

    // ── Eliminar foto ──────────────────────────────────────────

    @Transactional
    public void deletePhoto(UUID photoId) {
        HaircutPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Foto no encontrada"));

        if (!photo.getClientHaircut().getBarbershop().getId().equals(TenantContext.get())) {
            throw new RuntimeException("Acceso denegado");
        }

        // Borrar de Cloudinary
        deleteFromCloudinary(photo.getPublicId());

        // Borrar de BD
        photoRepository.delete(photo);
    }

    // ── Subir foto de referencia (cliente al agendar) ──────────
    // Esta foto no se guarda en BD de cortes, va directo a Cloudinary
    // y retorna la URL para guardarla en la cita o como referencia

    public Map<String, String> uploadReferencePhoto(
            UUID barbershopId,
            MultipartFile file) {

        validateImageFile(file);

        String folder = "barbershop/" + barbershopId + "/references";
        Map<?, ?> result = uploadToCloudinary(file, folder);

        return Map.of(
                "url",      result.get("secure_url").toString(),
                "publicId", result.get("public_id").toString()
        );
    }

    // ══════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ══════════════════════════════════════════════════════════

    public Map<?, ?> uploadToCloudinary(MultipartFile file, String folder) {
        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "barbershop/" + folder,
                            "resource_type", "image",
                            "transformation", "w_1200,c_limit,q_85"
                    )
            );
        } catch (IOException e) {
            log.error("Error al subir imagen a Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Error al subir la imagen. Intenta de nuevo.");
        }
    }

    private void deleteFromCloudinary(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            // Log pero no fallar — la foto ya se borra de BD
            log.warn("No se pudo borrar imagen de Cloudinary: {}", publicId);
        }
    }

    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("El archivo es requerido");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Solo se permiten imágenes (jpg, png, webp)");
        }

        // Validar tamaño máximo: 10MB
        long maxSize = 10L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("La imagen no puede superar 10MB");
        }
    }

    private User getCurrentUser() {
        try {
            String email = SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            return userRepository.findAll().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private HaircutPhotoResponse toResponse(HaircutPhoto p) {
        return new HaircutPhotoResponse(
                p.getId(),
                p.getUrl(),
                p.getPublicId(),
                p.getStorageProvider(),
                p.getTakenAt(),
                p.getNotes(),
                p.getUploadedByUser() != null ? p.getUploadedByUser().getFullName() : null
        );
    }
}