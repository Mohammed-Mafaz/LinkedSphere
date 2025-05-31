package com.quad.linkedin.notification_service.consumer;

import com.quad.linkedin.connections_service.event.AcceptConnectionRequestEvent;
import com.quad.linkedin.connections_service.event.SendConnectionRequestEvent;
import com.quad.linkedin.notification_service.service.SendNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConnectionsServiceConsumer {

    private final SendNotification sendNotification;

    @KafkaListener(topics = "send-connection-request-topic")
    public void handleSendConnectionRequest(SendConnectionRequestEvent sendConnectionRequestEvent){
        ///  connection request message is sent to receiver
        String message = "You have got a connection request from the user: %d"+sendConnectionRequestEvent.getSenderId();

        sendNotification.send(sendConnectionRequestEvent.getReceiverId(),message);
    }
    @KafkaListener(topics = "accept-connection-request-topic")
    public void handleAcceptConnectionRequest(AcceptConnectionRequestEvent acceptConnectionRequestEvent){
        ///  accepted message is sent to sender
        String message = "Your connection request has been accepted by the user with id: %d"+acceptConnectionRequestEvent.getReceiverId();

        sendNotification.send(acceptConnectionRequestEvent.getSenderId(),message);
    }
}
