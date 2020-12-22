package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "from",
        "to"
})
public class JsonForeignKey {
    public String name;
    public Part from;
    public Part to;

    public JsonForeignKey() {
        // no-op for jackson deserialisation
    }

    public JsonForeignKey(ForeignKeyConstraint fk) {
        this.name = fk.getName();
        this.from = new Part(fk.getRelation(), fk.getComponents().stream()
                .map(ForeignKeyConstraint.Component::getAttribute));
        this.to = new Part(fk.getReferencedRelation(), fk.getComponents().stream()
                .map(ForeignKeyConstraint.Component::getReferencedAttribute));
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "relation",
            "columns"
    })
    public static class Part {
        public String relation;
        public List<String> columns;

        public Part() {
            // no-op for jackson deserialisation
        }

        public Part(DatabaseRelationDefinition relation, Stream<Attribute> attributes) {
            this.relation = relation.getID().getSQLRendering();
            this.columns = attributes
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
}
