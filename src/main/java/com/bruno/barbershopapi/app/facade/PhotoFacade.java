package com.bruno.barbershopapi.app.facade;

import com.bruno.barbershopapi.app.service.PhotoService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.photo.haircut.HaircutPhotoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoFacade {

    private final PhotoService photoService;

    public ApiResponse<HaircutPhotoResponse> upload(
            UUID clientHaircutId, MultipartFile file, String notes) {
        try {
            HaircutPhotoResponse res = photoService.uploadHaircutPhoto(clientHaircutId, file, notes);
            log.info("Foto subida para corte: {}", clientHaircutId);
            return ApiResponse.ok("Foto guardada exitosamente", res);
        } catch (RuntimeException e) {
            log.warn("Error al subir foto: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<HaircutPhotoResponse>> getPhotos(UUID clientHaircutId) {
        try {
            return ApiResponse.ok(photoService.getPhotos(clientHaircutId));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<Void> delete(UUID photoId) {
        try {
            photoService.deletePhoto(photoId);
            return ApiResponse.ok("Foto eliminada", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<Map<String, String>> uploadReference(
            UUID barbershopId, MultipartFile file) {
        try {
            Map<String, String> result = photoService.uploadReferencePhoto(barbershopId, file);
            return ApiResponse.ok("Foto de referencia subida", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}