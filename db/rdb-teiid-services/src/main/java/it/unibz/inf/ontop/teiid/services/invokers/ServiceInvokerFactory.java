package it.unibz.inf.ontop.teiid.services.invokers;

import java.util.Objects;
import java.util.ServiceLoader;

import com.google.common.collect.Iterables;

import it.unibz.inf.ontop.teiid.services.model.Service;

public interface ServiceInvokerFactory {

    ServiceInvokerFactory DEFAULT_INSTANCE = compose(
            ServiceLoader.load(ServiceInvokerFactory.class));

    boolean supports(Service service);

    ServiceInvoker create(Service service);

    static ServiceInvokerFactory compose(final Iterable<ServiceInvokerFactory> instances) {

        if (Iterables.size(instances) == 1) {
            return Iterables.getFirst(instances, null);
        }

        final ServiceInvokerFactory[] delegates = Iterables.toArray(instances,
                ServiceInvokerFactory.class);

        return new ServiceInvokerFactory() {

            @Override
            public boolean supports(final Service service) {
                Objects.requireNonNull(service);
                for (final ServiceInvokerFactory delegate : delegates) {
                    if (delegate.supports(service)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public ServiceInvoker create(final Service service) {
                Objects.requireNonNull(service);
                for (final ServiceInvokerFactory delegate : delegates) {
                    if (delegate.supports(service)) {
                        return delegate.create(service);
                    }
                }
                throw new IllegalArgumentException(
                        "Cannot create invoker for service " + service.toString(true));
            }

        };
    }

}
