package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory.Supplier;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import org.eclipse.jdt.annotation.NonNullByDefault;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "relations"
})
@NonNullByDefault
public class JsonMetadata extends JsonOpenObject {

    public final List<JsonDatabaseTable> relations;

    public final Parameters metadata;

    @JsonCreator
    public JsonMetadata(@JsonProperty("relations") @Nullable List<JsonDatabaseTable> relations,
                        @JsonProperty("metadata") Parameters metadata) {
        this.relations = relations != null ? ImmutableList.copyOf(relations) : ImmutableList.of();
        this.metadata = Objects.requireNonNull(metadata);
    }

    public JsonMetadata(ImmutableMetadata metadata) {
        this.relations = metadata.getAllRelations().stream()
                .map(JsonDatabaseTable::new)
                .collect(ImmutableList.toImmutableList());
        this.metadata = new Parameters(metadata.getDBParameters());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    @JsonPropertyOrder({
            "dbmsProductName",
            "dbmsVersion",
            "driverName",
            "driverVersion",
            "quotationString",
            "extractionTime"
    })
    public static class Parameters extends JsonOpenObject {

        @Nullable
        public final String dbmsProductName;

        @Nullable
        public final String dbmsVersion;

        @Nullable
        public final String driverName;

        @Nullable
        public final String driverVersion;

        @Nullable
        public final String quotationString;

        @Nullable
        public final String extractionTime;

        @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
        @Nullable
        public final String idFactoryType;

        @Nullable
        private transient final Supplier idFactorySupplier;

        @Nullable
        private transient QuotedIDFactory cachedIdFactory;

        @JsonCreator
        public Parameters(@JsonProperty("dbmsProductName") @Nullable String dbmsProductName,
                          @JsonProperty("dbmsVersion") @Nullable String dbmsVersion,
                          @JsonProperty("driverName") @Nullable String driverName,
                          @JsonProperty("driverVersion") @Nullable String driverVersion,
                          @JsonProperty("quotationString") @Nullable String quotationString,
                          @JsonProperty("extractionTime") @Nullable String extractionTime,
                          @JsonProperty("idFactoryType") @Nullable String idFactoryType,
                          @JacksonInject @Nullable Supplier idFactorySupplier
        ) {
            this.dbmsProductName = dbmsProductName;
            this.dbmsVersion = dbmsVersion;
            this.driverName = driverName;
            this.driverVersion = driverVersion;
            this.quotationString = quotationString;
            this.extractionTime = extractionTime;
            this.idFactoryType = idFactoryType;
            this.idFactorySupplier = idFactorySupplier;
        }

        public Parameters(DBParameters parameters) {
            this.dbmsProductName = parameters.getDbmsProductName();
            this.dbmsVersion = parameters.getDbmsVersion();
            this.driverName = parameters.getDriverName();
            this.driverVersion = parameters.getDriverVersion();
            this.cachedIdFactory = parameters.getQuotedIDFactory();
            this.quotationString = this.cachedIdFactory.getIDQuotationString();
            String idFactoryType = this.cachedIdFactory.getIDFactoryType();
            this.idFactoryType = !idFactoryType.equals("STANDARD") ? idFactoryType : null;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            this.extractionTime = dateFormat.format(Calendar.getInstance().getTime());
            this.idFactorySupplier = null;
        }

        public QuotedIDFactory createQuotedIDFactory() throws MetadataExtractionException {
            if (cachedIdFactory == null) {
                // Delegate to the supplier (will fail if supplier is not available or idFactoryType is unknown)
                cachedIdFactory = Optional.ofNullable(idFactorySupplier)
                        .flatMap(s -> s.get(MoreObjects.firstNonNull(idFactoryType, "STANDARD")))
                        .orElseThrow(() -> new MetadataExtractionException(
                                "Could not resolve QuotedIDFactory with type identifier " + idFactoryType));
            }
            return cachedIdFactory;
        }

    }

    public static ImmutableList<String> serializeRelationID(RelationID id) {
        return id.getComponents().stream()
                .map(QuotedID::getSQLRendering)
                .collect(ImmutableList.toImmutableList()).reverse();
    }

    public static RelationID deserializeRelationID(QuotedIDFactory idFactory, List<String> o) {
        return idFactory.createRelationID(o.toArray(new String[0]));
    }
    
    public static List<String> serializeAttributeList(Stream<Attribute> attributes) {
        return attributes
                .map(a -> a.getID().getSQLRendering())
                .collect(ImmutableList.toImmutableList());
    }

    public interface AttributeConsumer {
        void add(QuotedID id) throws AttributeNotFoundException;
    }

    public static void deserializeAttributeList(QuotedIDFactory idFactory, List<String> ids, AttributeConsumer consumer) throws MetadataExtractionException {
        try {
            for (String id : ids)
                consumer.add(idFactory.createAttributeID(id));
        }
        catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(e);
        }
    }

}
