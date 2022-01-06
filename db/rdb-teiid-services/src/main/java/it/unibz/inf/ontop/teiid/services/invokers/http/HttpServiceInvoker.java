package it.unibz.inf.ontop.teiid.services.invokers.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.net.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibz.inf.ontop.teiid.services.invokers.AbstractServiceInvoker;
import it.unibz.inf.ontop.teiid.services.model.Service;
import it.unibz.inf.ontop.teiid.services.model.Signature;
import it.unibz.inf.ontop.teiid.services.model.Tuple;
import it.unibz.inf.ontop.teiid.services.serializers.TupleReader;
import it.unibz.inf.ontop.teiid.services.serializers.TupleSerializer;
import it.unibz.inf.ontop.teiid.services.serializers.TupleSerializerFactory;
import it.unibz.inf.ontop.teiid.services.serializers.TupleWriter;

public class HttpServiceInvoker extends AbstractServiceInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServiceInvoker.class);

    private static final AtomicInteger REQUEST_COUNTER = new AtomicInteger();

    private final HttpClient client;

    private final Method method;

    private final TupleWriter urlWriter;

    @Nullable
    private final TupleWriter requestBodyWriter;

    @Nullable
    private final TupleReader responseBodyReader;

    public HttpServiceInvoker(final HttpClient client, final Service service) {
        super(service);
        this.client = Objects.requireNonNull(client);
        this.method = Method.valueOf(
                service.getProperties().getOrDefault("method", "GET").trim().toUpperCase());
        this.urlWriter = Objects.requireNonNull( //
                createSerializer(TupleWriter.class, service.getInputSignature(),
                        MediaType.PLAIN_TEXT_UTF_8, service.getProperties(), "url"));
        this.requestBodyWriter = createSerializer(TupleWriter.class, service.getInputSignature(),
                null, service.getProperties(), "requestBody");
        this.responseBodyReader = createSerializer(TupleReader.class, service.getOutputSignature(),
                null, service.getProperties(), "responseBody");
    }

    @Override
    public Iterator<Tuple> invoke(final Tuple inputTuple) {

        final String requestId = String.format("REQ%04d", REQUEST_COUNTER.incrementAndGet());

        try {
            final String uri = this.urlWriter.writeString(ImmutableList.of(inputTuple));

            final HttpUriRequest request = this.method.createRequest(uri);

            if (this.responseBodyReader != null) {
                request.setHeader("Accept", this.responseBodyReader.getMediaType().toString());
            }

            if (this.requestBodyWriter != null) {
                final EntityTemplate requestBody = new EntityTemplate(stream -> {
                    this.requestBodyWriter.write(stream, ImmutableList.of(inputTuple));
                });
                requestBody.setContentType(this.requestBodyWriter.getMediaType().toString());
                ((HttpEntityEnclosingRequestBase) request).setEntity(requestBody);
            }

            LOGGER.info("{}: {} {} {}", requestId, this.method, uri, inputTuple.toString(true));

            final HttpResponse resp = this.client.execute(request);

            List<Tuple> outputTuples = ImmutableList.of();
            final HttpEntity entity = resp.getEntity();
            if (entity != null) {
                try {
                    if (this.responseBodyReader != null) {

                        MediaType responseBodyType = null;
                        if (entity.getContentType() != null) {
                            responseBodyType = MediaType.parse(entity.getContentType().getValue());
                            Preconditions.checkArgument(responseBodyType.withoutParameters().is(
                                    this.responseBodyReader.getMediaType().withoutParameters()));
                        }

                        final Charset receivedCharset = responseBodyType.charset().orNull();
                        final Charset expectedCharset = this.responseBodyReader.getMediaType()
                                .charset().orNull();
                        if (receivedCharset == null || receivedCharset.equals(expectedCharset)) {
                            outputTuples = this.responseBodyReader.read(entity.getContent());
                        } else {
                            outputTuples = this.responseBodyReader.read(
                                    new InputStreamReader(entity.getContent(), receivedCharset));
                        }
                    }
                } finally {
                    EntityUtils.consume(entity);
                }
            }

            if (LOGGER.isInfoEnabled()) {
                for (int i = 0; i < outputTuples.size(); ++i) {
                    LOGGER.info("{}: #{} {}", requestId, i + 1,
                            outputTuples.get(i).toString(true));
                }
            }

            return outputTuples.iterator();

        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <T extends TupleSerializer> T createSerializer(final Class<T> clazz,
            final Signature signature, @Nullable MediaType mediaType,
            final Map<String, String> properties, String prefix) {

        // Remap a property matching the prefix itself to property.template (for legacy configs)
        final Map<String, String> serializerProperties = Maps.newLinkedHashMap();
        prefix = prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;
        if (properties.containsKey(prefix)) {
            serializerProperties.put("template", properties.get(prefix));
        }

        // Keep all properties matching prefix (i.e., prefix.property), after stripping it away
        prefix = prefix + ".";
        for (final Entry<String, String> e : properties.entrySet()) {
            if (e.getKey().startsWith(prefix)) {
                serializerProperties.put(e.getKey().substring(prefix.length()), e.getValue());
            }
        }

        // Return null if the configuration is empty (i.e., serializer not used)
        if (serializerProperties.isEmpty()) {
            return null;
        }

        // Read media type from property 'type', if not fixed (i.e., controllable via properties)
        if (mediaType == null) {
            final String mediaTypeStr = serializerProperties.get("type");
            mediaType = mediaTypeStr != null //
                    ? MediaType.parse(mediaTypeStr)
                    : MediaType.JSON_UTF_8;
        }

        // Instantiate the serializer (either a tuple reader or a tuple writer)
        final TupleSerializerFactory factory = TupleSerializerFactory.DEFAULT_INSTANCE;
        return clazz.isAssignableFrom(TupleReader.class)
                ? (T) factory.createReader(signature, mediaType, serializerProperties)
                : (T) factory.createWriter(signature, mediaType, serializerProperties);
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
