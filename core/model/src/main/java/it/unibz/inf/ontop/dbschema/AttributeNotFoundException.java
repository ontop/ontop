package it.unibz.inf.ontop.dbschema;

public class AttributeNotFoundException extends Exception {

    private final RelationDefinition relation;
    private final QuotedID attributeId;

    public AttributeNotFoundException(RelationDefinition relation, QuotedID attributeId) {
        this.relation = relation;
        this.attributeId = attributeId;
    }

    public QuotedID getAttributeID() { return attributeId; }

    public RelationDefinition getRelation() { return relation; }

    @Override
    public String toString() {
        return "AttributeNotFoundException{" +
                "relation=" + relation +
                ", attributeId=" + attributeId +
                '}';
    }
}
