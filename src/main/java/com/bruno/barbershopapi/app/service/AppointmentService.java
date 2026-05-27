package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.app.domain.entity.*;
import com.bruno.barbershopapi.app.domain.repository.*;
import com.bruno.barbershopapi.app.web.model.appointment.appointment.AppointmentRequest;
import com.bruno.barbershopapi.app.web.model.appointment.appointment.AppointmentResponse;
import com.bruno.barbershopapi.app.web.model.appointment.appointment.AssignBarberRequest;
import com.bruno.barbershopapi.app.web.model.appointment.availability.DayAvailabilityResponse;
import com.bruno.barbershopapi.app.web.model.appointment.availability.TimeSlotResponse;
import com.bruno.barbershopapi.app.web.model.appointment.blocked.BlockedTimeRequest;
import com.bruno.barbershopapi.app.web.model.appointment.schedule.BarberScheduleRequest;
import com.bruno.barbershopapi.app.web.model.appointment.schedule.BarberScheduleResponse;
import com.bruno.barbershopapi.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository       appointmentRepository;
    private final BarberScheduleRepository    scheduleRepository;
    private final BarberBlockedTimeRepository blockedTimeRepository;
    private final BarbershopRepository        barbershopRepository;
    private final UserRepository              userRepository;
    private final ClientRepository            clientRepository;
    private final HaircutCatalogRepository    catalogRepository;
    private final SaleRepository              saleRepository;

    // ══════════════════════════════════════════════════════════
    //  DISPONIBILIDAD
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public DayAvailabilityResponse getAvailability(UUID barbershopId, LocalDate date) {
        barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new RuntimeException("Barbería no encontrada"));

        short dayOfWeek = (short) date.getDayOfWeek().getValue();
        String dayName  = date.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es", "MX"));

        List<BarberSchedule> schedules = scheduleRepository
                .findAllByBarbershopIdAndDayOfWeekAndIsActiveTrue(barbershopId, dayOfWeek);

        if (schedules.isEmpty())
            return new DayAvailabilityResponse(date, dayName, false, List.of());

        List<Appointment> booked = appointmentRepository
                .findActiveByBarbershopAndDate(barbershopId, date);
        List<BarberBlockedTime> blocks = blockedTimeRepository
                .findByBarbershopAndDate(barbershopId, date);

        return new DayAvailabilityResponse(
                date, dayName, true,
                calculateSlots(schedules, booked, blocks));
    }

    private List<TimeSlotResponse> calculateSlots(
            List<BarberSchedule> schedules,
            List<Appointment> booked,
            List<BarberBlockedTime> blocks) {

        List<TimeSlotResponse> result = new ArrayList<>();
        BarberSchedule main = schedules.get(0);
        int slotMin = main.getSlotDuration();
        LocalTime cur = main.getStartTime();
        LocalTime end = main.getEndTime();

        while (cur.plusMinutes(slotMin).compareTo(end) <= 0) {
            LocalTime slotEnd = cur.plusMinutes(slotMin);
            boolean available = isSlotAvailable(cur, slotEnd, schedules, booked, blocks);
            result.add(new TimeSlotResponse(cur, slotEnd, available));
            cur = slotEnd;
        }
        return result;
    }

    private boolean isSlotAvailable(
            LocalTime slotStart, LocalTime slotEnd,
            List<BarberSchedule> schedules,
            List<Appointment> booked,
            List<BarberBlockedTime> blocks) {

        for (BarberSchedule schedule : schedules) {
            UUID barberId = schedule.getUser().getId();

            if (slotStart.isBefore(schedule.getStartTime()) ||
                    slotEnd.isAfter(schedule.getEndTime())) continue;

            boolean blocked = blocks.stream()
                    .filter(b -> b.getUser().getId().equals(barberId))
                    .anyMatch(b -> b.getStartTime() == null ||
                            (b.getStartTime().isBefore(slotEnd) &&
                                    b.getEndTime().isAfter(slotStart)));
            if (blocked) continue;

            boolean occupied = booked.stream()
                    .filter(a -> a.getAssignedTo() != null &&
                            a.getAssignedTo().getId().equals(barberId))
                    .anyMatch(a -> a.getStartTime().isBefore(slotEnd) &&
                            a.getEndTime().isAfter(slotStart));
            if (occupied) continue;

            return true;
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════
    //  AGENDAR CITA
    // ══════════════════════════════════════════════════════════

    @Transactional
    public AppointmentResponse create(UUID barbershopId, AppointmentRequest req) {
        Barbershop shop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new RuntimeException("Barbería no encontrada"));

        if (req.appointmentDate().isBefore(LocalDate.now()))
            throw new RuntimeException("No puedes agendar citas en fechas pasadas");

        short dayOfWeek = (short) req.appointmentDate().getDayOfWeek().getValue();
        List<BarberSchedule> schedules = scheduleRepository
                .findAllByBarbershopIdAndDayOfWeekAndIsActiveTrue(barbershopId, dayOfWeek);

        if (schedules.isEmpty())
            throw new RuntimeException("La barbería no trabaja ese día");

        BarberSchedule schedule = schedules.get(0);

        // Duración: usa el servicio si viene, si no el slot por defecto
        int duration = req.serviceDurationMin() != null
                ? req.serviceDurationMin()
                : schedule.getSlotDuration();

        // ✅ endTime: NO viene en el request, siempre se calcula
        LocalTime endTime = req.startTime().plusMinutes(duration);

        if (req.startTime().isBefore(schedule.getStartTime()) ||
                endTime.isAfter(schedule.getEndTime()))
            throw new RuntimeException("Horario fuera del rango de trabajo");

        User assignedBarber = findAvailableBarber(
                schedules, barbershopId, req.appointmentDate(), req.startTime(), endTime);

        if (assignedBarber == null)
            throw new RuntimeException("No hay barberos disponibles en ese horario");

        Client client = req.clientId() != null
                ? clientRepository.findById(req.clientId()).orElse(null)
                : null;

        HaircutCatalog catalog = req.haircutCatalogId() != null
                ? catalogRepository.findById(req.haircutCatalogId()).orElse(null)
                : null;

        Appointment appt = Appointment.builder()
                .barbershop(shop)
                .client(client)
                .clientName(req.clientName())
                .clientPhone(req.clientPhone())
                .clientNotes(req.clientNotes())
                .assignedTo(assignedBarber)
                .haircutCatalog(catalog)
                .serviceCategoryId(req.serviceCategoryId())
                .serviceVariantId(req.serviceVariantId())
                .serviceName(req.serviceName())
                .servicePrice(req.servicePrice())
                .serviceDurationMin(duration)
                .serviceNotes(req.serviceNotes())
                .appointmentDate(req.appointmentDate())
                .startTime(req.startTime())
                .endTime(endTime)
                .status(Appointment.AppointmentStatus.confirmed)
                .source(Appointment.AppointmentSource.valueOf(
                        req.source() != null ? req.source() : "web"))
                .reminderSent(false)
                .build();

        return toResponse(appointmentRepository.save(appt));
    }

    private User findAvailableBarber(
            List<BarberSchedule> schedules, UUID barbershopId,
            LocalDate date, LocalTime start, LocalTime end) {

        List<BarberBlockedTime> blocks = blockedTimeRepository
                .findByBarbershopAndDate(barbershopId, date);

        for (BarberSchedule schedule : schedules) {
            UUID barberId = schedule.getUser().getId();

            boolean blocked = blocks.stream()
                    .filter(b -> b.getUser().getId().equals(barberId))
                    .anyMatch(b -> b.getStartTime() == null ||
                            (b.getStartTime().isBefore(end) &&
                                    b.getEndTime().isAfter(start)));
            if (blocked) continue;

            if (appointmentRepository.existsConflict(barberId, date, start, end)) continue;

            return schedule.getUser();
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════
    //  GESTIÓN DE CITAS
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByDate(LocalDate date) {
        return appointmentRepository
                .findAllByBarbershopIdAndAppointmentDateOrderByStartTimeAsc(TenantContext.get(), date)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findPending() {
        return appointmentRepository
                .findAllByBarbershopIdAndStatusOrderByAppointmentDateAscStartTimeAsc(
                        TenantContext.get(), "pending")
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findMyAgenda(LocalDate date) {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return appointmentRepository
                .findAllByAssignedToIdAndAppointmentDateOrderByStartTimeAsc(user.getId(), date)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByClient(UUID clientId) {
        return appointmentRepository
                .findAllByBarbershopIdAndClientIdOrderByAppointmentDateDescStartTimeDesc(
                        TenantContext.get(), clientId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public AppointmentResponse assignBarber(UUID appointmentId, AssignBarberRequest req) {
        Appointment appt = findOwned(appointmentId);

        User barber = userRepository.findById(req.barberId())
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));

        if (appointmentRepository.existsConflict(
                req.barberId(), appt.getAppointmentDate(),
                appt.getStartTime(), appt.getEndTime()))
            throw new RuntimeException("El barbero ya tiene una cita en ese horario");

        appt.setAssignedTo(barber);
        appt.setStatus(Appointment.AppointmentStatus.confirmed);
        return toResponse(appointmentRepository.save(appt));
    }

    // ── Cambiar estado — genera venta al completar ────────────
    @Transactional
    public AppointmentResponse updateStatus(UUID appointmentId, String newStatus) {
        Appointment appt = findOwned(appointmentId);

        List<String> valid = List.of(
                "confirmed", "in_progress", "completed", "cancelled", "no_show");
        if (!valid.contains(newStatus))
            throw new RuntimeException("Estado inválido: " + newStatus);

        appt.setStatus(Appointment.AppointmentStatus.valueOf(newStatus));

        // ✅ appt.getSale() porque en la entidad es: private Sale sale (relación)
        if ("completed".equals(newStatus) && appt.getSale() == null) {
            Sale sale = createSaleFromAppointment(appt);
            if (sale != null) {
                appt.setSale(sale);  // ✅ setea la relación, no un UUID
            }
        }

        return toResponse(appointmentRepository.save(appt));
    }

    // ── Crear Sale con cascade ────────────────────────────────
    private Sale createSaleFromAppointment(Appointment appt) {
        BigDecimal price = appt.getServicePrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) return null;

        String itemName = appt.getServiceName() != null
                ? appt.getServiceName()
                : "Servicio de barbería";

        // ✅ Sale usa: attendedByUser (User), no barberId (UUID)
        // ✅ Sale requiere: subtotal, discount, total (según tu entidad)
        SaleItem item = SaleItem.builder()
                .itemType("service")
                .itemRefId(appt.getServiceCategoryId())
                .itemName(itemName)
                .quantity(1)
                .unitPrice(price)
                .total(price)          // ✅ SaleItem tiene "total", no "subtotal"
                .build();

        Sale sale = Sale.builder()
                .barbershop(appt.getBarbershop())
                .client(appt.getClient())
                .attendedByUser(appt.getAssignedTo())   // ✅ relación User, no UUID
                .paymentMethod("cash")
                .status("completed")
                .notes("Auto-generada al completar cita")
                .subtotal(price)       // ✅ Sale requiere subtotal
                .discount(BigDecimal.ZERO)  // ✅ Sale requiere discount
                .total(price)
                .items(new ArrayList<>(List.of(item)))
                .build();

        // Relación bidireccional: cada item conoce su sale
        item.setSale(sale);

        return saleRepository.save(sale);
    }

    // ══════════════════════════════════════════════════════════
    //  HORARIOS
    // ══════════════════════════════════════════════════════════

    @Transactional
    public BarberScheduleResponse saveSchedule(UUID userId, BarberScheduleRequest req) {
        Optional<BarberSchedule> existing = scheduleRepository
                .findByUserIdAndDayOfWeek(userId, req.dayOfWeek());

        BarberSchedule schedule;

        if (existing.isPresent()) {
            schedule = existing.get();
            schedule.setStartTime(req.startTime());
            schedule.setEndTime(req.endTime());

            if (req.slotDuration() != null) {
                schedule.setSlotDuration(req.slotDuration());
            }

            schedule.setIsActive(req.isActive() != null ? req.isActive() : true);

        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Barbershop shop = barbershopRepository.findById(TenantContext.get())
                    .orElseThrow();

            schedule = BarberSchedule.builder()
                    .user(user)
                    .barbershop(shop)
                    .dayOfWeek(req.dayOfWeek())
                    .startTime(req.startTime())
                    .endTime(req.endTime())
                    .slotDuration(req.slotDuration() != null ? req.slotDuration() : 30)
                    .isActive(req.isActive() != null ? req.isActive() : true)
                    .build();
        }

        return toScheduleResponse(scheduleRepository.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<BarberScheduleResponse> getSchedules(UUID userId) {
        return scheduleRepository
                .findAllByUserIdOrderByDayOfWeekAsc(userId)
                .stream().map(this::toScheduleResponse).toList();
    }

    @Transactional
    public void addBlockedTime(UUID userId, BlockedTimeRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Barbershop shop = barbershopRepository.findById(TenantContext.get()).orElseThrow();

        blockedTimeRepository.save(BarberBlockedTime.builder()
                .user(user)
                .barbershop(shop)
                .blockedDate(req.blockedDate())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .reason(req.reason())
                .build());
    }

    // ══════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════

    private Appointment findOwned(UUID id) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        if (!appt.getBarbershop().getId().equals(TenantContext.get()))
            throw new RuntimeException("Acceso denegado");
        return appt;
    }

    private AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getClientName(),
                a.getClientPhone(),
                a.getClientNotes(),
                a.getClient() != null ? a.getClient().getId() : null,
                a.getAssignedTo() != null ? a.getAssignedTo().getFullName() : null,
                a.getAssignedTo() != null ? a.getAssignedTo().getId() : null,
                a.getHaircutCatalog() != null ? a.getHaircutCatalog().getName() : null,
                a.getServiceNotes(),
                a.getServiceCategoryId(),
                a.getServiceVariantId(),
                a.getServiceName(),
                a.getServicePrice(),
                a.getServiceDurationMin(),
                a.getSale() != null ? a.getSale().getId() : null,
                a.getAppointmentDate(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus().name(),
                a.getSource().name(),
                a.getReminderSent(),
                a.getCreatedAt()
        );
    }

    private static final String[] DAY_NAMES = {
            "", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    };

    private BarberScheduleResponse toScheduleResponse(BarberSchedule s) {
        return new BarberScheduleResponse(
                s.getId(),
                s.getDayOfWeek(),
                DAY_NAMES[s.getDayOfWeek()],
                s.getStartTime(),
                s.getEndTime(),
                s.getSlotDuration(),
                s.getIsActive()
        );
    }
}