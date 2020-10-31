package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;

import java.io.IOException;
import java.util.stream.Collectors;

public class RelationIDImpl implements RelationID {
    private final ImmutableList<QuotedID> components;

    /**
     * (used only in QuotedIDFactory implementations)
     *
     */

    RelationIDImpl(ImmutableList<QuotedID> components) {
        this.components = components;
    }

    @Override
    @JsonIgnore
    public ImmutableList<QuotedID> getComponents() {
        return components;
    }

    /**
     *
     * @return the relation ID that has the same name but no schema name
     */
    @JsonIgnore
    @Override
    public RelationID getTableOnlyID() {
        return new RelationIDImpl(ImmutableList.of(components.get(TABLE_INDEX)));
    }

    @JsonProperty("name")
    public QuotedID getTableID() {
        return components.get(TABLE_INDEX);
    }

    /**
     *
     * @return null if the schema name is empty or the schema name (as is, without quotation marks)
     */
    @JsonProperty("schema")
    public QuotedID getSchemaID() {
        return components.get(1);
    }

    /**
     *
     * @return SQL rendering of the name (possibly with quotation marks)
     */
    @JsonIgnore
    @Override
    public String getSQLRendering() {
        return components.reverse().stream()
                .filter(c -> c.getName() != null)
                .map(QuotedID::getSQLRendering)
                .collect(Collectors.joining("."));
    }

    @Override
    public String toString() {
        return getSQLRendering();
    }

    @Override
    public int hashCode() {
        return components.get(TABLE_INDEX).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof RelationIDImpl) {
            RelationIDImpl other = (RelationIDImpl)obj;
            return this.components.equals(other.components);
        }

        return false;
    }

    public static class RelationIDSerializer extends JsonSerializer<RelationID> {
        @Override
        public void serialize(RelationID value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getSQLRendering());
        }
    }
}
