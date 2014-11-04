package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;

public class DataPropertyExpressionImpl implements DataPropertyExpression {

	private static final long serialVersionUID = 500873858691854474L;

	private final boolean isInverse;
	private final Predicate predicate;
	private final String string;
	public final DataPropertyExpressionImpl inverseProperty;

	DataPropertyExpressionImpl(Predicate p, boolean isInverse) {
		this.predicate = p;
		this.isInverse = isInverse;
		this.inverseProperty = new DataPropertyExpressionImpl(p, !isInverse, this);
		StringBuilder bf = new StringBuilder();
		bf.append(predicate.toString());
		if (isInverse) 
			bf.append("^-");
		this.string =  bf.toString();
	}

	private DataPropertyExpressionImpl(Predicate p, boolean isInverse, DataPropertyExpressionImpl inverseProperty) {
		this.predicate = p;
		this.isInverse = isInverse;
		this.inverseProperty = inverseProperty;
		StringBuilder bf = new StringBuilder();
		bf.append(predicate.toString());
		if (isInverse) 
			bf.append("^-");
		this.string =  bf.toString();
	}

	@Override
	public boolean isInverse() {
		return isInverse;
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}
	
	@Override
	public DataPropertyExpressionImpl getInverse() {
		throw new RuntimeException("create inverse of DataProperty");
		//return inverseProperty;
	}	

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataPropertyExpressionImpl) {
			DataPropertyExpressionImpl other = (DataPropertyExpressionImpl) obj;
			return (isInverse == other.isInverse) && predicate.equals(other.predicate);
		}
		
		// the two types of properties share the same name space
		
		if (obj instanceof ObjectPropertyExpressionImpl) {
			ObjectPropertyExpressionImpl other = (ObjectPropertyExpressionImpl) obj;
			return (isInverse == other.isInverse()) && predicate.equals(other.getPredicate());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}
	
	@Override
	public String toString() {
		return string;
	}

}
