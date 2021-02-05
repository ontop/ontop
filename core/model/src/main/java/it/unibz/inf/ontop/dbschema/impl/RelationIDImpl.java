package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;

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
    public ImmutableList<QuotedID> getComponents() {
        return components;
    }

    /**
     * Used in SQLParser for creating implicit aliases
     * @return the relation ID that has the same name but no schema name
     */
    @Override
    public RelationID getTableOnlyID() {
        return new RelationIDImpl(ImmutableList.of(components.get(TABLE_INDEX)));
    }

    public QuotedID getTableID() {
        return components.get(TABLE_INDEX);
    }

    /**
     * NOT USED!!!
     * @return null if the schema name is empty or the schema name (as is, without quotation marks)
     */
    public QuotedID getSchemaID() {
        return components.get(1);
    }

    /**
     *
     * @return SQL rendering of the name (possibly with quotation marks)
     */
    @Override
    public String getSQLRendering() {
        return components.reverse().stream()
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
}
