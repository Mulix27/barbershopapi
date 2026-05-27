package com.bruno.barbershopapi.util;

import java.util.UUID;

/**
 * Almacena el barbershopId del usuario autenticado en un ThreadLocal.
 * Se inicializa en JwtFilter y se usa en los servicios para filtrar
 * datos automáticamente por tenant.
 *
 * Uso en cualquier servicio:
 *   UUID tenantId = TenantContext.get();
 *   clientRepository.findAllByBarbershopId(tenantId);
 */
public class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    public static void set(UUID barbershopId) {
        CURRENT_TENANT.set(barbershopId);
    }

    public static UUID get() {
        UUID id = CURRENT_TENANT.get();
        if (id == null) {
            throw new IllegalStateException(
                    "No hay tenant en el contexto. ¿El endpoint requiere autenticación?");
        }
        return id;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}