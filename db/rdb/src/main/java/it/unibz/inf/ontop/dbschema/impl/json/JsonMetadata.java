package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ImmutableBiMap;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.ImmutableMetadata;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.impl.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "relations"
})
public class JsonMetadata {
    public List<JsonDatabaseTable> relations;
    public Parameters metadata;

    public JsonMetadata() {
        // no-op for jackson deserialisation
    }

    public JsonMetadata(ImmutableMetadata metadata) {
        this.relations = metadata.getAllRelations().stream()
                .map(JsonDatabaseTable::new)
                .collect(ImmutableCollectors.toList());
        this.metadata = new Parameters(metadata.getDBParameters());
    }


    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
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
    public static class Parameters {
        public String dbmsProductName;
        public String dbmsVersion;
        public String driverName;
        public String driverVersion;
        public String quotationString;
        public String extractionTime;

        private static final String ID_FACTORY_KEY = "id-factory";

        public Parameters() {
            // no-op for jackson deserialisation
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
            if (idFactoryType != null && !idFactoryType.equals("STANDARD"))
                additionalProperties.put(ID_FACTORY_KEY, idFactoryType);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            extractionTime = dateFormat.format(Calendar.getInstance().getTime());
        }

        public QuotedIDFactory createQuotedIDFactory() throws MetadataExtractionException {
            String idFactoryType = (String)additionalProperties.get(ID_FACTORY_KEY);
            try {
                return QUOTED_ID_FACTORIES.getOrDefault(idFactoryType, SQLStandardQuotedIDFactory.class).newInstance();
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new MetadataExtractionException(e);
            }
        }

        private final Map<String, Object> additionalProperties = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            additionalProperties.put(name, value);
        }
    }
}
