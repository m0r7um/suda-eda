package org.food.sudaeda.emailservice.configuration;

import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.food.sudaeda.emailservice.configuration.properties.ArtemisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;

@Configuration
@EnableJms
@RequiredArgsConstructor
public class ArtemisConfig {
    private final ArtemisProperties artemisProperties;

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setTransactionManager(new JmsTransactionManager(connectionFactory));
        factory.setSessionTransacted(true);
        factory.setConcurrency("1-1");
        return factory;
    }

    @Bean
    public JmsTransactionManager jmsTransactionManager(ConnectionFactory connectionFactory) {
        return new JmsTransactionManager(connectionFactory);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        String brokerUrl = "amqp://localhost:5672";
        JmsConnectionFactory qpidFactory = new JmsConnectionFactory(
                artemisProperties.getUsername(),
                artemisProperties.getPassword(),
                brokerUrl);
        CachingConnectionFactory cachingFactory = new CachingConnectionFactory(qpidFactory);
        cachingFactory.setSessionCacheSize(10);
        return cachingFactory;
    }
}
