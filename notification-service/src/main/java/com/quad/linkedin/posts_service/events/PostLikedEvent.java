package com.quad.linkedin.posts_service.events;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostLikedEvent {
    /// notification is sent to post's owner(creator)
    Long postId;
    Long creatorId; /// will get from post entity
    Long likedByUserId;
}
