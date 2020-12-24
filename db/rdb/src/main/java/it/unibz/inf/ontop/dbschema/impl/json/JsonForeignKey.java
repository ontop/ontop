package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DatabaseTableDefinition;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.List;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "from",
        "to"
})
public class JsonForeignKey extends JsonOpenObject  {
    public final String name;
    public final Part from, to;

    @JsonCreator
    public JsonForeignKey(@JsonProperty("name") String name,
                          @JsonProperty("from") Part from,
                          @JsonProperty("to") Part to) {
        this.name = name;
        this.from = from;
        this.to = to;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "relation",
            "columns"
    })
    public static class Part extends JsonOpenObject {
        public final Object relation;
        public final List<String> columns;

        @JsonCreator
        public Part(@JsonProperty("relation") Object relation,
                    @JsonProperty("columns") List<String> columns) {
            this.relation = relation;
            this.columns = columns;
        }

        public Part(NamedRelationDefinition relation, Stream<Attribute> attributes) {
            this.relation = JsonMetadata.serializeRelationID(relation.getID());
            this.columns = JsonMetadata.serializeAttributeList(attributes);
        }
    }
}
