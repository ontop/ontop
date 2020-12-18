package it.unibz.inf.ontop.dbschema.impl.JSONRelation;

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
    private String dbmsProductName;
    @JsonProperty("dbmsVersion")
    private String dbmsVersion;
    @JsonProperty("driverName")
    private String driverName;
    @JsonProperty("driverVersion")
    private String driverVersion;
    @JsonProperty("quotationString")
    private String quotationString;
    @JsonProperty("extractionTime")
    private String extractionTime;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("dbmsProductName")
    public String getDbmsProductName() {
        return dbmsProductName;
    }

    @JsonProperty("dbmsProductName")
    public void setDbmsProductName(String dbmsProductName) {
        this.dbmsProductName = dbmsProductName;
    }

    @JsonProperty("dbmsVersion")
    public String getDbmsVersion() {
        return dbmsVersion;
    }

    @JsonProperty("dbmsVersion")
    public void setDbmsVersion(String dbmsVersion) {
        this.dbmsVersion = dbmsVersion;
    }

    @JsonProperty("driverName")
    public String getDriverName() {
        return driverName;
    }

    @JsonProperty("driverName")
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    @JsonProperty("driverVersion")
    public String getDriverVersion() {
        return driverVersion;
    }

    @JsonProperty("driverVersion")
    public void setDriverVersion(String driverVersion) {
        this.driverVersion = driverVersion;
    }

    @JsonProperty("quotationString")
    public String getQuotationString() {
        return quotationString;
    }

    @JsonProperty("quotationString")
    public void setQuotationString(String quotationString) {
        this.quotationString = quotationString;
    }

    @JsonProperty("extractionTime")
    public String getExtractionTime() {
        return extractionTime;
    }

    @JsonProperty("extractionTime")
    public void setExtractionTime(String extractionTime) {
        this.extractionTime = extractionTime;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
