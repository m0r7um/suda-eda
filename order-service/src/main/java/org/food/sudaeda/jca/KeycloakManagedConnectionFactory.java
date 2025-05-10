package org.food.sudaeda.jca;

import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import lombok.Getter;
import lombok.Setter;

import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
public class KeycloakManagedConnectionFactory implements ManagedConnectionFactory, Serializable {
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;

    @Override
    public Object createConnectionFactory() {
        return (KeycloakConnectionFactory) () -> new KeycloakConnectionImpl(serverUrl, realm, clientId, clientSecret);
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) {
        return createConnectionFactory();
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) {
        return new KeycloakManagedConnection(this);
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connections, Subject subject, ConnectionRequestInfo cxRequestInfo) { return null; }

    @Override
    public void setLogWriter(PrintWriter out) {}

    @Override
    public PrintWriter getLogWriter() { return null; }
}
