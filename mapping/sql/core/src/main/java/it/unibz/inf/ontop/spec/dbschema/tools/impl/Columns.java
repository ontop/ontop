package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

public class Columns {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "name", "isNullable", "datatype" })

    @JsonProperty("name")
    private String name;

    @JsonProperty("isNullable")
    private boolean isNullable;

    @JsonProperty("datatype")
    private String datatype;

    /*@JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();*/

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("isNullable")
    public void setIsNullable(boolean isNullable) {
        this.isNullable = isNullable;
    }

    @JsonProperty("datatype")
    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    /*@JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        }*/
}
