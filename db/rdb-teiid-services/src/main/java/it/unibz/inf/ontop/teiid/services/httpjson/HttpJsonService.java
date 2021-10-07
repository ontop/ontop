package it.unibz.inf.ontop.teiid.services.httpjson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibz.inf.ontop.teiid.services.AbstractService;
import it.unibz.inf.ontop.teiid.services.util.Json;
import it.unibz.inf.ontop.teiid.services.util.JsonTemplate;
import it.unibz.inf.ontop.teiid.services.util.Signature;
import it.unibz.inf.ontop.teiid.services.util.StringTemplate;
import it.unibz.inf.ontop.teiid.services.util.Tuple;

public class HttpJsonService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpJsonService.class);

    public static final String PROP_URL = "url";

    public static final String PROP_METHOD = "method";

    public static final String PROP_REQUEST_BODY = "requestBody";

    public static final String PROP_RESPONSE_BODY = "responseBody";

    private static final AtomicInteger REQUEST_COUNTER = new AtomicInteger();

    private final HttpClient client;

    private final Method method;

    private final StringTemplate urlTemplate;

    private final Function<Tuple[], Tuple> projection;

    @Nullable
    private final JsonTemplate requestBodyTemplate;

    private final JsonTemplate responseBodyTemplate;

    public HttpJsonService(final String name, final Signature inputSignature,
            final Signature outputSignature, final Map<String, Object> properties) {
        this(name, inputSignature, outputSignature, properties, null);
    }

    public HttpJsonService(final String name, final Signature inputSignature,
            final Signature outputSignature, final Map<String, Object> properties,
            @Nullable final HttpClient client) {

        super(name, inputSignature, outputSignature);

        final String url = (String) properties.get(PROP_URL);
        final String method = (String) properties.get(PROP_METHOD);
        final String requestBody = (String) properties.get(PROP_REQUEST_BODY);
        final String responseBody = (String) properties.get(PROP_RESPONSE_BODY);

        this.client = Objects.requireNonNull(client);
        this.method = Method.valueOf(method != null ? method.trim().toUpperCase() : "GET");
        this.urlTemplate = new StringTemplate(url);
        this.requestBodyTemplate = requestBody != null ? new JsonTemplate(requestBody) : null;
        this.responseBodyTemplate = responseBody != null ? new JsonTemplate(responseBody) : null;
        this.projection = this.responseBodyTemplate == null ? null
                : Tuple.projectFunction(outputSignature, this.responseBodyTemplate.getSignature(),
                        inputSignature);
    }

    @Override
    public Iterator<Tuple> invoke(final Tuple inputTuple) {

        final String requestId = String.format("REQ%04d", REQUEST_COUNTER.incrementAndGet());

        try {
            final String uri = this.urlTemplate.format(inputTuple);

            final HttpUriRequest request = this.method.createRequest(uri);
            request.setHeader("Accept", ContentType.APPLICATION_JSON.toString());

            if (this.requestBodyTemplate != null) {
                final JsonNode js = this.requestBodyTemplate.format(ImmutableList.of(inputTuple));
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Json.write(js, new OutputStreamWriter(bos, Charsets.UTF_8));
                final ByteArrayEntity entity = new ByteArrayEntity(bos.toByteArray());
                entity.setContentType(ContentType.APPLICATION_JSON.toString());
                ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
            }

            LOGGER.info("{}: {} {} {}", requestId, this.method, uri, inputTuple.toString(true));

            final HttpResponse resp = this.client.execute(request);

            final HttpEntity entity = resp.getEntity();
            final Charset charset = MoreObjects
                    .firstNonNull(ContentType.getOrDefault(entity).getCharset(), Charsets.UTF_8);
            final JsonNode responseBody;
            try (Reader reader = new InputStreamReader(resp.getEntity().getContent(), charset)) {
                responseBody = Json.read(reader, JsonNode.class);
            }

            final List<Tuple> outputTuples = this.responseBodyTemplate != null //
                    ? this.responseBodyTemplate.parse(responseBody)
                    : ImmutableList.of();

            final Tuple[] tmpTuples = new Tuple[] { null, inputTuple };
            for (int i = 0; i < outputTuples.size(); ++i) {
                Tuple t = outputTuples.get(i);
                tmpTuples[0] = t;
                t = this.projection.apply(tmpTuples);
                outputTuples.set(i, t);
                LOGGER.info("{}: #{} {}", requestId, i + 1, t.toString(true));
            }

            return outputTuples.iterator();

        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static enum Method {

        GET,

        POST,

        PUT,

        DELETE;

        public HttpUriRequest createRequest(final String uri) {
            if (this == Method.GET) {
                return new HttpGet(uri);
            } else if (this == Method.POST) {
                return new HttpPost(uri);
            } else if (this == Method.PUT) {
                return new HttpPut(uri);
            } else if (this == Method.DELETE) {
                return new HttpDelete(uri);
            }
            throw new Error();
        }

    };

}
