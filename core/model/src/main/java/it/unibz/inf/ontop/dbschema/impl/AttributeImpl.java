package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.model.type.DBTermType;

public class AttributeImpl implements Attribute {

    private final RelationDefinition relation;

    private final QuotedID id;
    private final int index;
    private final DBTermType termType;
    private final String typeName;
    private final boolean isNullable;

    /**
     * With a term type
     */
    AttributeImpl(RelationDefinition relation, QuotedID id, int index, String typeName,
              DBTermType termType, boolean isNullable) {
        this.relation = relation;
        this.id = id;
        this.index = index;
        this.typeName = typeName;
        this.termType = termType;
        this.isNullable = isNullable;
    }

    @Override
    public RelationDefinition getRelation() { return relation; }

    @Override
    public QuotedID getID() { return id; }

    /**
     * @return the index (starting from 1)
     */
    @Override
    public int getIndex() { return index; }

    @Override
    public boolean isNullable() { return isNullable; }

    /**
     * @return the name of the SQL type associated with this attribute.
     */
    public String getSQLTypeName() { return typeName; }

    /**
     * @return the precise term type
     */
    @Override
    public DBTermType getTermType() { return termType; }

    @Override
    public String toString() {
        return id + (typeName == null ? "" : " " + typeName) + (isNullable ? "" : " NOT NULL");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof AttributeImpl) {
            AttributeImpl other = (AttributeImpl)obj;
            // the same reference(!) for the relation
            return this.id.equals(other.id) && (this.relation == other.relation);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode(); // never mix attributes from different relations
    }

}
