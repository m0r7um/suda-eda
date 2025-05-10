package org.food.sudaeda.jca;

import jakarta.resource.ResourceException;

public interface KeycloakConnectionFactory {
    KeycloakConnection getConnection() throws ResourceException;
}
