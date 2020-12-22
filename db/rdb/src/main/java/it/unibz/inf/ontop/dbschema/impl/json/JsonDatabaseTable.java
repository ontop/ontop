package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.AttributeImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public JsonDatabaseTable() {
        // no-op for jackson deserialisation
    }

    public JsonDatabaseTable(DatabaseRelationDefinition relation) {
        this.name = relation.getID().getSQLRendering();
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
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }
    }
}
