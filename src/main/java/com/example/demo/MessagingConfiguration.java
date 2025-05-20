package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
@Slf4j
public class MessagingConfiguration {

    @Bean
    ActiveMQConnectionFactoryCustomizer activeMQConnectionFactoryCustomizer(@Value("${spring.jms.client-id}") String clientId) {
        return connectionFactory -> {
            log.info("Configuring JMS connection factory: {}", connectionFactory.getClass().getName());
            connectionFactory.setClientID(clientId);
        };
    }

    @Bean
    MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    UserMessagingDummyService userMessagingDummy(JmsTemplate jmsTemplate, UserRepository userRepository) {
        return new UserMessagingDummyService(jmsTemplate, userRepository);
    }
}
