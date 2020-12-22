package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.*;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.ImmutableMetadata;
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
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
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

        public Parameters() {
            // no-op for jackson deserialisation
        }

        public Parameters(DBParameters parameters) {
            dbmsProductName = parameters.getDbmsProductName();
            dbmsVersion = parameters.getDbmsVersion();
            driverName = parameters.getDriverName();
            driverVersion = parameters.getDriverVersion();
            quotationString = parameters.getQuotedIDFactory().getIDQuotationString();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            extractionTime = dateFormat.format(Calendar.getInstance().getTime());
        }

        private final Map<String, Object> additionalProperties = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }
    }
}
