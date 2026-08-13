package com.gamzenur.crmservice.service;

import com.gamzenur.crmservice.domain.Offer;
import com.gamzenur.crmservice.domain.OfferStatus;
import com.gamzenur.crmservice.dto.OfferResponse;
import com.gamzenur.crmservice.repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OfferService {

    private final OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    @Transactional
    public void createForAppointment(
            UUID appointmentId,
            UUID storeId,
            String serviceType,
            String correlationId
    ) {
        if (offerRepository.existsByAppointmentId(appointmentId)) {
            return;
        }

        OfferSuggestion suggestion = suggestionFor(serviceType);
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setAppointmentId(appointmentId);
        offer.setStoreId(storeId);
        offer.setSourceServiceType(serviceType);
        offer.setOfferedServiceType(suggestion.serviceType());
        offer.setTitle(suggestion.title());
        offer.setDiscountPercent(suggestion.discountPercent());
        offer.setStatus(OfferStatus.CREATED);
        offer.setCorrelationId(correlationId);
        offer.setCreatedAt(OffsetDateTime.now());
        offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public List<OfferResponse> getOffers(UUID appointmentId) {
        List<Offer> offers = appointmentId == null
                ? offerRepository.findAll()
                : offerRepository.findAllByAppointmentIdOrderByCreatedAtDesc(appointmentId);
        return offers.stream().map(this::toResponse).toList();
    }

    private OfferSuggestion suggestionFor(String serviceType) {
        return switch (serviceType) {
            case "sac-kesimi", "sac-boyama" ->
                    new OfferSuggestion("sac-bakimi", "Saç bakımında %15 indirim", 15);
            case "manikur" ->
                    new OfferSuggestion("pedikur", "Pedikür hizmetinde %10 indirim", 10);
            case "cilt-bakimi" ->
                    new OfferSuggestion("kas-tasarimi", "Kaş tasarımında %10 indirim", 10);
            default ->
                    new OfferSuggestion("danismanlik", "Bakım danışmanlığı ücretsiz", 100);
        };
    }

    private OfferResponse toResponse(Offer offer) {
        return new OfferResponse(
                offer.getId(), offer.getAppointmentId(), offer.getStoreId(),
                offer.getSourceServiceType(), offer.getOfferedServiceType(), offer.getTitle(),
                offer.getDiscountPercent(), offer.getStatus(), offer.getCorrelationId(), offer.getCreatedAt()
        );
    }

    private record OfferSuggestion(String serviceType, String title, int discountPercent) {
    }
}
