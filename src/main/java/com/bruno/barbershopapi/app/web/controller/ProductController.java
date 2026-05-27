package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.facade.ProductFacade;
import com.bruno.barbershopapi.app.web.model.*;
import com.bruno.barbershopapi.app.web.model.product.ProductRequest;
import com.bruno.barbershopapi.app.web.model.product.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Productos", description = "Inventario de productos físicos vendibles (pomadas, ceras, etc.)")
public class ProductController {

    private final ProductFacade productFacade;

    @Operation(summary = "Listar productos activos")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> findAll() {
        ApiResponse<List<ProductResponse>> res = productFacade.findAll();
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @RequestPart("product") @Valid ProductRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        ApiResponse<ProductResponse> res = productFacade.create(req, file);
        return ResponseEntity.status(res.success() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(res);
    }

    @Operation(summary = "Actualizar producto")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest req) {
        ApiResponse<ProductResponse> res = productFacade.update(id, req);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Operation(
            summary = "Ajustar stock",
            description = "Suma o resta unidades al stock. Usa cantidad positiva para agregar, negativa para restar."
    )
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ProductResponse>> adjustStock(
            @PathVariable UUID id,
            @Parameter(description = "Cantidad a sumar (+) o restar (-)", example = "10")
            @RequestParam int quantity) {
        ApiResponse<ProductResponse> res = productFacade.adjustStock(id, quantity);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Operation(summary = "Activar o desactivar producto")
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggle(@PathVariable UUID id) {
        ApiResponse<Void> res = productFacade.toggleActive(id);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @PatchMapping("/{id}/image")
    public ResponseEntity<ApiResponse<ProductResponse>> uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        ApiResponse<ProductResponse> res = productFacade.uploadImage(id, file);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }
}