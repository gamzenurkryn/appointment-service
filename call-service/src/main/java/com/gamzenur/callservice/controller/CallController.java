package com.gamzenur.callservice.controller;

import com.gamzenur.callservice.domain.CallStatus;
import com.gamzenur.callservice.dto.CallResponse;
import com.gamzenur.callservice.dto.CreateCallRequest;
import com.gamzenur.callservice.dto.UpdateCallRequest;
import com.gamzenur.callservice.service.CallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calls")
@Tag(name = "Çağrılar", description = "LiveKit teyit çağrılarının yaşam döngüsünü yönetir")
public class CallController {

    private final CallService callService;

    public CallController(CallService callService) {
        this.callService = callService;
    }

    @PostMapping
    @Operation(summary = "Teyit çağrısı başlat", description = "Randevu için LiveKit odası oluşturur ve çağrıyı kuyruğa alır.")
    public ResponseEntity<CallResponse> createCall(@Valid @RequestBody CreateCallRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(callService.createCall(request));
    }

    @GetMapping
    @Operation(summary = "Çağrıları listele", description = "Durum, mağaza, oda adı veya müşteri telefonuna göre filtreler.")
    public List<CallResponse> getCalls(
            @RequestParam(required = false) CallStatus status,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) String q
    ) {
        return callService.getCalls(status, storeId, q);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Çağrı detayını getir")
    public CallResponse getCall(@PathVariable UUID id) {
        return callService.getCall(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Çağrı durumunu güncelle", description = "AI ajanı veya webhook tarafından durum, sonuç ve katılımcı bilgilerini günceller.")
    public CallResponse updateCall(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody UpdateCallRequest request
    ) {
        return callService.updateCall(id, request, correlationId);
    }
}
