package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "uniqueConstraints", "otherFunctionalDependencies", "foreignKeys", "columns", "name" })
public class Relations {
    @JsonProperty("uniqueConstraints")
    private List<UniqueConstraints> uniqueConstraints;

    @JsonProperty("otherFunctionalDependencies")
    private List<String> otherFunctionalDependencies;

    @JsonProperty("foreignKeys")
    private List<ForeignKeys> foreignKeys;

    @JsonProperty("columns")
    private List<Columns> columns;

    @JsonProperty("name")
    private String name;

    /*@JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();*/

    @JsonProperty("uniqueConstraints")
    public List<UniqueConstraints> getUniqueConstraints() {
        return uniqueConstraints;
    }

    @JsonProperty("uniqueConstraints")
    public void setUniqueConstraints(List<UniqueConstraints> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
    }

    @JsonProperty("otherFunctionalDependencies")
    public void setOtherFunctionalDependencies(List<String> otherFunctionalDependencies) {
        this.otherFunctionalDependencies = otherFunctionalDependencies;
    }

    @JsonProperty("foreignKeys")
    public void setForeignKeys(List<ForeignKeys> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    @JsonProperty("columns")
    public void setColumns(List<Columns> columns) {
        this.columns = columns;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /*@JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }*/
}
