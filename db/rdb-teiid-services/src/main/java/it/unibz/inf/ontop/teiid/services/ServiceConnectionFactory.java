package it.unibz.inf.ontop.teiid.services;

import java.io.Closeable;
import java.util.Objects;

import org.teiid.resource.api.ConnectionFactory;

public final class ServiceConnectionFactory
        implements ConnectionFactory<ServiceConnection>, Closeable {

    private final ServiceManager serviceManager;

    public ServiceConnectionFactory(final ServiceManager serviceManager) {
        this.serviceManager = Objects.requireNonNull(serviceManager);
    }

    public ServiceManager getServiceManager() {
        return this.serviceManager;
    }

    @Override
    public ServiceConnection getConnection() {
        return new ServiceConnection(this);
    }

    @Override
    public void close() {
    }

}
