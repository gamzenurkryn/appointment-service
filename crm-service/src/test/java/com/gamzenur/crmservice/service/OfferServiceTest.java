package com.gamzenur.crmservice.service;

import com.gamzenur.crmservice.domain.Offer;
import com.gamzenur.crmservice.repository.OfferRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OfferServiceTest {

    @Test
    void createsHairCareUpsellForHaircutAppointment() {
        OfferRepository repository = mock(OfferRepository.class);
        UUID appointmentId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        when(repository.existsByAppointmentId(appointmentId)).thenReturn(false);
        OfferService service = new OfferService(repository);

        service.createForAppointment(appointmentId, storeId, "sac-kesimi", "correlation-1");

        ArgumentCaptor<Offer> captor = ArgumentCaptor.forClass(Offer.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOfferedServiceType()).isEqualTo("sac-bakimi");
        assertThat(captor.getValue().getDiscountPercent()).isEqualTo(15);
        assertThat(captor.getValue().getCorrelationId()).isEqualTo("correlation-1");
    }
}
