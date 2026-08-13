package com.gamzenur.appointmentservice.service;

import com.gamzenur.appointmentservice.dto.StoreContextResponse;
import com.gamzenur.appointmentservice.entity.Store;
import com.gamzenur.appointmentservice.repository.StoreRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StoreContextServiceTest {

    @Test
    void returnsStoreServicesAndWorkingHours() {
        UUID storeId = UUID.fromString("b2c30000-0000-0000-0000-000000000001");
        Store store = new Store();
        store.setId(storeId);
        store.setName("Bizim Beauty Saloon - Nişantaşı");
        store.setLocation("Nişantaşı, Şişli, İstanbul");
        store.setTimezone("Europe/Istanbul");

        StoreRepository repository = mock(StoreRepository.class);
        when(repository.findById(storeId)).thenReturn(Optional.of(store));

        StoreContextService service = new StoreContextService(repository);
        StoreContextResponse response = service.getContext(storeId);

        assertThat(response.storeId()).isEqualTo(storeId);
        assertThat(response.openingTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(response.closingTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(response.slotDurationMinutes()).isEqualTo(30);
        assertThat(response.services())
                .extracting("code")
                .contains("sac-kesimi", "cilt-bakimi", "manikur");
    }
}
