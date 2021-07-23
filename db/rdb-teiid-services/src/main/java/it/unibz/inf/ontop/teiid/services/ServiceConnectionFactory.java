package it.unibz.inf.ontop.teiid.services;

import java.io.Closeable;
import java.util.Objects;

import javax.annotation.Nullable;

import org.teiid.resource.api.ConnectionFactory;

import it.unibz.inf.ontop.teiid.services.httpjson.HttpJsonServiceManager;

public final class ServiceConnectionFactory
        implements ConnectionFactory<ServiceConnection>, Closeable {

    private final ServiceManager serviceManager;

    public ServiceConnectionFactory() {
        this(null);
    }

    public ServiceConnectionFactory(@Nullable ServiceManager serviceManager) {

        if (serviceManager == null) {
            // TODO: discover on classpath via ServiceLoader mechanism
            serviceManager = new HttpJsonServiceManager(null);
        }

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
