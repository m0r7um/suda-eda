package org.food.sudaeda.core.service;

import jakarta.annotation.Resource;
import jakarta.resource.ResourceException;
import lombok.RequiredArgsConstructor;

import org.food.sudaeda.jca.KeycloakConnection;
import org.food.sudaeda.jca.KeycloakConnectionFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAttributeService {

    private final KeycloakConnectionFactory connectionFactory;

    public List<String> getAttribute(String userId, String attribute) {
        try (KeycloakConnection conn = connectionFactory.getConnection()) {
            return conn.getUserAttribute(userId, attribute);
        } catch (ResourceException e) {
            throw new RuntimeException("Ошибка подключения к Keycloak", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateAttribute(String userId, String attribute, String value) {
        try (KeycloakConnection conn = connectionFactory.getConnection()) {
            conn.updateUserAttribute(userId, attribute, value);
        } catch (ResourceException e) {
            throw new RuntimeException("Ошибка подключения к Keycloak", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
