package it.unibz.inf.ontop.teiid.services;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import it.unibz.inf.ontop.teiid.services.util.Signature;

public interface ServiceManager {

    @Nullable
    Service get(String name);

    List<Service> getAll();

    @Nullable
    default Service define(final String name, final Signature inputSignature,
            final Signature outputSignature, final Map<String, Object> properties) {
        return null;
    }

    static ServiceManager wrap(final Service... services) {
        return wrap(Arrays.asList(services));
    }

    static ServiceManager wrap(final Iterable<? extends Service> services) {

        final int size = Iterables.size(services);

        if (size == 0) {
            return EMPTY;
        }

        final List<Service> serviceList = ImmutableList.copyOf(services);

        final ImmutableMap.Builder<String, Service> mb = ImmutableMap.builder();
        for (final Service service : serviceList) {
            mb.put(service.getName(), service);
        }
        final Map<String, Service> serviceMap = mb.build();

        return new ServiceManager() {

            @Override
            public Service get(final String name) {
                return serviceMap.get(name);
            }

            @Override
            public List<Service> getAll() {
                return serviceList;
            }

        };
    }

    static ServiceManager compose(final ServiceManager... managers) {
        return compose(Arrays.asList(managers));
    }

    static ServiceManager compose(final Iterable<? extends ServiceManager> managers) {

        final int size = Iterables.size(managers);

        if (size == 0) {
            return EMPTY;
        }

        if (size == 1) {
            return Iterables.get(managers, 0);
        }

        final List<ServiceManager> delegates = ImmutableList.copyOf(managers);
        return new ServiceManager() {

            @Override
            public Service get(final String name) {
                synchronized (delegates) {
                    for (final ServiceManager delegate : delegates) {
                        final Service service = delegate.get(name);
                        if (service != null) {
                            return service;
                        }
                    }
                    return null;
                }
            }

            @Override
            public List<Service> getAll() {
                synchronized (delegates) {
                    final Set<String> seenNames = Sets.newHashSet();
                    final List<Service> services = Lists.newArrayList();
                    for (final ServiceManager delegate : delegates) {
                        for (final Service service : delegate.getAll()) {
                            if (seenNames.add(service.getName())) {
                                services.add(service);
                            }
                        }
                    }
                    return services;
                }
            }

            @Override
            public Service define(final String name, final Signature inputSignature,
                    final Signature outputSignature, final Map<String, Object> properties) {
                synchronized (delegates) {
                    if (get(name) != null) {
                        throw new Error("Service " + name + " already defined");
                    }
                    for (final ServiceManager delegate : delegates) {
                        final Service service = delegate.define(name, inputSignature,
                                outputSignature, properties);
                        if (service != null) {
                            return service;
                        }
                    }
                    return null;
                }
            }

        };
    }

    final ServiceManager EMPTY = new ServiceManager() {

        @Override
        public Service get(final String name) {
            Objects.requireNonNull(name);
            return null;
        }

        @Override
        public List<Service> getAll() {
            return ImmutableList.of();
        }

    };

}
