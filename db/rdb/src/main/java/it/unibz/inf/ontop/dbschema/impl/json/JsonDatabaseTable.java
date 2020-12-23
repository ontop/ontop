package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.AbstractRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.AttributeImpl;
import it.unibz.inf.ontop.dbschema.impl.DatabaseTableDefinition;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({   // Why is this "reversed order"?
    "uniqueConstraints",
    "otherFunctionalDependencies",
    "foreignKeys",
    "columns",
    "name"
})
public class JsonDatabaseTable {
    public List<JsonUniqueConstraint> uniqueConstraints;
    public List<Object> otherFunctionalDependencies; // never used
    public List<JsonForeignKey> foreignKeys;
    public List<Column> columns;
    public String name;

    private static final String OTHER_NAMES_KEY = "other-names";

    public JsonDatabaseTable() {
        // no-op for jackson deserialisation
    }

    public JsonDatabaseTable(DatabaseRelationDefinition relation) {
        this.name = relation.getID().getSQLRendering();
        ImmutableList<String> otherNames = relation.getAllIDs().stream()
                .filter(id -> !id.equals(relation.getID()))
                .map(RelationID::getSQLRendering)
                .collect(ImmutableCollectors.toList());
        if (!otherNames.isEmpty())
            additionalProperties.put(OTHER_NAMES_KEY, otherNames);
        this.columns = relation.getAttributes().stream()
                .map(Column::new)
                .collect(ImmutableCollectors.toList());
        this.foreignKeys = relation.getForeignKeys().stream()
                .map(JsonForeignKey::new)
                .collect(ImmutableCollectors.toList());
        this.uniqueConstraints = relation.getUniqueConstraints().stream()
                .map(JsonUniqueConstraint::new)
                .collect(ImmutableCollectors.toList());
        this.otherFunctionalDependencies = ImmutableList.of();
    }

    public DatabaseTableDefinition createDatabaseTableDefinition(DBParameters dbParameters) {
        DBTypeFactory dbTypeFactory = dbParameters.getDBTypeFactory();
        QuotedIDFactory idFactory = dbParameters.getQuotedIDFactory();
        RelationDefinition.AttributeListBuilder attributeListBuilder = AbstractRelationDefinition.attributeListBuilder();
        for (Column attribute: columns)
            attributeListBuilder.addAttribute(
                    idFactory.createAttributeID(attribute.name),
                    dbTypeFactory.getDBTermType(attribute.datatype),
                    attribute.isNullable);

        List<String> otherNames = (List<String>) additionalProperties.get(OTHER_NAMES_KEY);
        ImmutableList<RelationID> allIDs =
                (otherNames == null ? Stream.of(name) : Stream.concat(Stream.of(name), otherNames.stream()))
                .map(s -> deserializeRelationID(idFactory, s))
                .collect(ImmutableCollectors.toList());

        return new DatabaseTableDefinition(allIDs, attributeListBuilder);
    }

    public void insertIntegrityConstraints(MetadataLookup lookup) throws MetadataExtractionException {
        RelationID id = deserializeRelationID(lookup.getQuotedIDFactory(), name);
        DatabaseTableDefinition relation = (DatabaseTableDefinition)lookup.getRelation(id);

        for (JsonUniqueConstraint uc: uniqueConstraints)
            uc.insert(relation, lookup.getQuotedIDFactory());

        for (JsonForeignKey fk : foreignKeys) {
            if (!fk.from.relation.equals(this.name))
                throw new MetadataExtractionException("Table names mismatch: " + name + " != " + fk.from.relation);

            fk.insert(relation, lookup);
        }
    }

    private static RelationID deserializeRelationID(QuotedIDFactory idFactory, String s) {
        return idFactory.createRelationID(s.split("\\."));
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
            "name",
            "isNullable",
            "datatype"
    })
    public static class Column {
        public String name;
        public Boolean isNullable;
        public String datatype;

        public Column() {
            // no-op for jackson deserialisation
        }

        public Column(Attribute attribute) {
            this.name = attribute.getID().getSQLRendering();
            this.isNullable = attribute.isNullable();
            this.datatype = ((AttributeImpl)attribute).getSQLTypeName();
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
