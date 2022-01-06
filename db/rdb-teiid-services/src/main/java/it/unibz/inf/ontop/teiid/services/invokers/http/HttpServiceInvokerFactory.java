package it.unibz.inf.ontop.teiid.services.invokers.http;

import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import it.unibz.inf.ontop.teiid.services.invokers.ServiceInvoker;
import it.unibz.inf.ontop.teiid.services.invokers.ServiceInvokerFactory;
import it.unibz.inf.ontop.teiid.services.model.Service;

public class HttpServiceInvokerFactory implements ServiceInvokerFactory {

    private static final HttpClient CLIENT = HttpClients.createDefault();

    @Override
    public boolean supports(final Service service) {
        final Map<String, String> props = service.getProperties();
        return props.get("url") instanceof String;
    }

    @Override
    public ServiceInvoker create(final Service service) {
        return new HttpServiceInvoker(CLIENT, service);
    }

}
