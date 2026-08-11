package com.gamzenur.whatsappservice.service;

import com.gamzenur.whatsappservice.exception.WhatsAppExceptions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhatsAppStoreResolverTest {

    private static final UUID STORE_ID =
            UUID.fromString("b2c30000-0000-0000-0000-000000000001");

    @Test
    void resolvesStoreIdFromPhoneNumberId() {
        WhatsAppStoreResolver resolver = new WhatsAppStoreResolver(
                "122255556612270=" + STORE_ID
        );

        assertThat(resolver.resolve("122255556612270")).isEqualTo(STORE_ID);
    }

    @Test
    void rejectsUnknownPhoneNumberId() {
        WhatsAppStoreResolver resolver = new WhatsAppStoreResolver(
                "122255556612270=" + STORE_ID
        );

        assertThatThrownBy(() -> resolver.resolve("unknown"))
                .isInstanceOf(WhatsAppExceptions.BadRequest.class)
                .hasMessageContaining("mağaza eşlemesi bulunamadı");
    }
}
