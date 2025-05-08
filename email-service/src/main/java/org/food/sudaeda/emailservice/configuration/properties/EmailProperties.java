package org.food.sudaeda.emailservice.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("email")
public class EmailProperties {
    private String from;
    private String subject;
}
