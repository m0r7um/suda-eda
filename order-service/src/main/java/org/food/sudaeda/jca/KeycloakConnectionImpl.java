package org.food.sudaeda.jca;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class KeycloakConnectionImpl implements KeycloakConnection {
    private final Keycloak keycloak;
    private final String realm;

    public KeycloakConnectionImpl(Keycloak keycloak, String realm) {
        this.keycloak = keycloak;
        this.realm = realm;
    }

    public KeycloakConnectionImpl(String serverUrl, String realm, String clientId, String clientSecret) {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
        this.realm = realm;
    }

    @Override
    public List<String> getUserAttribute(String userId, String attributeName) {
        UserRepresentation userRep = keycloak.realm(realm)
                .users()
                .get(userId)
                .toRepresentation();

        return Optional.ofNullable(userRep.getAttributes())
                .map(attrs -> attrs.get(attributeName))
                .orElse(Collections.emptyList());
    }

    @Override
    public void updateUserAttribute(String userId, String attributeName, String value) {
        UserResource user = keycloak.realm(realm).users().get(userId);
        UserRepresentation userRep = user.toRepresentation();
        userRep.singleAttribute(attributeName, value);
        user.update(userRep);
    }

    @Override
    public void close() {
        keycloak.close();
    }
}
