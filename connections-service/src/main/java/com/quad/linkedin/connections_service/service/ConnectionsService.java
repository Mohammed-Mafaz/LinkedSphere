package com.quad.linkedin.connections_service.service;

import com.quad.linkedin.connections_service.auth.UserContextHolder;
import com.quad.linkedin.connections_service.entity.Person;
import com.quad.linkedin.connections_service.event.AcceptConnectionRequestEvent;
import com.quad.linkedin.connections_service.event.SendConnectionRequestEvent;
import com.quad.linkedin.connections_service.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ConnectionsService {

    private final PersonRepository personRepository;
    private final KafkaTemplate<Long, SendConnectionRequestEvent> sendRequestKafkaTemplate;
    private final KafkaTemplate<Long, AcceptConnectionRequestEvent> acceptRequestKafkaTemplate;

    public List<Person> getFirstDegreeConnections(Long userId) {
        log.info("Getting first degree connections for user with id: {}", userId);

        return personRepository.getFirstDegreeConnections(userId);
    }

    public Boolean sendConnectionRequest(Long receiverId) {
        Long senderId = UserContextHolder.getCurrentUserId();
        log.info("Trying to send connection request, sender: {}, receiver: {}",senderId, receiverId);

        if (senderId.equals(receiverId)){
            throw new RuntimeException("Sender and Receiver cannot not be same");
        }

        boolean alreadySentRequest = personRepository.alreadyConnected(senderId,receiverId);
        if(alreadySentRequest){
            log.info("The user is already connected to the person with id : {} ",receiverId);
            throw new RuntimeException("The user is already connected to the user with id: " + receiverId);
        }
        boolean connectionRequestExists = personRepository.connectionRequestExists(senderId,receiverId);
        if (connectionRequestExists){
            log.info("The user has already sent connection request to person with id : {} ",receiverId);
            throw new RuntimeException("The user has already sent connection request to the user with id: " + receiverId);
        }

        personRepository.addConnectionRequest(senderId,receiverId);
        log.info("Successfully sent the connection request, sender:{}, receiver:{}",senderId,receiverId);
        SendConnectionRequestEvent connectionRequestEvent = SendConnectionRequestEvent.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .build();
        sendRequestKafkaTemplate.send("send-connection-request-topic",connectionRequestEvent);
        return true;
    }

    public Boolean addConnectionRequest(Long senderId) {
        Long receiverId = UserContextHolder.getCurrentUserId();
        log.info("Trying to accept connection request, sender: {}, receiver: {}", senderId, receiverId);

        if (senderId.equals(receiverId)) {
            throw new RuntimeException("Sender and Receiver cannot not be same");
        }

        boolean alreadyConnected = personRepository.alreadyConnected(senderId, receiverId);
        if (alreadyConnected) {
            throw new RuntimeException("Connection already exits");
        }

        boolean connectionRequestExists = personRepository.connectionRequestExists(senderId, receiverId);
        if (!connectionRequestExists) {
            log.info("The user has got no connection request from the person with id : {} to accept", senderId);
            throw new RuntimeException("The user has got no connection request from the person with id : {} , to accept"+ senderId);
        }

        ///  acceptConnectionRequest() also handles the deletion of REQUESTED-TO edge
        personRepository.acceptConnectionRequest(senderId,receiverId);
        log.info("Successfully accepted the connection request from user with id: {}",senderId);

        AcceptConnectionRequestEvent acceptConnectionRequestEvent = AcceptConnectionRequestEvent.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .build();
        acceptRequestKafkaTemplate.send("accept-connection-request-topic",acceptConnectionRequestEvent);
        return true;
    }

    public Boolean rejectConnectionRequest(Long senderId) {
        Long receiverId = UserContextHolder.getCurrentUserId();
        log.info("Proceeding with rejecting the connection request, sender:{}, receiver:{}",senderId,receiverId);

        if (senderId.equals(receiverId)) {
            throw new RuntimeException("Sender and Receiver cannot not be same");
        }

        boolean connectionRequestExists = personRepository.connectionRequestExists(senderId, receiverId);
        if (!connectionRequestExists) {
            log.info("The user has got no connection request from the person with id : {} , to reject", senderId);
            throw new RuntimeException("The user has got no connection request from the person with id : {} to reject"+ senderId);
        }

        personRepository.rejectConnectionRequest(senderId,receiverId);
        log.info("Successfully reject the connection request from the user: {}", senderId);
        return true;
    }
}
