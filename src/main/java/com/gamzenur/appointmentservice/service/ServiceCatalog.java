package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.exception.BadRequestException;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class ServiceCatalog {

    private static final Map<String, String> SERVICES = createServices();

    private ServiceCatalog() {
    }

    public static void validate(String serviceType) {
        if (!SERVICES.containsKey(serviceType)) {
            throw new BadRequestException(
                    "Desteklenmeyen hizmet türü: " + serviceType
                            + ". Geçerli değerler: " + String.join(", ", SERVICES.keySet())
            );
        }
    }

    public static String displayName(String serviceType) {
        return SERVICES.getOrDefault(serviceType, serviceType);
    }

    public static Set<String> supportedTypes() {
        return SERVICES.keySet();
    }

    private static Map<String, String> createServices() {
        Map<String, String> services = new LinkedHashMap<>();
        services.put("sac-kesimi", "Saç Kesimi");
        services.put("sac-boyama", "Saç Boyama");
        services.put("sac-bakimi", "Saç Bakımı");
        services.put("cilt-bakimi", "Cilt Bakımı");
        services.put("manikur", "Manikür");
        services.put("pedikur", "Pedikür");
        services.put("kas-tasarimi", "Kaş Tasarımı");
        services.put("danismanlik", "Danışmanlık");
        return Collections.unmodifiableMap(services);
    }
}
