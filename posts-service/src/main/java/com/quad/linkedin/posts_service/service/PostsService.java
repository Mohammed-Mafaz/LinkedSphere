package com.quad.linkedin.posts_service.service;

import com.quad.linkedin.posts_service.auth.UserContextHolder;
import com.quad.linkedin.posts_service.clients.ConnectionsFeignClient;
import com.quad.linkedin.posts_service.dto.PostCreateRequestDto;
import com.quad.linkedin.posts_service.dto.PostDto;
import com.quad.linkedin.posts_service.entity.Post;
import com.quad.linkedin.posts_service.events.PostCreatedEvent;
import com.quad.linkedin.posts_service.exception.ResourceNotFoundException;
import com.quad.linkedin.posts_service.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostsService {

    private final PostsRepository postsRepository;
    private final ModelMapper modelMapper;
    private final ConnectionsFeignClient connectionsFeignClient;
    private final KafkaTemplate<Long, PostCreatedEvent> kafkaTemplate;

//    @Value("${kafka.topic.post-created}")
    private final String KAFKA_POST_CREATED_TOPIC = "post-created";

    public PostDto createPost(PostCreateRequestDto postDto) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("[createPost] - User Id from UserContextHolder: {}", userId);
        Post post = modelMapper.map(postDto, Post.class);
        post.setUserId(userId);

        Post savedPost = postsRepository.save(post);

        PostCreatedEvent postCreatedEvent = PostCreatedEvent.builder()
                .postId(savedPost.getId())
                .creatorId(userId)
                .content(savedPost.getContent())
                .build();
        log.info("Sending new post info to Kafka");
        kafkaTemplate.send(KAFKA_POST_CREATED_TOPIC,savedPost.getId(),postCreatedEvent);
        return modelMapper.map(savedPost, PostDto.class);
    }

    public PostDto getPostById(Long postId) {
        log.debug("Retrieving post with ID: {}", postId);

        Long userId = UserContextHolder.getCurrentUserId();
        
        Post post = postsRepository.findById(postId).orElseThrow(() ->
                new ResourceNotFoundException("Post not found with id: "+postId));
        return modelMapper.map(post, PostDto.class);
    }

    public List<PostDto> getAllPostsOfUser(Long userId) {
        List<Post> posts = postsRepository.findByUserId(userId);

        return posts
            .stream()
            .map((element) -> modelMapper.map(element, PostDto.class))
            .collect(Collectors.toList());
    }
}
