package com.gamzenur.callservice.exception;

import java.util.UUID;

public class CallNotFoundException extends RuntimeException {

    public CallNotFoundException(UUID id) {
        super("Çağrı bulunamadı: " + id);
    }

    public CallNotFoundException(String roomName) {
        super("LiveKit odasına ait çağrı bulunamadı: " + roomName);
    }
}
