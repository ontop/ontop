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
    public final List<JsonUniqueConstraint> uniqueConstraints;
    public final List<Object> otherFunctionalDependencies; // never used
    public final List<JsonForeignKey> foreignKeys;
    public final List<Column> columns;
    public final Object name;

    private static final String OTHER_NAMES_KEY = "other-names";

    @JsonCreator
    public JsonDatabaseTable(@JsonProperty("uniqueConstraints") List<JsonUniqueConstraint> uniqueConstraints,
                             @JsonProperty("otherFunctionalDependencies")  List<Object> otherFunctionalDependencies,
                             @JsonProperty("foreignKeys")  List<JsonForeignKey> foreignKeys,
                             @JsonProperty("columns")  List<Column> columns,
                             @JsonProperty("name")  Object name) {
        this.uniqueConstraints = uniqueConstraints;
        this.otherFunctionalDependencies = otherFunctionalDependencies;
        this.foreignKeys = foreignKeys;
        this.columns = columns;
        this.name = name;
    }

    public JsonDatabaseTable(DatabaseRelationDefinition relation) {
        this.name = JsonMetadata.serializeRelationID(relation.getID());
        ImmutableList<Object> otherNames = relation.getAllIDs().stream()
                .filter(id -> !id.equals(relation.getID()))
                .map(JsonMetadata::serializeRelationID)
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

        List<Object> otherNames = (List<Object>) additionalProperties.get(OTHER_NAMES_KEY);
        ImmutableList<RelationID> allIDs =
                (otherNames == null ? Stream.of(name) : Stream.concat(Stream.of(name), otherNames.stream()))
                        .map(s -> JsonMetadata.deserializeRelationID(dbParameters.getQuotedIDFactory(), s))
                        .collect(ImmutableCollectors.toList());

        return new DatabaseTableDefinition(allIDs, attributeListBuilder);
    }

    public void insertIntegrityConstraints(MetadataLookup lookup) throws MetadataExtractionException {
        DatabaseTableDefinition relation = (DatabaseTableDefinition)lookup.getRelation(
                JsonMetadata.deserializeRelationID(lookup.getQuotedIDFactory(), name));

        for (JsonUniqueConstraint uc: uniqueConstraints)
            uc.insert(relation, lookup.getQuotedIDFactory());

        for (JsonForeignKey fk : foreignKeys) {
            if (!fk.from.relation.equals(this.name))
                throw new MetadataExtractionException("Table names mismatch: " + name + " != " + fk.from.relation);

            fk.insert(relation, lookup);
        }
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
        public final String name;
        public final Boolean isNullable;
        public final String datatype;

        @JsonCreator
        public Column(@JsonProperty("name") String name,
                      @JsonProperty("isNullable")  Boolean isNullable,
                      @JsonProperty("datatype")  String datatype) {
            this.name = name;
            this.isNullable = isNullable;
            this.datatype = datatype;
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
