package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.exception.BadRequestException;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ServiceCatalog {

    private static final Map<String, ServiceDefinition> SERVICES = createServices();

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
        ServiceDefinition definition = SERVICES.get(serviceType);
        return definition == null ? serviceType : definition.displayName();
    }

    public static Duration duration(String serviceType) {
        validate(serviceType);
        return SERVICES.get(serviceType).duration();
    }

    public static Set<String> supportedTypes() {
        return SERVICES.keySet();
    }

    private static Map<String, ServiceDefinition> createServices() {
        Map<String, ServiceDefinition> services = new LinkedHashMap<>();

        services.put(
                "sac-kesimi",
                new ServiceDefinition("Saç Kesimi", Duration.ofMinutes(30))
        );
        services.put(
                "sac-boyama",
                new ServiceDefinition("Saç Boyama", Duration.ofMinutes(90))
        );
        services.put(
                "sac-bakimi",
                new ServiceDefinition("Saç Bakımı", Duration.ofMinutes(60))
        );
        services.put(
                "cilt-bakimi",
                new ServiceDefinition("Cilt Bakımı", Duration.ofMinutes(60))
        );
        services.put(
                "manikur",
                new ServiceDefinition("Manikür", Duration.ofMinutes(45))
        );
        services.put(
                "pedikur",
                new ServiceDefinition("Pedikür", Duration.ofMinutes(60))
        );
        services.put(
                "kas-tasarimi",
                new ServiceDefinition("Kaş Tasarımı", Duration.ofMinutes(30))
        );
        services.put(
                "danismanlik",
                new ServiceDefinition("Danışmanlık", Duration.ofMinutes(30))
        );

        return Collections.unmodifiableMap(services);
    }

    private record ServiceDefinition(
            String displayName,
            Duration duration
    ) {
    }
}