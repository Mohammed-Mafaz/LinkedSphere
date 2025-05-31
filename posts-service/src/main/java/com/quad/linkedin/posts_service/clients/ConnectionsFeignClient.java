package com.quad.linkedin.posts_service.clients;

import com.quad.linkedin.posts_service.dto.PersonDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "connections-service" , path = "/connections")
public interface ConnectionsFeignClient {

    @GetMapping("/core/first-degree")
    List<PersonDto> getFirstConnections();
}
