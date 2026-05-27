package com.bruno.barbershopapi.app.facade;

import com.bruno.barbershopapi.app.service.SaleService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.sale.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaleFacade {

    private final SaleService saleService;

    public ApiResponse<SaleResponse> create(SaleRequest req) {
        try {
            SaleResponse sale = saleService.create(req);
            log.info("Venta registrada: {} total: {}", sale.id(), sale.total());
            return ApiResponse.ok("Venta registrada", sale);
        } catch (RuntimeException e) {
            log.warn("Error al registrar venta: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<SaleResponse>> findAll() {
        try {
            return ApiResponse.ok(saleService.findAll());
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<SaleResponse>> findByDateRange(OffsetDateTime from, OffsetDateTime to) {
        try {
            return ApiResponse.ok(saleService.findByDateRange(from, to));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<SaleResponse> findById(UUID id) {
        try {
            return ApiResponse.ok(saleService.findById(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<SaleResponse> cancel(UUID id) {
        try {
            return ApiResponse.ok("Venta cancelada", saleService.cancel(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<SaleResponse>> findByClient(UUID clientId) {
        try {
            return ApiResponse.ok(saleService.findByClient(clientId));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
 