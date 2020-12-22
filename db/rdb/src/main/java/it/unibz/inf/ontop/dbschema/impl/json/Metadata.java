package it.unibz.inf.ontop.dbschema.impl.json;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "dbmsProductName",
        "dbmsVersion",
        "driverName",
        "driverVersion",
        "quotationString",
        "extractionTime"
})
public class Metadata {

    @JsonProperty("dbmsProductName")
    public String dbmsProductName;
    @JsonProperty("dbmsVersion")
    public String dbmsVersion;
    @JsonProperty("driverName")
    public String driverName;
    @JsonProperty("driverVersion")
    public String driverVersion;
    @JsonProperty("quotationString")
    public String quotationString;
    @JsonProperty("extractionTime")
    public String extractionTime;
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

}
