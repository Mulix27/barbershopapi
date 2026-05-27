package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.app.domain.entity.Barbershop;
import com.bruno.barbershopapi.app.domain.entity.Product;
import com.bruno.barbershopapi.app.domain.repository.BarbershopRepository;
import com.bruno.barbershopapi.app.domain.repository.ProductRepository;
import com.bruno.barbershopapi.app.web.model.product.ProductRequest;
import com.bruno.barbershopapi.app.web.model.product.ProductResponse;
import com.bruno.barbershopapi.util.TenantContext;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository    productRepository;
    private final BarbershopRepository barbershopRepository;
    private final Cloudinary cloudinary;

    @Transactional
    public ProductResponse create(ProductRequest req, MultipartFile file) {
        Barbershop shop = barbershopRepository.findById(TenantContext.get()).orElseThrow();

        Product product = Product.builder()
                .barbershop(shop)
                .name(req.name())
                .sku(req.sku())
                .description(req.description())
                .price(req.price())
                .cost(req.cost())
                .stock(req.stock() != null ? req.stock() : 0)
                .stockMin(req.stockMin() != null ? req.stockMin() : 5)
                .isActive(true)
                .build();

        if (file != null && !file.isEmpty()) {
            validateImageFile(file);

            String folder = "barbershop/" + TenantContext.get() + "/products";
            Map<?, ?> uploadResult = uploadToCloudinary(file, folder);

            product.setImageUrl(uploadResult.get("secure_url").toString());
            product.setImagePublicId(uploadResult.get("public_id").toString());
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest req) {
        Product product = findOwned(id);
        product.setName(req.name());
        product.setSku(req.sku());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setCost(req.cost());
        if (req.stockMin() != null) product.setStockMin(req.stockMin());
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository
                .findAllByBarbershopIdAndIsActiveTrueOrderByNameAsc(TenantContext.get())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return toResponse(findOwned(id));
    }

    // Ajuste manual de stock (ej: recibe mercancía)
    @Transactional
    public ProductResponse adjustStock(UUID id, int quantity) {
        Product product = findOwned(id);
        int newStock = product.getStock() + quantity;
        if (newStock < 0) throw new RuntimeException("Stock insuficiente");
        product.setStock(newStock);
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findLowStock() {
        return productRepository
                .findAllByBarbershopIdAndStockLessThanEqualAndIsActiveTrue(
                        TenantContext.get(), 0) // se usa stockMin en el query real
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void toggleActive(UUID id) {
        Product product = findOwned(id);
        product.setIsActive(!product.getIsActive());
        productRepository.save(product);
    }

    private Product findOwned(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        if (!product.getBarbershop().getId().equals(TenantContext.get()))
            throw new RuntimeException("Acceso denegado");
        return product;
    }

    public ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(), p.getName(), p.getSku(),
                p.getPrice(), p.getCost(),
                p.getStock(), p.getStockMin(),
                p.getIsActive(),
                p.getImageUrl(),
                p.getStock() <= p.getStockMin()   // lowStock flag
        );
    }

    @Transactional
    public ProductResponse uploadImage(UUID id, MultipartFile file) {

        Product product = findOwned(id);

        validateImageFile(file);

        String folder = "products/" + TenantContext.get();
        Map<?, ?> uploadResult = uploadToCloudinary(file, folder);
        product.setImageUrl(uploadResult.get("secure_url").toString());
        product.setImagePublicId(uploadResult.get("public_id").toString());

        return toResponse(productRepository.save(product));
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("El archivo es requerido");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Solo se permiten imágenes");
        }

        long maxSize = 10L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("La imagen no puede superar 10MB");
        }
    }

    private Map<?, ?> uploadToCloudinary(MultipartFile file, String folder) {
        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "transformation", "w_800,c_limit,q_85"
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException("Error al subir imagen");
        }
    }
}