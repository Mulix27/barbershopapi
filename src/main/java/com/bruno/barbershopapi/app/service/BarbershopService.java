package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.app.domain.entity.Barbershop;
import com.bruno.barbershopapi.app.domain.repository.BarbershopRepository;
import com.bruno.barbershopapi.app.web.model.barbershop.BarbershopResponse;
import com.bruno.barbershopapi.app.web.model.barbershop.BarbershopUpdateRequest;
import com.bruno.barbershopapi.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BarbershopService {

    private final BarbershopRepository barbershopRepository;
    private final PhotoService photoService;

    @Transactional(readOnly = true)
    public BarbershopResponse getCurrentBarbershop() {
        Barbershop barbershop = getCurrentEntity();
        return toResponse(barbershop);
    }

    @Transactional
    public BarbershopResponse updateCurrentBarbershop(BarbershopUpdateRequest request) {
        Barbershop barbershop = getCurrentEntity();

        if (request.name() != null) {
            barbershop.setName(request.name());
        }

        if (request.phone() != null) {
            barbershop.setPhone(request.phone());
        }

        if (request.email() != null) {
            barbershop.setEmail(request.email());
        }

        if (request.address() != null) {
            barbershop.setAddress(request.address());
        }

        if (request.city() != null) {
            barbershop.setCity(request.city());
        }

        if (request.primaryColor() != null) {
            barbershop.setPrimaryColor(request.primaryColor());
        }

        if (request.subdomain() != null) {
            barbershop.setSubdomain(request.subdomain());
        }

        if (request.singleBarber() != null) {
            barbershop.setSingleBarber(request.singleBarber());
        }

        return toResponse(barbershopRepository.save(barbershop));
    }

    @Transactional
    public BarbershopResponse updateLogoUrl(String logoUrl) {
        Barbershop barbershop = getCurrentEntity();
        barbershop.setLogoUrl(logoUrl);
        return toResponse(barbershopRepository.save(barbershop));
    }

    private Barbershop getCurrentEntity() {
        return barbershopRepository.findById(TenantContext.get())
                .orElseThrow(() -> new RuntimeException("Barbería no encontrada"));
    }

    private BarbershopResponse toResponse(Barbershop b) {
        return new BarbershopResponse(
                b.getId(),
                b.getName(),
                b.getSlug(),
                b.getPhone(),
                b.getEmail(),
                b.getAddress(),
                b.getCity(),
                b.getLogoUrl(),
                b.getPrimaryColor(),
                b.getSubdomain(),
                b.getIsActive(),
                b.getSingleBarber(),
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }

    @Transactional
    public BarbershopResponse uploadLogo(MultipartFile file) {

        Barbershop barbershop = getCurrentEntity();

        photoService.validateImageFile(file);

        String folder = barbershop.getId() + "/branding";

        Map<?, ?> result = photoService.uploadToCloudinary(file, folder);

        barbershop.setLogoUrl(
                result.get("secure_url").toString()
        );

        return toResponse(
                barbershopRepository.save(barbershop)
        );
    }
}