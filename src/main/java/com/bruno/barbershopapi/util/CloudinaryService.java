package com.bruno.barbershopapi.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Tipos de archivo permitidos
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    // Tamaño máximo: 5 MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    // ── Upload ─────────────────────────────────────────────────

    /**
     * Sube un archivo a Cloudinary en la carpeta indicada.
     *
     * @param file   archivo recibido del frontend
     * @param folder carpeta dentro de Cloudinary (ej. "barber_profiles", "haircut_photos")
     * @return Map con los campos de Cloudinary: secure_url, public_id, width, height, etc.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> upload(MultipartFile file, String folder) throws IOException {
        validateFile(file);

        // public_id único para evitar colisiones
        String publicId = folder + "/" + UUID.randomUUID();

        Map<String, Object> options = ObjectUtils.asMap(
                "folder",          folder,
                "public_id",       publicId,
                "overwrite",       true,
                "resource_type",   "image",
                // Transformación automática: max 800x800, calidad auto
                "transformation",  "c_limit,w_800,h_800,q_auto,f_auto"
        );

        return (Map<String, Object>) cloudinary.uploader()
                .upload(file.getBytes(), options);
    }

    // ── Delete ─────────────────────────────────────────────────

    /**
     * Elimina un recurso de Cloudinary por su public_id.
     *
     * @param publicId el campo "public_id" retornado por upload()
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> delete(String publicId) throws IOException {
        if (publicId == null || publicId.isBlank()) {
            log.warn("Se intentó borrar un publicId nulo o vacío");
            return Map.of("result", "skipped");
        }

        Map<String, Object> result = (Map<String, Object>) cloudinary.uploader()
                .destroy(publicId, ObjectUtils.emptyMap());

        log.info("Cloudinary delete '{}': {}", publicId, result.get("result"));
        return result;
    }

    // ── Upload desde URL (para migrar fotos existentes) ────────

    /**
     * Sube una imagen desde una URL externa a Cloudinary.
     * Útil para importar fotos de Google, Facebook, etc.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadFromUrl(String imageUrl, String folder) throws IOException {
        String publicId = folder + "/" + UUID.randomUUID();

        Map<String, Object> options = ObjectUtils.asMap(
                "folder",        folder,
                "public_id",     publicId,
                "overwrite",     true,
                "resource_type", "image"
        );

        return (Map<String, Object>) cloudinary.uploader()
                .upload(imageUrl, options);
    }

    // ── Validación ─────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "El archivo excede el tamaño máximo permitido (5 MB)"
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido. Solo se aceptan: JPEG, PNG, WebP, GIF"
            );
        }
    }
}