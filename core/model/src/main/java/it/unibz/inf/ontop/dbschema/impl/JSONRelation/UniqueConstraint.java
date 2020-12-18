package it.unibz.inf.ontop.dbschema.impl.JSONRelation;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "determinants",
        "isPrimaryKey"
})
public class UniqueConstraint {

    @JsonProperty("name")
    private String name;
    @JsonProperty("determinants")
    private List<String> determinants = null;
    @JsonProperty("isPrimaryKey")
    private Boolean isPrimaryKey;
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

    @JsonProperty("determinants")
    public List<String> getDeterminants() {
        return determinants;
    }

    @JsonProperty("determinants")
    public void setDeterminants(List<String> determinants) {
        this.determinants = determinants;
    }

    @JsonProperty("isPrimaryKey")
    public Boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    @JsonProperty("isPrimaryKey")
    public void setIsPrimaryKey(Boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
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
