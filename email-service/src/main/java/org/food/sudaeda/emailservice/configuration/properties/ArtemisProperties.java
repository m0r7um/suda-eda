package org.food.sudaeda.emailservice.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("artemis")
public class ArtemisProperties {
    private String username;
    private String password;
    private String orderStatusUpdateQueue;
}
