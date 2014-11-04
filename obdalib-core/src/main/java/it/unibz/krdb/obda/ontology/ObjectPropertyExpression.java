package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;

public interface ObjectPropertyExpression extends PropertyExpression {

	public boolean isInverse();

	public Predicate getPredicate();
	
	public ObjectPropertyExpression getInverse();

}
