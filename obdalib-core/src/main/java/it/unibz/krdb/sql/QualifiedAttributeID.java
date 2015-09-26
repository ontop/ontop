package it.unibz.krdb.sql;

public class QualifiedAttributeID {

	private final QuotedID attribute;
	private final RelationID relation;
	
	public QualifiedAttributeID(RelationID relation, QuotedID attribute) {
		this.relation = relation;
		this.attribute = attribute;
	}
	
	public QuotedID getAttribute() {
		return attribute;
	}
	
	public RelationID getRelation() {
		return relation;
	}
	
	public String getSQLRendering() {
		return ((relation == null) ? "" : (relation.getSQLRendering() + ".")) + attribute.getSQLRendering();
	}
	
	@Override 
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj instanceof QualifiedAttributeID) {
			QualifiedAttributeID other = (QualifiedAttributeID)obj;
			return (this.attribute.equals(other.attribute) && 
					((this.relation == other.relation) || 
							((this.relation != null) && this.relation.equals(other.relation))));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return attribute.hashCode();
	}
	
	@Override
	public String toString() {
		return getSQLRendering();
	}
}
