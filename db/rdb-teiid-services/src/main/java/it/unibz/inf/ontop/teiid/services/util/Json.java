package it.unibz.inf.ontop.teiid.services.util;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Objects;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.io.CharStreams;
import com.google.common.reflect.TypeToken;

public class Json {

    private static final ObjectMapper MAPPER;

    private static final JsonNodeFactory NODE_FACTORY;

    static {
        // Initialize ObjectMapper
        MAPPER = new ObjectMapper();
        MAPPER.getFactory().configure(Feature.AUTO_CLOSE_TARGET, false);
        MAPPER.registerModules(ObjectMapper.findModules());
        MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Initialize JsonNodeFactory
        NODE_FACTORY = MAPPER.getNodeFactory();
    }

    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    public static JsonNodeFactory getNodeFactory() {
        return NODE_FACTORY;
    }

    @Nullable
    public static String write(@Nullable final Object object) {
        return write(object, false);
    }

    @Nullable
    public static String write(@Nullable final Object object, final boolean prettify) {

        // Handle the null case
        if (object == null || object instanceof NullNode || object instanceof MissingNode) {
            return null;
        }

        try {
            // Serialize, possibly prettifying the result
            final ObjectMapper om = Json.getObjectMapper();
            if (prettify) {
                return om.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } else {
                return om.writeValueAsString(object);
            }

        } catch (final IOException ex) {
            // Wrap and propagate
            throw new RuntimeException(ex);
        }
    }

    public static <T extends Appendable> T write(@Nullable final Object object, final T sink) {
        return write(object, sink, false);
    }

    public static <T extends Appendable> T write(@Nullable Object object, final T sink,
            final boolean prettify) {

        // Check arguments
        Objects.requireNonNull(sink);

        // Default contract: if the object is null, we write the JSON constant NULL
        if (object == null || object instanceof MissingNode) {
            object = NullNode.getInstance();
        }

        try {
            // Handle two cases based on the type of sink
            if (sink instanceof Writer) {
                // (1) Writer sink: use specific support by Jackson
                final Writer writer = (Writer) sink;
                final ObjectMapper om = getObjectMapper();
                if (prettify) {
                    om.writerWithDefaultPrettyPrinter().writeValue(writer, object);
                } else {
                    om.writeValue(writer, object);
                }

            } else {
                // (2) Other sink: delegate to write() method producing a String
                sink.append(write(object, prettify));
            }

        } catch (final IOException ex) {
            // Wrap and propagate
            throw new UncheckedIOException(ex);
        }

        // Return the supplied sink
        return sink;
    }

    @Nullable
    public static <T> T read(@Nullable final String string, final Class<T> type) {

        // Check arguments
        Objects.requireNonNull(type);

        // Handle null input
        if (string == null) {
            Objects.requireNonNull(type);
            return null;
        }

        try {
            // Parse the string into an object of the type supplied
            return getObjectMapper().readValue(string, type);

        } catch (final IOException ex) {
            // Wrap and propagate
            throw new UncheckedIOException(ex);
        }
    }

    @Nullable
    public static <T> T read(@Nullable final String string, final TypeToken<T> type) {

        // Check arguments
        Objects.requireNonNull(type);

        // Handle null input
        if (string == null) {
            Objects.requireNonNull(type);
            return null;
        }

        try {
            // Convert to JavaType keeping track of generics, then parse
            final JavaType javaType = TypeFactory.defaultInstance().constructType(type.getType());
            return getObjectMapper().readValue(string, javaType);

        } catch (final IOException ex) {
            // Wrap and propagate
            throw new UncheckedIOException(ex);
        }
    }

    @Nullable
    public static <T> T read(final Readable readable, final Class<T> type) {

        // Check arguments
        Objects.requireNonNull(readable);
        Objects.requireNonNull(type);

        try {
            // Handle the Reader case supported by Jackson, and delegate otherwise
            if (readable instanceof Reader) {
                return getObjectMapper().readValue((Reader) readable, type);
            } else {
                return read(CharStreams.toString(readable), type);
            }

        } catch (final IOException ex) {
            // Wrap and propagate
            throw new UncheckedIOException(ex);
        }
    }

    public static <T> T read(final Readable readable, final TypeToken<T> type) {

        // Check arguments
        Objects.requireNonNull(readable);
        Objects.requireNonNull(type);

        try {
            // Handle the Reader case supported by Jackson, and delegate otherwise
            if (readable instanceof Reader) {
                final JavaType t = TypeFactory.defaultInstance().constructType(type.getType());
                return getObjectMapper().readValue((Reader) readable, t);
            } else {
                return read(CharStreams.toString(readable), type);
            }

        } catch (final IOException ex) {
            // Wrap and propagate
            throw new UncheckedIOException(ex);
        }
    }

}
