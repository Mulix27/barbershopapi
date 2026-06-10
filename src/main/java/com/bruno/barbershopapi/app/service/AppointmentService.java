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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BarberScheduleRepository scheduleRepository;
    private final BarberBlockedTimeRepository blockedTimeRepository;
    private final BarbershopRepository barbershopRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final HaircutCatalogRepository catalogRepository;
    private final SaleRepository saleRepository;

    private static final ZoneId BARBERSHOP_ZONE = ZoneId.of("America/Merida");

    // ══════════════════════════════════════════════════════════
    //  DISPONIBILIDAD
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public DayAvailabilityResponse getAvailability(
            UUID barbershopId,
            LocalDate date,
            Integer durationMin
    ) {
        barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new RuntimeException("Barbería no encontrada"));

        short dayOfWeek = (short) date.getDayOfWeek().getValue();

        String dayName = date.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es", "MX"));

        List<BarberSchedule> schedules = scheduleRepository
                .findAllByBarbershopIdAndDayOfWeekAndIsActiveTrue(barbershopId, dayOfWeek);

        if (schedules.isEmpty()) {
            return new DayAvailabilityResponse(date, dayName, false, List.of());
        }

        List<Appointment> booked = appointmentRepository
                .findActiveByBarbershopAndDate(barbershopId, date);

        List<BarberBlockedTime> blocks = blockedTimeRepository
                .findByBarbershopAndDate(barbershopId, date);

        List<TimeSlotResponse> slots = calculateSlots(
                date,
                durationMin,
                schedules,
                booked,
                blocks
        );

        return new DayAvailabilityResponse(date, dayName, true, slots);
    }

    private List<TimeSlotResponse> calculateSlots(
            LocalDate date,
            Integer durationMin,
            List<BarberSchedule> schedules,
            List<Appointment> booked,
            List<BarberBlockedTime> blocks
    ) {
        List<TimeSlotResponse> result = new ArrayList<>();

        if (schedules == null || schedules.isEmpty()) {
            return result;
        }

        int slotStep = schedules.stream()
                .map(BarberSchedule::getSlotDuration)
                .filter(Objects::nonNull)
                .mapToInt(Short::intValue)
                .min()
                .orElse(30);

        int realDuration = durationMin != null && durationMin > 0
                ? durationMin
                : slotStep;

        LocalTime dayStart = schedules.stream()
                .map(BarberSchedule::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalTime::compareTo)
                .orElse(LocalTime.of(9, 0));

        LocalTime dayEnd = schedules.stream()
                .map(BarberSchedule::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalTime::compareTo)
                .orElse(LocalTime.of(18, 0));

        LocalTime current = dayStart;

        while (!current.plusMinutes(realDuration).isAfter(dayEnd)) {
            LocalTime slotStart = current;
            LocalTime slotEnd = slotStart.plusMinutes(realDuration);

            boolean pastSlot = isPastSlot(date, slotStart);

            boolean available = !pastSlot && isSlotAvailable(
                    slotStart,
                    slotEnd,
                    schedules,
                    booked,
                    blocks
            );

            result.add(new TimeSlotResponse(slotStart, slotEnd, available));

            current = current.plusMinutes(slotStep);
        }

        return result;
    }

    private boolean isSlotAvailable(
            LocalTime slotStart,
            LocalTime slotEnd,
            List<BarberSchedule> schedules,
            List<Appointment> booked,
            List<BarberBlockedTime> blocks
    ) {
        for (BarberSchedule schedule : schedules) {
            UUID barberId = schedule.getUser().getId();

            boolean fitsSchedule =
                    !slotStart.isBefore(schedule.getStartTime()) &&
                            !slotEnd.isAfter(schedule.getEndTime());

            if (!fitsSchedule) {
                continue;
            }

            boolean blocked = blocks.stream()
                    .filter(block -> block.getUser().getId().equals(barberId))
                    .anyMatch(block -> isBlockedDuring(block, slotStart, slotEnd));

            if (blocked) {
                continue;
            }

            boolean occupied = booked.stream()
                    .filter(appointment -> appointment.getAssignedTo() != null)
                    .filter(appointment -> appointment.getAssignedTo().getId().equals(barberId))
                    .anyMatch(appointment ->
                            appointment.getStartTime().isBefore(slotEnd) &&
                                    appointment.getEndTime().isAfter(slotStart)
                    );

            if (occupied) {
                continue;
            }

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

        validateNotPastAppointment(req.appointmentDate(), req.startTime());

        short dayOfWeek = (short) req.appointmentDate().getDayOfWeek().getValue();

        List<BarberSchedule> schedules = scheduleRepository
                .findAllByBarbershopIdAndDayOfWeekAndIsActiveTrue(barbershopId, dayOfWeek);

        if (schedules.isEmpty()) {
            throw new RuntimeException("La barbería no trabaja ese día");
        }

        BarberSchedule defaultSchedule = schedules.get(0);

        int duration = req.serviceDurationMin() != null && req.serviceDurationMin() > 0
                ? req.serviceDurationMin()
                : defaultSchedule.getSlotDuration();

        LocalTime endTime = req.startTime().plusMinutes(duration);

        User assignedBarber;

        if (req.assignedToId() != null) {
            assignedBarber = userRepository.findById(req.assignedToId())
                    .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));

            if (!assignedBarber.getBarbershop().getId().equals(barbershopId)) {
                throw new RuntimeException("El barbero no pertenece a esta barbería");
            }

            BarberSchedule barberSchedule = schedules.stream()
                    .filter(schedule -> schedule.getUser().getId().equals(req.assignedToId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("El barbero seleccionado no trabaja ese día"));

            validateSlotInsideSchedule(req.startTime(), endTime, barberSchedule);

            boolean blocked = blockedTimeRepository
                    .findByBarbershopAndDate(barbershopId, req.appointmentDate())
                    .stream()
                    .filter(block -> block.getUser().getId().equals(req.assignedToId()))
                    .anyMatch(block -> isBlockedDuring(block, req.startTime(), endTime));

            if (blocked) {
                throw new RuntimeException("El barbero seleccionado tiene bloqueado ese horario");
            }

            if (appointmentRepository.existsConflict(
                    req.assignedToId(),
                    req.appointmentDate(),
                    req.startTime(),
                    endTime
            )) {
                throw new RuntimeException("El barbero seleccionado ya tiene una cita en ese horario");
            }

        } else {
            assignedBarber = findAvailableBarber(
                    schedules,
                    barbershopId,
                    req.appointmentDate(),
                    req.startTime(),
                    endTime
            );

            if (assignedBarber == null) {
                throw new RuntimeException("No hay barberos disponibles en ese horario");
            }
        }

        Client client = resolveClientForAppointment(shop, req);

        HaircutCatalog catalog = req.haircutCatalogId() != null
                ? catalogRepository.findById(req.haircutCatalogId()).orElse(null)
                : null;

        Appointment appointment = Appointment.builder()
                .barbershop(shop)
                .client(client)
                .clientName(client != null ? client.getFullName() : req.clientName())
                .clientPhone(client != null ? client.getPhone() : req.clientPhone())
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
                        req.source() != null ? req.source() : "web"
                ))
                .reminderSent(false)
                .build();

        return toResponse(appointmentRepository.save(appointment));
    }

    private User findAvailableBarber(
            List<BarberSchedule> schedules,
            UUID barbershopId,
            LocalDate date,
            LocalTime start,
            LocalTime end
    ) {
        List<BarberBlockedTime> blocks = blockedTimeRepository
                .findByBarbershopAndDate(barbershopId, date);

        for (BarberSchedule schedule : schedules) {
            UUID barberId = schedule.getUser().getId();

            boolean fitsSchedule =
                    !start.isBefore(schedule.getStartTime()) &&
                            !end.isAfter(schedule.getEndTime());

            if (!fitsSchedule) {
                continue;
            }

            boolean blocked = blocks.stream()
                    .filter(block -> block.getUser().getId().equals(barberId))
                    .anyMatch(block -> isBlockedDuring(block, start, end));

            if (blocked) {
                continue;
            }

            if (appointmentRepository.existsConflict(barberId, date, start, end)) {
                continue;
            }

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
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findPending() {
        return appointmentRepository
                .findAllByBarbershopIdAndStatusOrderByAppointmentDateAscStartTimeAsc(
                        TenantContext.get(),
                        "pending"
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findMyAgenda(LocalDate date) {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findAll()
                .stream()
                .filter(currentUser -> currentUser.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return appointmentRepository
                .findAllByAssignedToIdAndAppointmentDateOrderByStartTimeAsc(user.getId(), date)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByClient(UUID clientId) {
        return appointmentRepository
                .findAllByBarbershopIdAndClientIdOrderByAppointmentDateDescStartTimeDesc(
                        TenantContext.get(),
                        clientId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AppointmentResponse assignBarber(UUID appointmentId, AssignBarberRequest req) {
        Appointment appointment = findOwned(appointmentId);

        User barber = userRepository.findById(req.barberId())
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));

        if (!barber.getBarbershop().getId().equals(TenantContext.get())) {
            throw new RuntimeException("El barbero no pertenece a esta barbería");
        }

        if (appointmentRepository.existsConflict(
                req.barberId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime()
        )) {
            throw new RuntimeException("El barbero ya tiene una cita en ese horario");
        }

        appointment.setAssignedTo(barber);
        appointment.setStatus(Appointment.AppointmentStatus.confirmed);

        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse updateStatus(UUID appointmentId, String newStatus) {
        Appointment appointment = findOwned(appointmentId);

        List<String> validStatuses = List.of(
                "confirmed",
                "in_progress",
                "completed",
                "cancelled",
                "no_show"
        );

        if (!validStatuses.contains(newStatus)) {
            throw new RuntimeException("Estado inválido: " + newStatus);
        }

        appointment.setStatus(Appointment.AppointmentStatus.valueOf(newStatus));

        if ("completed".equals(newStatus) && appointment.getSale() == null) {
            Sale sale = createSaleFromAppointment(appointment);

            if (sale != null) {
                appointment.setSale(sale);
            }
        }

        return toResponse(appointmentRepository.save(appointment));
    }

    private Sale createSaleFromAppointment(Appointment appointment) {
        BigDecimal price = appointment.getServicePrice();

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        String itemName = appointment.getServiceName() != null
                ? appointment.getServiceName()
                : "Servicio de barbería";

        SaleItem item = SaleItem.builder()
                .itemType("service")
                .itemRefId(appointment.getServiceCategoryId())
                .itemName(itemName)
                .quantity(1)
                .unitPrice(price)
                .total(price)
                .build();

        Sale sale = Sale.builder()
                .barbershop(appointment.getBarbershop())
                .client(appointment.getClient())
                .attendedByUser(appointment.getAssignedTo())
                .paymentMethod("cash")
                .status("completed")
                .notes("Auto-generada al completar cita")
                .subtotal(price)
                .discount(BigDecimal.ZERO)
                .total(price)
                .items(new ArrayList<>(List.of(item)))
                .build();

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
                .stream()
                .map(this::toScheduleResponse)
                .toList();
    }

    @Transactional
    public void addBlockedTime(UUID userId, BlockedTimeRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Barbershop shop = barbershopRepository.findById(TenantContext.get())
                .orElseThrow();

        blockedTimeRepository.save(
                BarberBlockedTime.builder()
                        .user(user)
                        .barbershop(shop)
                        .blockedDate(req.blockedDate())
                        .startTime(req.startTime())
                        .endTime(req.endTime())
                        .reason(req.reason())
                        .build()
        );
    }

    // ══════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════

    private Appointment findOwned(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (!appointment.getBarbershop().getId().equals(TenantContext.get())) {
            throw new RuntimeException("Acceso denegado");
        }

        return appointment;
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getClientName(),
                appointment.getClientPhone(),
                appointment.getClientNotes(),
                appointment.getClient() != null ? appointment.getClient().getId() : null,
                appointment.getAssignedTo() != null ? appointment.getAssignedTo().getFullName() : null,
                appointment.getAssignedTo() != null ? appointment.getAssignedTo().getId() : null,
                appointment.getHaircutCatalog() != null ? appointment.getHaircutCatalog().getName() : null,
                appointment.getServiceNotes(),
                appointment.getServiceCategoryId(),
                appointment.getServiceVariantId(),
                appointment.getServiceName(),
                appointment.getServicePrice(),
                appointment.getServiceDurationMin(),
                appointment.getSale() != null ? appointment.getSale().getId() : null,
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus().name(),
                appointment.getSource().name(),
                appointment.getReminderSent(),
                appointment.getCreatedAt()
        );
    }

    private Client resolveClientForAppointment(Barbershop shop, AppointmentRequest req) {
        if (req.clientId() != null) {
            Client client = clientRepository.findById(req.clientId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            if (!client.getBarbershop().getId().equals(shop.getId())) {
                throw new RuntimeException("El cliente no pertenece a esta barbería");
            }

            return client;
        }

        String phoneLast10 = getLast10Digits(req.clientPhone());

        if (phoneLast10.length() == 10) {
            Optional<Client> existingClient = clientRepository.findByBarbershopIdAndPhoneLast10(
                    shop.getId(),
                    phoneLast10
            );

            if (existingClient.isPresent()) {
                Client client = existingClient.get();

                if ((client.getFullName() == null || client.getFullName().isBlank())
                        && req.clientName() != null
                        && !req.clientName().isBlank()) {
                    client.setFullName(req.clientName().trim());
                    clientRepository.save(client);
                }

                return client;
            }
        }

        Client newClient = Client.builder()
                .barbershop(shop)
                .fullName(req.clientName() != null ? req.clientName().trim() : "Cliente")
                .phone(normalizeMexicanPhone(req.clientPhone()))
                .notes(req.clientNotes())
                .isActive(true)
                .build();

        return clientRepository.save(newClient);
    }

    private static final String[] DAY_NAMES = {
            "",
            "Lunes",
            "Martes",
            "Miércoles",
            "Jueves",
            "Viernes",
            "Sábado",
            "Domingo"
    };

    private BarberScheduleResponse toScheduleResponse(BarberSchedule schedule) {
        return new BarberScheduleResponse(
                schedule.getId(),
                schedule.getDayOfWeek(),
                DAY_NAMES[schedule.getDayOfWeek()],
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getSlotDuration(),
                schedule.getIsActive()
        );
    }

    private void validateSlotInsideSchedule(
            LocalTime start,
            LocalTime end,
            BarberSchedule schedule
    ) {
        if (start.isBefore(schedule.getStartTime()) || end.isAfter(schedule.getEndTime())) {
            throw new RuntimeException("Horario fuera del rango de trabajo");
        }
    }

    private void validateNotPastAppointment(LocalDate appointmentDate, LocalTime startTime) {
        if (isPastSlot(appointmentDate, startTime)) {
            throw new RuntimeException("No puedes reservar un horario vencido.");
        }
    }

    private boolean isPastSlot(LocalDate appointmentDate, LocalTime startTime) {
        ZonedDateTime now = ZonedDateTime.now(BARBERSHOP_ZONE);

        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime()
                .withSecond(0)
                .withNano(0);

        if (appointmentDate.isBefore(today)) {
            return true;
        }

        if (appointmentDate.isAfter(today)) {
            return false;
        }

        return !startTime.isAfter(currentTime);
    }

    private boolean isBlockedDuring(
            BarberBlockedTime block,
            LocalTime start,
            LocalTime end
    ) {
        if (block.getStartTime() == null || block.getEndTime() == null) {
            return true;
        }

        return block.getStartTime().isBefore(end) &&
                block.getEndTime().isAfter(start);
    }

    private String getLast10Digits(String phone) {
        if (phone == null) {
            return "";
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.length() <= 10) {
            return digits;
        }

        return digits.substring(digits.length() - 10);
    }

    private String normalizeMexicanPhone(String phone) {
        String last10 = getLast10Digits(phone);

        if (last10.length() != 10) {
            return phone;
        }

        return "52" + last10;
    }
}