package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.app.domain.entity.*;
import com.bruno.barbershopapi.app.domain.repository.*;
import com.bruno.barbershopapi.app.web.model.*;
import com.bruno.barbershopapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository         userRepository;
    private final BarbershopRepository   barbershopRepository;
    private final PlanRepository         planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder        passwordEncoder;
    private final JwtUtil                jwtUtil;

    // ── Login ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository
                .findAll()
                .stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(request.email()))
                .filter(User::getIsActive)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        Barbershop shop = user.getBarbershop();

        if (!shop.getIsActive()) {
            throw new RuntimeException("La barbería está inactiva");
        }

        // ✅ .name() convierte el enum Role a String ("owner", "barber", "cashier")
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole(),
                shop.getId()
        );

        return new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),   // ✅ enum → String
                shop.getId(),
                shop.getName(),
                shop.getLogoUrl(),
                shop.getSingleBarber()
        );
    }

    // ── Registro de nueva barbería ─────────────────────────────

    @Transactional
    public LoginResponse register(RegisterRequest request) {

        if (barbershopRepository.existsBySlug(request.slug())) {
            throw new RuntimeException("El slug '" + request.slug() + "' ya está en uso");
        }

        // ✅ findById viene de JpaRepository<Plan, UUID>
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        Barbershop shop = Barbershop.builder()
                .name(request.barbershopName())
                .slug(request.slug())
                .phone(request.phone())
                .city(request.city())
                .singleBarber(request.singleBarber() != null ? request.singleBarber() : false)
                .isActive(true)
                .build();
        shop = barbershopRepository.save(shop);

        User owner = User.builder()
                .barbershop(shop)
                .fullName(request.ownerFullName())
                .email(request.ownerEmail())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("owner")
                .isActive(true)
                .build();
        owner = userRepository.save(owner);

        // ✅ save viene de JpaRepository<Subscription, UUID>
        Subscription subscription = Subscription.builder()
                .barbershop(shop)
                .plan(plan)
                .status("trialing")
                .currentPeriodStart(LocalDate.now())
                .currentPeriodEnd(LocalDate.now().plusDays(14))
                .build();
        subscriptionRepository.save(subscription);

        String token = jwtUtil.generateToken(
                owner.getEmail(),
                owner.getRole(),  // ✅ enum → String
                shop.getId()
        );

        return new LoginResponse(
                token,
                "Bearer",
                owner.getId(),
                owner.getFullName(),
                owner.getEmail(),
                owner.getRole(),  // ✅ enum → String
                shop.getId(),
                shop.getName(),
                shop.getLogoUrl(),
                shop.getSingleBarber()
        );
    }
}