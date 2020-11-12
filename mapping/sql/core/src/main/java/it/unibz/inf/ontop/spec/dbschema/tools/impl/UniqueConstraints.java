package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public @JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "determinants", "isPrimaryKey" })
class UniqueConstraints {

    @JsonProperty("name")
    private String name;

    @JsonProperty("determinants")
    private List<String> determinants;

    @JsonProperty("isPrimaryKey")
    private boolean isPrimaryKey;

    /*@JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();*/

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("determinants")
    public void setDeterminants(List<String> determinants) {
        this.determinants = determinants;
    }

    @JsonProperty("isPrimaryKey")
    public boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    @JsonProperty("isPrimaryKey")
    public void setIsPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    /*@JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }*/
}
