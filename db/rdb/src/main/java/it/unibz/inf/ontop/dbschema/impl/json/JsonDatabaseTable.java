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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({   // Why is this "reversed order"?
    "uniqueConstraints",
    "otherFunctionalDependencies",
    "foreignKeys",
    "columns",
    "name"
})
public class JsonDatabaseTable extends JsonOpenObject {
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    public final List<JsonUniqueConstraint> uniqueConstraints;
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    public final List<JsonFunctionalDependency> otherFunctionalDependencies;
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    public final List<JsonForeignKey> foreignKeys;
    public final List<Column> columns;
    public final List<String> name;
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    public final List<List<String>> otherNames;

    @JsonCreator
    public JsonDatabaseTable(@JsonProperty("uniqueConstraints") List<JsonUniqueConstraint> uniqueConstraints,
                             @JsonProperty("otherFunctionalDependencies") List<JsonFunctionalDependency> otherFunctionalDependencies,
                             @JsonProperty("foreignKeys") List<JsonForeignKey> foreignKeys,
                             @JsonProperty("columns") List<Column> columns,
                             @JsonProperty("name") List<String> name,
                             @JsonProperty("otherNames") List<List<String>> otherNames) {
        this.uniqueConstraints = Optional.ofNullable(uniqueConstraints).orElse(ImmutableList.of());
        this.otherFunctionalDependencies = Optional.ofNullable(otherFunctionalDependencies).orElse(ImmutableList.of());
        this.foreignKeys =  Optional.ofNullable(foreignKeys).orElse(ImmutableList.of());
        this.columns = columns;
        this.name = name;
        this.otherNames = Optional.ofNullable(otherNames).orElse(ImmutableList.of());
    }

    public JsonDatabaseTable(NamedRelationDefinition relation) {
        this.name = JsonMetadata.serializeRelationID(relation.getID());
        this.otherNames = relation.getAllIDs().stream()
                .filter(id -> !id.equals(relation.getID()))
                .map(JsonMetadata::serializeRelationID)
                .collect(ImmutableCollectors.toList());
        this.columns = relation.getAttributes().stream()
                .map(Column::new)
                .collect(ImmutableCollectors.toList());
        this.foreignKeys = relation.getForeignKeys().stream()
                .map(JsonForeignKey::new)
                .collect(ImmutableCollectors.toList());
        this.uniqueConstraints = relation.getUniqueConstraints().stream()
                .map(JsonUniqueConstraint::new)
                .collect(ImmutableCollectors.toList());
        this.otherFunctionalDependencies = relation.getOtherFunctionalDependencies().stream()
                .map(JsonFunctionalDependency::new)
                .collect(ImmutableCollectors.toList());
    }

    public DatabaseTableDefinition createDatabaseTableDefinition(DBParameters dbParameters) {
        DBTypeFactory dbTypeFactory = dbParameters.getDBTypeFactory();
        QuotedIDFactory idFactory = dbParameters.getQuotedIDFactory();
        RelationDefinition.AttributeListBuilder attributeListBuilder = AbstractRelationDefinition.attributeListBuilder();
        for (Column attribute: columns)
            attributeListBuilder.addAttribute(
                    idFactory.createAttributeID(attribute.name),
                    attribute.datatype != null
                            ? dbTypeFactory.getDBTermType(attribute.datatype)
                            :dbTypeFactory.getAbstractRootDBType(),
                    attribute.isNullable);

        ImmutableList<RelationID> allIDs = Stream.concat(Stream.of(name), otherNames.stream())
                .map(s -> JsonMetadata.deserializeRelationID(dbParameters.getQuotedIDFactory(), s))
                .collect(ImmutableCollectors.toList());

        return new DatabaseTableDefinition(allIDs, attributeListBuilder);
    }

    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup lookupForFk) throws MetadataExtractionException {

        for (JsonUniqueConstraint uc: uniqueConstraints)
            uc.insert(relation, lookupForFk.getQuotedIDFactory());

        for (JsonFunctionalDependency fd: otherFunctionalDependencies)
            fd.insert(relation, lookupForFk.getQuotedIDFactory());

        for (JsonForeignKey fk : foreignKeys) {
            if (!fk.from.relation.equals(this.name))
                throw new MetadataExtractionException("Table names mismatch: " + name + " != " + fk.from.relation);

            fk.insert(relation, lookupForFk);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "name",
            "isNullable",
            "datatype"
    })
    public static class Column extends JsonOpenObject {
        public final String name;
        public final Boolean isNullable;
        @Nullable
        public final String datatype;

        @JsonCreator
        public Column(@JsonProperty("name") String name,
                      @JsonProperty("isNullable") Boolean isNullable,
                      @Nullable @JsonProperty("datatype") String datatype) {
            this.name = name;
            this.isNullable = isNullable;
            this.datatype = datatype;
        }

        public Column(Attribute attribute) {
            this.name = attribute.getID().getSQLRendering();
            this.isNullable = attribute.isNullable();
            this.datatype = attribute.getTermType().isAbstract()
                    ? null
                    : ((AttributeImpl)attribute).getSQLTypeName();
        }
    }
}
