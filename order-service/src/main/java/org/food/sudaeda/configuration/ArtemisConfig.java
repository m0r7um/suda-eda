package org.food.sudaeda.configuration;

import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.food.sudaeda.configuration.properties.ArtemisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@RequiredArgsConstructor
public class ArtemisConfig {
    private final ArtemisProperties artemisProperties;

    @Bean
    public ConnectionFactory connectionFactory() {
        var brokerUrl = "amqp://localhost:5672";
        return new JmsConnectionFactory(
                artemisProperties.getUsername(),
                artemisProperties.getPassword(),
                brokerUrl);
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }
}
