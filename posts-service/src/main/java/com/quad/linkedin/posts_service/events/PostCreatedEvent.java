package com.quad.linkedin.posts_service.events;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostCreatedEvent {
    Long postId;
    String content;
    Long creatorId;
}
