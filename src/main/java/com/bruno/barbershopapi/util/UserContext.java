package com.bruno.barbershopapi.util;

import java.util.UUID;

public class UserContext {

    private static final ThreadLocal<UUID>   userId = new ThreadLocal<>();
    private static final ThreadLocal<String> role   = new ThreadLocal<>();

    public static void setUserId(UUID id) { userId.set(id); }
    public static UUID getUserId()        { return userId.get(); }

    public static void setRole(String r)  { role.set(r); }
    public static String getRole()        { return role.get(); }

    public static void clear() {
        userId.remove();
        role.remove();
    }
}