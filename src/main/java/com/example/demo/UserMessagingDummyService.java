package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class UserMessagingDummyService {

    private final JmsTemplate jmsTopicTemplate;
    private final UserRepository userRepository;

    @Transactional
    @EventListener
    public void on(UserChangedEvent event) {
        log.info("Sending MSG to JMS destinations with connectionFactory='{}',  connectionFactoryClass='{}' , isSessionTransacted='{}'  <{}> ...",
                jmsTopicTemplate.getConnectionFactory(), jmsTopicTemplate.getConnectionFactory().getClass().getName(), jmsTopicTemplate.isSessionTransacted(), event);
        jmsTopicTemplate.convertAndSend("my_user_topic", event);
        log.info("sending done");
    }

    @Transactional
    @JmsListener(destination = "my_user_topic")
    public void receiveUserChangedEventFromTopic(UserChangedEvent event) {
        log.info("Received MSG From Topic Listener 1 <{}>", event);
        userRepository.findById(event.getUser().getId())
                .ifPresent(user -> log.info("loaded user from DB :<{}>", user));
    }
}
