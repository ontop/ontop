package it.unibz.inf.ontop.teiid.services;

import java.io.Closeable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.teiid.metadata.AbstractMetadataRecord;
import org.teiid.resource.api.ConnectionFactory;

import it.unibz.inf.ontop.teiid.services.invokers.ServiceInvoker;
import it.unibz.inf.ontop.teiid.services.invokers.ServiceInvokerFactory;
import it.unibz.inf.ontop.teiid.services.model.Service;

public final class ServiceConnectionFactory
        implements ConnectionFactory<ServiceConnection>, Closeable {

    private final Map<String, ServiceInvoker> invokers;

    public ServiceConnectionFactory() {
        this.invokers = Maps.newConcurrentMap();
    }

    public ServiceInvoker getInvoker(final AbstractMetadataRecord metadata) {
        return getInvoker(metadata.getName(), () -> Service.create(metadata));
    }

    public ServiceInvoker getInvoker(final String serviceName,
            @Nullable final Supplier<Service> serviceSupplier) {

        Objects.requireNonNull(serviceName);

        ServiceInvoker invoker = this.invokers.get(serviceName);

        if (invoker == null) {
            Preconditions.checkArgument(serviceSupplier != null,
                    "Cannot create invoker for service " + serviceName);
            final Service service = serviceSupplier.get();
            synchronized (this) {
                invoker = this.invokers.get(serviceName);
                if (invoker == null) {
                    invoker = ServiceInvokerFactory.DEFAULT_INSTANCE.create(service);
                    this.invokers.put(serviceName, invoker);
                }
            }
        }

        return invoker;
    }

    @Override
    public ServiceConnection getConnection() {
        return new ServiceConnection(this);
    }

    @Override
    public void close() {
    }

}
