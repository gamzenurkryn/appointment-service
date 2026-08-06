package com.gamzenur.callservice.messaging;

import com.gamzenur.callservice.dto.CallResponse;

public interface CallEventPublisher {

    void publish(String eventType, CallResponse call);
}
