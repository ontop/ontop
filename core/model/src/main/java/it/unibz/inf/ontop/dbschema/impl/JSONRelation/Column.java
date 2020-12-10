package it.unibz.inf.ontop.dbschema.impl.JSONRelation;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "isNullable",
    "datatype"
})
public class Column {

    @JsonProperty("name")
    private String name;
    @JsonProperty("isNullable")
    private Boolean isNullable;
    @JsonProperty("datatype")
    private String datatype;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("isNullable")
    public Boolean getIsNullable() {
        return isNullable;
    }

    @JsonProperty("isNullable")
    public void setIsNullable(Boolean isNullable) {
        this.isNullable = isNullable;
    }

    @JsonProperty("datatype")
    public String getDatatype() {
        return datatype;
    }

    @JsonProperty("datatype")
    public void setDatatype(String datatype) {
        this.datatype = datatype;
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
