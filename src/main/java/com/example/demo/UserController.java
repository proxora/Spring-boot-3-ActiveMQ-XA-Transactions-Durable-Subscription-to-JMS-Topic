package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserRepository userRepository;

    @GetMapping("/user")
    @Transactional
    public Iterable<User> findAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/user/count")
    @Transactional
    public long countAllUsers() {
        return userRepository.count();
    }


    @GetMapping("/user/create")
    @Transactional
    public User createUser() {
        User entity = new User();
        entity.setUsername("testUser");
        entity = userRepository.save(entity);
        applicationEventPublisher.publishEvent(new UserChangedEvent(entity));
        return entity;
    }

    @GetMapping("/user/createError")
    @Transactional
    public User createUserError() {
        User entity = new User();
        entity.setUsername("testUser");
        entity = userRepository.save(entity);
        applicationEventPublisher.publishEvent(new UserChangedEvent(entity));
        if (true) {
            throw new IllegalStateException("UserCreateError");
        }
        return entity;
    }
}
