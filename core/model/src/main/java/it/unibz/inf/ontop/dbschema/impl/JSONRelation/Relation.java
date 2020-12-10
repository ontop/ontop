package it.unibz.inf.ontop.dbschema.impl.JSONRelation;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "uniqueConstraints",
    "otherFunctionalDependencies",
    "foreignKeys",
    "columns",
    "name"
})
public class Relation {

    @JsonProperty("uniqueConstraints")
    private List<UniqueConstraint> uniqueConstraints = null;
    @JsonProperty("otherFunctionalDependencies")
    private List<Object> otherFunctionalDependencies = null;
    @JsonProperty("foreignKeys")
    private List<ForeignKey> foreignKeys = null;
    @JsonProperty("columns")
    private List<Column> columns = null;
    @JsonProperty("name")
    private String name;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("uniqueConstraints")
    public List<UniqueConstraint> getUniqueConstraints() {
        return uniqueConstraints;
    }

    @JsonProperty("uniqueConstraints")
    public void setUniqueConstraints(List<UniqueConstraint> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
    }

    @JsonProperty("otherFunctionalDependencies")
    public List<Object> getOtherFunctionalDependencies() {
        return otherFunctionalDependencies;
    }

    @JsonProperty("otherFunctionalDependencies")
    public void setOtherFunctionalDependencies(List<Object> otherFunctionalDependencies) {
        this.otherFunctionalDependencies = otherFunctionalDependencies;
    }

    @JsonProperty("foreignKeys")
    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    @JsonProperty("foreignKeys")
    public void setForeignKeys(List<ForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    @JsonProperty("columns")
    public List<Column> getColumns() {
        return columns;
    }

    @JsonProperty("columns")
    public void setColumns(List<Column> columns) {
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
