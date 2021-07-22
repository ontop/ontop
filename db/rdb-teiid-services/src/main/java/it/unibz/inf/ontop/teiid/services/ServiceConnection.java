package it.unibz.inf.ontop.teiid.services;

import java.util.Objects;

import org.teiid.resource.api.Connection;

public final class ServiceConnection implements Connection {

    private final ServiceConnectionFactory factory;

    public ServiceConnection(final ServiceConnectionFactory factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    public ServiceConnectionFactory getFactory() {
        return this.factory;
    }

    public ServiceManager getServiceManager() {
        return this.factory.getServiceManager();
    }

    @Override
    public void close() {
    }

}
