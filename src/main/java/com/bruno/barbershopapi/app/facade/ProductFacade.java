package com.bruno.barbershopapi.app.facade;

import com.bruno.barbershopapi.app.service.ProductService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.product.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;

    public ApiResponse<ProductResponse> create(ProductRequest req, MultipartFile file) {
        try {
            return ApiResponse.ok("Producto creado", productService.create(req, file));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<ProductResponse> update(UUID id, ProductRequest req) {
        try {
            return ApiResponse.ok("Producto actualizado", productService.update(id, req));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<ProductResponse>> findAll() {
        try {
            return ApiResponse.ok(productService.findAll());
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<ProductResponse> adjustStock(UUID id, int quantity) {
        try {
            return ApiResponse.ok("Stock ajustado", productService.adjustStock(id, quantity));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<Void> toggleActive(UUID id) {
        try {
            productService.toggleActive(id);
            return ApiResponse.ok("Estado actualizado", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<ProductResponse> uploadImage(UUID id, MultipartFile file) {
        try {
            return ApiResponse.ok("Imagen subida", productService.uploadImage(id, file));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}