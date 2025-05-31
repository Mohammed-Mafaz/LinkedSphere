package com.quad.linkedin.posts_service.service;

import com.quad.linkedin.posts_service.auth.UserContextHolder;
import com.quad.linkedin.posts_service.entity.Post;
import com.quad.linkedin.posts_service.entity.PostLike;
import com.quad.linkedin.posts_service.events.PostLikedEvent;
import com.quad.linkedin.posts_service.exception.BadRequestException;
import com.quad.linkedin.posts_service.exception.ResourceNotFoundException;
import com.quad.linkedin.posts_service.repository.PostLikeRepository;
import com.quad.linkedin.posts_service.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostsRepository postsRepository;
    private final KafkaTemplate<Long, PostLikedEvent> kafkaTemplate;

    private final String PostLikedTopic ="post-liked";

    public void likePost(Long postId) {
        log.info("Attempting to like the post with id: {}", postId);

        Post post = postsRepository.findById(postId).orElseThrow(
                () -> new ResourceNotFoundException("Post not found with id: "+postId)
        );

        Long userId = UserContextHolder.getCurrentUserId();

        boolean alreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(alreadyLiked) throw new BadRequestException("Cannot like the same post again.");

        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        postLikeRepository.save(postLike);
        log.info("Post with id: {} liked successfully", postId);

        ///  Sending Liked notification
        PostLikedEvent postLikedEvent = PostLikedEvent.builder()
                        .postId(postId)
                                .likedByUserId(userId)
                                        .creatorId(post.getUserId())
                                                .build();
        log.info("Sending liked notification to post owner");
        kafkaTemplate.send(PostLikedTopic,postId,postLikedEvent);
    }

    public void unlikePost(Long postId) {
        log.info("Attempting to unlike the post with id: {}", postId);
        boolean exists = postsRepository.existsById(postId);
        if(!exists) throw new ResourceNotFoundException("Post not found with id: "+postId);

        Long userId = UserContextHolder.getCurrentUserId();

        boolean alreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(!alreadyLiked) throw new BadRequestException("Cannot unlike the post which is not liked.");

        postLikeRepository.deleteByUserIdAndPostId(userId, postId);

        log.info("Post with id: {} unliked successfully", postId);
    }
}
