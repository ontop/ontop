package it.unibz.inf.ontop.dbschema.impl.json;

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
    public List<UniqueConstraint> uniqueConstraints;
    @JsonProperty("otherFunctionalDependencies")
    public List<Object> otherFunctionalDependencies;
    @JsonProperty("foreignKeys")
    public List<ForeignKey> foreignKeys;
    @JsonProperty("columns")
    public List<Column> columns;
    @JsonProperty("name")
    public String name;
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
