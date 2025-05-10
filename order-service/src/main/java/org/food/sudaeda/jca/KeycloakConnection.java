package org.food.sudaeda.jca;

import jakarta.resource.ResourceException;

import java.util.List;

public interface KeycloakConnection extends AutoCloseable {
    List<String> getUserAttribute(String userId, String attributeName) throws ResourceException;
    void updateUserAttribute(String userId, String attributeName, String value) throws ResourceException;
}
