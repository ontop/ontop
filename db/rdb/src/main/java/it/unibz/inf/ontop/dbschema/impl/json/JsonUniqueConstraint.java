package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.*;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "determinants",
        "isPrimaryKey"
})
public class JsonUniqueConstraint {
    public String name;
    public List<String> determinants;
    public Boolean isPrimaryKey;

    public JsonUniqueConstraint() {
        // no-op for jackson deserialisation
    }

    public JsonUniqueConstraint(UniqueConstraint uc) {
        this.name = uc.getName();
        this.isPrimaryKey = uc.isPrimaryKey();
        this.determinants = uc.getAttributes().stream()
                .map(a -> a.getID().getSQLRendering())
                .collect(ImmutableCollectors.toList());
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
