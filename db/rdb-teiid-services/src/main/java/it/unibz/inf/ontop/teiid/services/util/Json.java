package it.unibz.inf.ontop.teiid.services.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Objects;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.io.CharStreams;
import com.google.common.reflect.TypeToken;

import org.locationtech.jts.geom.Geometry;
import org.teiid.core.types.BinaryType;
import org.teiid.core.types.BlobType;
import org.teiid.core.types.ClobImpl;
import org.teiid.core.types.ClobType;
import org.teiid.core.types.GeographyType;
import org.teiid.core.types.GeometryType;
import org.teiid.core.types.JsonType;
import org.teiid.core.types.SQLXMLImpl;
import org.teiid.core.types.XMLType;

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

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T map(@Nullable final Object object, final Class<T> type) {

        // Check arguments
        Objects.requireNonNull(type);

        // Handle null case
        if (object == null) {
            return null;
        }

        try {
            // Read as tree and convert tree to object of desired type
            final ObjectMapper mapper = getObjectMapper();
            final JsonNode node = object instanceof JsonNode //
                    ? (JsonNode) object
                    : mapper.valueToTree(object);
            return TreeNode.class.isAssignableFrom(type) && type.isInstance(node) //
                    ? (T) node
                    : mapper.treeToValue(node, type);

        } catch (final JsonProcessingException ex) {
            // Wrap and propagate
            throw new UncheckedIOException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T map(@Nullable final Object object, final TypeToken<T> type) {

        // Check arguments
        Objects.requireNonNull(type);

        if (object == null) {
            return null;
        }

        try {
            // Read as tree and convert tree to object of desired type
            final ObjectMapper mapper = getObjectMapper();
            final JsonNode node = object instanceof JsonNode //
                    ? (JsonNode) object
                    : mapper.valueToTree(object);
            return TreeNode.class.isAssignableFrom(type.getRawType())
                    && type.isAssignableFrom(node.getClass()) //
                            ? (T) node
                            : mapper.readValue(mapper.treeAsTokens(node),
                                    TypeFactory.defaultInstance().constructType(type.getType()));

        } catch (final IOException ex) {
            // Wrap and propagate
            throw new UncheckedIOException(ex);
        }
    }

    public static class TeiidModule extends Module implements Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public String getModuleName() {
            return getClass().getName();
        }

        @Override
        public Version version() {
            return VersionUtil.parseVersion("2.12.1", "com.fasterxml.jackson.core",
                    "jackson-core");
        }

        @Override
        public void setupModule(final SetupContext context) {

            final SimpleSerializers serializers = new SimpleSerializers();
            serializers.addSerializer(ClobType.class, CLOB_SERIALIZER);
            serializers.addSerializer(XMLType.class, XML_SERIALIZER);
            serializers.addSerializer(JsonType.class, JSON_SERIALIZER);
            serializers.addSerializer(BinaryType.class, VARBINARY_SERIALIZER);
            serializers.addSerializer(BlobType.class, BLOB_SERIALIZER);
            serializers.addSerializer(GeometryType.class, GEOMETRY_SERIALIZER);
            serializers.addSerializer(GeographyType.class, GEOGRAPHY_SERIALIZER);

            final SimpleDeserializers deserializers = new SimpleDeserializers();
            deserializers.addDeserializer(ClobType.class, CLOB_DESERIALIZER);
            deserializers.addDeserializer(XMLType.class, XML_DESERIALIZER);
            deserializers.addDeserializer(JsonType.class, JSON_DESERIALIZER);
            deserializers.addDeserializer(BinaryType.class, VARBINARY_DESERIALIZER);
            deserializers.addDeserializer(BlobType.class, BLOB_DESERIALIZER);
            deserializers.addDeserializer(GeometryType.class, GEOMETRY_DESERIALIZER);
            deserializers.addDeserializer(GeographyType.class, GEOGRAPHY_DESERIALIZER);

            context.addSerializers(serializers);
            context.addDeserializers(deserializers);
        }

        private static final JsonSerializer<ClobType> CLOB_SERIALIZER = new StdSerializer<ClobType>(
                ClobType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public void serialize(final ClobType v, final JsonGenerator g,
                    final SerializerProvider p) throws IOException {
                try {
                    try (Reader reader = v.getCharacterStream()) {
                        g.writeString(reader, (int) v.length());
                    }
                } catch (final SQLException ex) {
                    throw new IOException(ex);
                }
            }

        };

        private static final JsonDeserializer<ClobType> CLOB_DESERIALIZER = new StdDeserializer<ClobType>(
                ClobType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public ClobType deserialize(final JsonParser p, final DeserializationContext ctx)
                    throws IOException, JsonProcessingException {
                return new ClobType(new ClobImpl(p.getText()));
            }

        };

        private static final JsonSerializer<XMLType> XML_SERIALIZER = new StdSerializer<XMLType>(
                XMLType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public void serialize(final XMLType v, final JsonGenerator g,
                    final SerializerProvider p) throws IOException {
                try {
                    try (Reader reader = v.getCharacterStream()) {
                        g.writeString(reader, (int) v.length());
                    }
                } catch (final SQLException ex) {
                    throw new IOException(ex);
                }
            }

        };

        private static final JsonDeserializer<XMLType> XML_DESERIALIZER = new StdDeserializer<XMLType>(
                XMLType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public XMLType deserialize(final JsonParser p, final DeserializationContext ctx)
                    throws IOException, JsonProcessingException {
                return new XMLType(new SQLXMLImpl(p.getText()));
            }

        };

        private static final JsonSerializer<JsonType> JSON_SERIALIZER = new StdSerializer<JsonType>(
                JsonType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public void serialize(final JsonType v, final JsonGenerator g,
                    final SerializerProvider p) throws IOException {
                try {
                    g.writeRawValue(v.getSubString(0, (int) v.getLength()));
                } catch (final SQLException ex) {
                    throw new IOException(ex);
                }
            }

        };

        private static final JsonDeserializer<JsonType> JSON_DESERIALIZER = new StdDeserializer<JsonType>(
                JsonType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public JsonType deserialize(final JsonParser p, final DeserializationContext ctx)
                    throws IOException, JsonProcessingException {
                final JsonNode node = p.readValueAsTree();
                return new JsonType(new ClobImpl(Json.write(node)));
            }

        };

        private static final JsonSerializer<BinaryType> VARBINARY_SERIALIZER = new StdSerializer<BinaryType>(
                BinaryType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public void serialize(final BinaryType v, final JsonGenerator g,
                    final SerializerProvider p) throws IOException {
                g.writeBinary(v.getBytesDirect());
            }

        };

        private static final JsonDeserializer<BinaryType> VARBINARY_DESERIALIZER = new StdDeserializer<BinaryType>(
                BinaryType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public BinaryType deserialize(final JsonParser p, final DeserializationContext ctx)
                    throws IOException, JsonProcessingException {
                return new BinaryType(p.getBinaryValue());
            }

        };

        private static final JsonSerializer<BlobType> BLOB_SERIALIZER = new StdSerializer<BlobType>(
                BlobType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public void serialize(final BlobType v, final JsonGenerator g,
                    final SerializerProvider p) throws IOException {
                try {
                    try (InputStream stream = v.getBinaryStream()) {
                        g.writeBinary(stream, (int) v.length());
                    }
                } catch (final SQLException ex) {
                    throw new IOException(ex);
                }
            }

        };

        private static final JsonDeserializer<BlobType> BLOB_DESERIALIZER = new StdDeserializer<BlobType>(
                BlobType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public BlobType deserialize(final JsonParser p, final DeserializationContext ctx)
                    throws IOException, JsonProcessingException {
                return new BlobType(p.getBinaryValue());
            }

        };

        private static final JsonSerializer<GeometryType> GEOMETRY_SERIALIZER = new StdSerializer<GeometryType>(
                GeometryType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public void serialize(final GeometryType v, final JsonGenerator g,
                    final SerializerProvider p) throws IOException {
                try {
                    try (InputStream stream = v.getBinaryStream()) {
                        final Geometry geometry = Geo.fromWKB(stream);
                        final int sridFromObject = v.getSrid();
                        final int sridFromWkb = geometry.getSRID();
                        if (sridFromObject != 0 && sridFromObject != sridFromWkb) {
                            geometry.setSRID(v.getSrid());
                        }
                        final String wkt = Geo.toWKT(geometry);
                        g.writeString(wkt);
                    }
                } catch (final SQLException ex) {
                    throw new IOException(ex);
                }
            }

        };

        private static final JsonDeserializer<GeometryType> GEOMETRY_DESERIALIZER = new StdDeserializer<GeometryType>(
                GeometryType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public GeometryType deserialize(final JsonParser p, final DeserializationContext ctx)
                    throws IOException, JsonProcessingException {
                final String wkt = p.getText();
                final Geometry geometry = Geo.fromWKT(wkt);
                final byte[] wkb = Geo.toWKB(geometry);
                final int srid = geometry.getSRID();
                return new GeometryType(wkb, srid);
            }

        };

        private static final JsonSerializer<GeographyType> GEOGRAPHY_SERIALIZER = new StdSerializer<GeographyType>(
                GeographyType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public void serialize(final GeographyType v, final JsonGenerator g,
                    final SerializerProvider p) throws IOException {
                try {
                    try (InputStream stream = v.getBinaryStream()) {
                        final Geometry geometry = Geo.fromWKB(stream);
                        final int sridFromObject = v.getSrid();
                        final int sridFromWkb = geometry.getSRID();
                        if (sridFromObject != 0 && sridFromObject != sridFromWkb) {
                            geometry.setSRID(v.getSrid());
                        }
                        final String wkt = Geo.toWKT(geometry);
                        g.writeString(wkt);
                    }
                } catch (final SQLException ex) {
                    throw new IOException(ex);
                }
            }

        };

        private static final JsonDeserializer<GeographyType> GEOGRAPHY_DESERIALIZER = new StdDeserializer<GeographyType>(
                GeographyType.class) {

            private static final long serialVersionUID = 1L;

            @Override
            public GeographyType deserialize(final JsonParser p, final DeserializationContext ctx)
                    throws IOException, JsonProcessingException {
                final String wkt = p.getText();
                final Geometry geometry = Geo.fromWKT(wkt);
                final byte[] wkb = Geo.toWKB(geometry);
                final int srid = geometry.getSRID();
                return new GeographyType(wkb, srid);
            }

        };

    }

}
