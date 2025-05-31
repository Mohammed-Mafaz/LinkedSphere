package com.quad.linkedin.notification_service.consumer;

import com.quad.linkedin.notification_service.clients.ConnectionsFeignClient;
import com.quad.linkedin.notification_service.dto.PersonDto;
import com.quad.linkedin.notification_service.entity.Notification;
import com.quad.linkedin.notification_service.repository.NotificationRepository;
import com.quad.linkedin.notification_service.service.SendNotification;
import com.quad.linkedin.posts_service.events.PostCreatedEvent;
import com.quad.linkedin.posts_service.events.PostLikedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostServiceConsumer {

    private final ConnectionsFeignClient connectionsFeignClient;
    private final NotificationRepository notificationRepository;
    private final SendNotification sendNotification;

    @KafkaListener(topics = "post-created")
    public void handlePostCreated(PostCreatedEvent postCreatedEvent){
        ///  got the post-created event from kafka
        ///  now get all the first degree connections and send message

        log.info("Sending notifications: handlePostCreated: {}", postCreatedEvent);

        List<PersonDto> connections = connectionsFeignClient.getFirstConnections(postCreatedEvent.getCreatorId());

        for (PersonDto connection: connections){
            sendNotification.send(connection.getUserId(), "Your connection " + postCreatedEvent.getCreatorId()+" a post, Check it out");
        }
    }

    @KafkaListener(topics = "post-liked")
    public void handlePostLiked(PostLikedEvent postLikedEvent){
        /// got the post-liked event from kafka
        /// now send this notification to post owner

        log.info("Sending notification: handlePostLiked: {}", postLikedEvent);
        String message = String.format("Your post, %d has been liked by %d",postLikedEvent.getPostId(),
                postLikedEvent.getLikedByUserId());

        sendNotification.send(postLikedEvent.getCreatorId(), message);
    }

}
