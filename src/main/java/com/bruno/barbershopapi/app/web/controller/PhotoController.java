package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.facade.PhotoFacade;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.photo.haircut.HaircutPhotoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Fotos de cortes", description = "Subir y gestionar fotos del historial de cortes de los clientes")
public class PhotoController {

    private final PhotoFacade photoFacade;

    // ── Subir foto de corte (barbero) ──────────────────────────

    @Operation(
            summary = "Subir foto de un corte",
            description = "El barbero sube una foto desde su celular o computadora. " +
                    "Se comprime automáticamente y se guarda en Cloudinary. " +
                    "La foto queda ligada al corte del cliente para su historial.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(
            value = "/api/clients/haircuts/{clientHaircutId}/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<HaircutPhotoResponse>> upload(
            @PathVariable UUID clientHaircutId,

            @Parameter(description = "Archivo de imagen (jpg, png, webp — máx 10MB)")
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "Notas opcionales sobre la foto", example = "Resultado final")
            @RequestPart(value = "notes", required = false) String notes) {

        ApiResponse<HaircutPhotoResponse> res = photoFacade.upload(clientHaircutId, file, notes);
        return ResponseEntity.status(res.success() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(res);
    }

    // ── Listar fotos de un corte ───────────────────────────────

    @Operation(
            summary = "Ver fotos de un corte",
            description = "Retorna todas las fotos guardadas de un corte específico del cliente, " +
                    "ordenadas de la más reciente a la más antigua."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/clients/haircuts/{clientHaircutId}/photos")
    public ResponseEntity<ApiResponse<List<HaircutPhotoResponse>>> getPhotos(
            @PathVariable UUID clientHaircutId) {

        ApiResponse<List<HaircutPhotoResponse>> res = photoFacade.getPhotos(clientHaircutId);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── Eliminar foto ──────────────────────────────────────────

    @Operation(
            summary = "Eliminar foto",
            description = "Elimina la foto tanto de Cloudinary como de la base de datos."
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/api/clients/haircuts/photos/{photoId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID photoId) {
        ApiResponse<Void> res = photoFacade.delete(photoId);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── Foto de referencia (cliente al agendar) ────────────────

    @Operation(
            summary = "Subir foto de referencia",
            description = "El cliente sube una foto de referencia al agendar una cita " +
                    "(por ejemplo: 'quiero este corte'). " +
                    "Endpoint público — no requiere token. " +
                    "Retorna la URL para incluirla en la solicitud de cita.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
    )
    @PostMapping(
            value = "/api/public/barbershops/{barbershopId}/reference-photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadReference(
            @PathVariable UUID barbershopId,

            @Parameter(description = "Foto de referencia del corte deseado (máx 10MB)")
            @RequestPart("file") MultipartFile file) {

        ApiResponse<Map<String, String>> res = photoFacade.uploadReference(barbershopId, file);
        return ResponseEntity.status(res.success() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(res);
    }
}