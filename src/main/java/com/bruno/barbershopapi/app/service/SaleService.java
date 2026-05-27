package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.app.domain.entity.*;
import com.bruno.barbershopapi.app.domain.repository.*;
import com.bruno.barbershopapi.app.web.model.sale.*;
import com.bruno.barbershopapi.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository           saleRepository;
    private final ClientRepository         clientRepository;
    private final HaircutCatalogRepository catalogRepository;
    private final ProductRepository        productRepository;
    private final UserRepository           userRepository;
    private final BarbershopRepository     barbershopRepository;

    // ── Registrar venta ────────────────────────────────────────

    @Transactional
    public SaleResponse create(SaleRequest req) {
        UUID shopId = TenantContext.get();
        Barbershop shop = barbershopRepository.findById(shopId).orElseThrow();

        // Cliente opcional (puede ser de paso)
        Client client = null;
        if (req.clientId() != null) {
            client = clientRepository.findById(req.clientId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        }

        // Barbero que atendió (opcional)
        User attendedBy = null;
        if (req.attendedByUserId() != null) {
            attendedBy = userRepository.findById(req.attendedByUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }

        // Construir items y calcular totales
        List<SaleItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (SaleItemRequest itemReq : req.items()) {
            SaleItem item = buildItem(itemReq, shopId);
            subtotal = subtotal.add(item.getTotal());
            items.add(item);
        }

        BigDecimal discount = req.discount() != null ? req.discount() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount);

        // Construir la venta
        Sale sale = Sale.builder()
                .barbershop(shop)
                .client(client)
                .attendedByUser(attendedBy)
                .paymentMethod(req.paymentMethod())
                .subtotal(subtotal)
                .discount(discount)
                .total(total)
                .status("completed")
                .notes(req.notes())
                .build();

        // Asignar la venta a cada item (relación bidireccional)
        items.forEach(item -> item.setSale(sale));
        sale.getItems().addAll(items);

        return toResponse(saleRepository.save(sale));
    }

    // ── Listar ventas de la barbería ───────────────────────────

    @Transactional(readOnly = true)
    public List<SaleResponse> findAll() {
        return saleRepository
                .findAllByBarbershopIdOrderByCreatedAtDesc(TenantContext.get())
                .stream().map(this::toResponse).toList();
    }

    // ── Ventas por rango de fecha ──────────────────────────────

    @Transactional(readOnly = true)
    public List<SaleResponse> findByDateRange(OffsetDateTime from, OffsetDateTime to) {
        return saleRepository
                .findByBarbershopAndDateRange(TenantContext.get(), from, to)
                .stream().map(this::toResponse).toList();
    }

    // ── Historial de ventas de un cliente ─────────────────────

    @Transactional(readOnly = true)
    public List<SaleResponse> findByClient(UUID clientId) {
        return saleRepository
                .findAllByBarbershopIdAndClientIdOrderByCreatedAtDesc(
                        TenantContext.get(), clientId)
                .stream().map(this::toResponse).toList();
    }

    // ── Detalle de una venta ───────────────────────────────────

    @Transactional(readOnly = true)
    public SaleResponse findById(UUID saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        if (!sale.getBarbershop().getId().equals(TenantContext.get()))
            throw new RuntimeException("Acceso denegado");
        return toResponse(sale);
    }

    // ── Cancelar venta ─────────────────────────────────────────

    @Transactional
    public SaleResponse cancel(UUID saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        if (!sale.getBarbershop().getId().equals(TenantContext.get()))
            throw new RuntimeException("Acceso denegado");
        if ("cancelled".equals(sale.getStatus()))
            throw new RuntimeException("La venta ya está cancelada");

        sale.setStatus("cancelled");

        // Devolver stock a los productos
        sale.getItems().stream()
                .filter(i -> "product".equals(i.getItemType()))
                .forEach(i -> productRepository.findById(i.getItemRefId())
                        .ifPresent(p -> {
                            p.setStock(p.getStock() + i.getQuantity());
                            productRepository.save(p);
                        }));

        return toResponse(saleRepository.save(sale));
    }

    // ── Helpers ───────────────────────────────────────────────

    private SaleItem buildItem(SaleItemRequest req, UUID shopId) {
        String name;
        BigDecimal unitPrice;

        if ("service".equals(req.itemType())) {
            HaircutCatalog service = catalogRepository.findById(req.itemRefId())
                    .orElseThrow(() -> new RuntimeException(
                            "Servicio no encontrado: " + req.itemRefId()));
            if (!service.getBarbershop().getId().equals(shopId))
                throw new RuntimeException("Servicio no pertenece a esta barbería");
            name = service.getName();
            unitPrice = service.getPrice();

        } else if ("product".equals(req.itemType())) {
            Product product = productRepository.findById(req.itemRefId())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado: " + req.itemRefId()));
            if (!product.getBarbershop().getId().equals(shopId))
                throw new RuntimeException("Producto no pertenece a esta barbería");
            if (product.getStock() < req.quantity())
                throw new RuntimeException(
                        "Stock insuficiente para: " + product.getName() +
                                " (disponible: " + product.getStock() + ")");

            // Descontar stock al vender
            product.setStock(product.getStock() - req.quantity());
            productRepository.save(product);

            name = product.getName();
            unitPrice = product.getPrice();

        } else {
            throw new RuntimeException("itemType inválido: " + req.itemType());
        }

        int qty = req.quantity() != null ? req.quantity() : 1;
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(qty));

        return SaleItem.builder()
                .itemType(req.itemType())
                .itemRefId(req.itemRefId())
                .itemName(name)
                .unitPrice(unitPrice)
                .quantity(qty)
                .total(total)
                .build();
    }

    private SaleResponse toResponse(Sale s) {
        String clientName = s.getClient() != null ? s.getClient().getFullName() : "Cliente de paso";
        String attendedByUser = s.getAttendedByUser() != null ? s.getAttendedByUser().getFullName() : null;

        List<SaleItemResponse> items = s.getItems().stream()
                .map(i -> new SaleItemResponse(
                        i.getId(), i.getItemType(), i.getItemName(),
                        i.getUnitPrice(), i.getQuantity(), i.getTotal()))
                .toList();

        return new SaleResponse(
                s.getId(),
                s.getClient() != null ? s.getClient().getId() : null,
                clientName,
                attendedByUser,
                s.getPaymentMethod(),
                s.getSubtotal(),
                s.getDiscount(),
                s.getTotal(),
                s.getStatus(),
                items,
                s.getCreatedAt()
        );
    }
}