package org.food.sudaeda.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class KeycloakManagedConnection implements ManagedConnection {

    private final KeycloakManagedConnectionFactory factory;
    private KeycloakConnectionImpl connection;
    private PrintWriter logWriter;
    private final List<ConnectionEventListener> listeners = new ArrayList<>();

    public KeycloakManagedConnection(KeycloakManagedConnectionFactory factory) {
        this.factory = factory;
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        if (connection == null) {
            connection = new KeycloakConnectionImpl(
                    factory.getServerUrl(),
                    factory.getRealm(),
                    factory.getClientId(),
                    factory.getClientSecret()
            );
        }
        return connection;
    }

    @Override
    public void destroy() throws ResourceException {
        if (connection != null) {
            connection.close();
            notifyListeners(new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED));
            connection = null;
        }
    }

    @Override
    public void cleanup() throws ResourceException {
        if (connection != null) {
            connection.close();
            notifyListeners(new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED));
            connection = null;
        }
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        if (!(connection instanceof KeycloakConnectionImpl)) {
            throw new ResourceException("Invalid connection type");
        }
        this.connection = (KeycloakConnectionImpl) connection;
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Получение XA ресурса
     *
     * @return Возвращаем null, так как Keycloak не поддерживает XA-транзакции
     * @throws ResourceException ошибка ресурса
     */
    @Override
    public XAResource getXAResource() throws ResourceException {
        return null;
    }

    /**
     * Заглушки, так как Keycloak не поддерживает локальные транзакции
     *
     * @return Пустую реализацию LocalTransaction
     * @throws ResourceException ошибка ресурса
     */
    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return new LocalTransaction() {
            @Override
            public void begin() {}
            @Override
            public void commit() {}
            @Override
            public void rollback() {}
        };
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return new ManagedConnectionMetaData() {
            @Override
            public String getEISProductName() throws ResourceException {
                return "Keycloak Adapter";
            }

            @Override
            public String getEISProductVersion() throws ResourceException {
                return "1.0";
            }

            @Override
            public int getMaxConnections() throws ResourceException {
                return 10;
            }

            @Override
            public String getUserName() throws ResourceException {
                return factory.getClientId();
            }
        };
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    private void notifyListeners(ConnectionEvent event) {
        for (ConnectionEventListener listener : listeners) {
            listener.connectionClosed(event);
        }
    }
}
