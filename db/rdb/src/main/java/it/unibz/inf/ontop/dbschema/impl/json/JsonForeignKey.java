package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DatabaseTableDefinition;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
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
    public Part from, to;

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

    public void insert(DatabaseTableDefinition relation, MetadataLookup lookup) throws MetadataExtractionException {

        ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(name, relation,
                lookup.getRelation(JsonMetadata.deserializeRelationID(lookup.getQuotedIDFactory(), to.relation)));

        try {
            for (int i = 0; i < from.columns.size(); i++)
                builder.add(lookup.getQuotedIDFactory().createAttributeID(from.columns.get(i)),
                        lookup.getQuotedIDFactory().createAttributeID(to.columns.get(i)));
        }
        catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(e);
        }

        builder.build();
    }

    private final Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "relation",
            "columns"
    })
    public static class Part {
        public Object relation;
        public List<String> columns;

        public Part() {
            // no-op for jackson deserialisation
        }

        public Part(DatabaseRelationDefinition relation, Stream<Attribute> attributes) {
            this.relation = JsonMetadata.serializeRelationID(relation.getID());
            this.columns = attributes
                    .map(a -> a.getID().getSQLRendering())
                    .collect(ImmutableCollectors.toList());
        }

        private final Map<String, Object> additionalProperties = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            additionalProperties.put(name, value);
        }
    }
}
