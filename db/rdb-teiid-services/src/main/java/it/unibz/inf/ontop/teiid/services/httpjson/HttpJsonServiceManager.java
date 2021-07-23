package it.unibz.inf.ontop.teiid.services.httpjson;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import it.unibz.inf.ontop.teiid.services.Service;
import it.unibz.inf.ontop.teiid.services.ServiceManager;
import it.unibz.inf.ontop.teiid.services.util.Signature;

public final class HttpJsonServiceManager implements ServiceManager {

    private final HttpClient client;

    private final Map<String, Service> serviceMap;

    public HttpJsonServiceManager(@Nullable final HttpClient client) {
        this.client = client != null ? client : HttpClients.createDefault();
        this.serviceMap = Maps.newHashMap();
    }

    @Override
    @Nullable
    public Service get(final String name) {
        Objects.requireNonNull(name);
        synchronized (this.serviceMap) {
            return this.serviceMap.get(name);
        }
    }

    @Override
    public List<Service> getAll() {
        synchronized (this.serviceMap) {
            return ImmutableList.copyOf(this.serviceMap.values().stream()
                    .sorted((s1, s2) -> s1.getName().compareTo(s2.getName()))
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public Service define(final String name, final Signature inputSignature,
            final Signature outputSignature, final Map<String, Object> properties) {

        final HttpJsonService service = new HttpJsonService( //
                name, inputSignature, outputSignature, properties, this.client);

        synchronized (this.serviceMap) {
            if (this.serviceMap.containsKey(name)) {
                throw new IllegalArgumentException(
                        "Service for name " + name + " already defined");
            }
            this.serviceMap.put(name, service);
        }

        return service;
    }

}
