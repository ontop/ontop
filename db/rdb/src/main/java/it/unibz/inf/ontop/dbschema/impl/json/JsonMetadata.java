package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "relations"
})
public class JsonMetadata extends JsonOpenObject {
    public final List<JsonDatabaseTable> relations;
    public final Parameters metadata;

    @JsonCreator
    public JsonMetadata(@JsonProperty("relations") List<JsonDatabaseTable> relations,
                        @JsonProperty("metadata") Parameters metadata) {
        this.relations = relations;
        this.metadata = metadata;
    }

    public JsonMetadata(ImmutableMetadata metadata) {
        this.relations = metadata.getAllRelations().stream()
                .map(JsonDatabaseTable::new)
                .collect(ImmutableCollectors.toList());
        this.metadata = new Parameters(metadata.getDBParameters());
    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "dbmsProductName",
            "dbmsVersion",
            "driverName",
            "driverVersion",
            "quotationString",
            "extractionTime"
    })
    public static class Parameters extends JsonOpenObject {
        public final String dbmsProductName;
        public final String dbmsVersion;
        public final String driverName;
        public final String driverVersion;
        public final String quotationString;
        public final String extractionTime;
        @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
        public final String idFactoryType;

        @JsonCreator
        public Parameters(@JsonProperty("dbmsProductName") String dbmsProductName,
                          @JsonProperty("dbmsVersion") String dbmsVersion,
                          @JsonProperty("driverName") String driverName,
                          @JsonProperty("driverVersion") String driverVersion,
                          @JsonProperty("quotationString") String quotationString,
                          @JsonProperty("extractionTime") String extractionTime,
                          @JsonProperty("idFactoryType") String idFactoryType) {
            this.dbmsProductName = dbmsProductName;
            this.dbmsVersion = dbmsVersion;
            this.driverName = driverName;
            this.driverVersion = driverVersion;
            this.quotationString = quotationString;
            this.extractionTime = extractionTime;
            this.idFactoryType = idFactoryType;
        }

        private static final ImmutableBiMap<String, Class<? extends QuotedIDFactory>> QUOTED_ID_FACTORIES = ImmutableBiMap.<String, Class<? extends QuotedIDFactory>>builder()
                .put("STANDARD", SQLStandardQuotedIDFactory.class)
                .put("DREMIO", DremioQuotedIDFactory.class)
                .put("MYSQL-U", MySQLCaseSensitiveTableNamesQuotedIDFactory.class)
                .put("MYSQL-D", MySQLCaseNotSensitiveTableNamesQuotedIDFactory.class)
                .put("POSTGRESQL", PostgreSQLQuotedIDFactory.class)
                .put("MSSQLSERVER", SQLServerQuotedIDFactory.class)
                .build();

        public Parameters(DBParameters parameters) {
            dbmsProductName = parameters.getDbmsProductName();
            dbmsVersion = parameters.getDbmsVersion();
            driverName = parameters.getDriverName();
            driverVersion = parameters.getDriverVersion();
            QuotedIDFactory idFactory = parameters.getQuotedIDFactory();
            quotationString = idFactory.getIDQuotationString();
            String idFactoryType = QUOTED_ID_FACTORIES.inverse().get(idFactory.getClass());
            this.idFactoryType = (idFactoryType != null && !idFactoryType.equals("STANDARD")) ? idFactoryType : null;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            extractionTime = dateFormat.format(Calendar.getInstance().getTime());
        }

        public QuotedIDFactory createQuotedIDFactory() throws MetadataExtractionException {
            try {
                return QUOTED_ID_FACTORIES.getOrDefault(idFactoryType, SQLStandardQuotedIDFactory.class).newInstance();
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new MetadataExtractionException(e);
            }
        }
    }

    public static ImmutableList<String> serializeRelationID(RelationID id) {
        return id.getComponents().stream()
                .map(QuotedID::getSQLRendering)
                .collect(ImmutableCollectors.toList()).reverse();
    }

    public static RelationID deserializeRelationID(QuotedIDFactory idFactory, List<String> o) {
        return idFactory.createRelationID(o.toArray(new String[0]));
    }
    
    public static List<String> serializeAttributeList(Stream<Attribute> attributes) {
        return attributes
                .map(a -> a.getID().getSQLRendering())
                .collect(ImmutableCollectors.toList());
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
