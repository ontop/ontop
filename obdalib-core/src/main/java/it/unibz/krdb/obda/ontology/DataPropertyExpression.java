package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;

public interface DataPropertyExpression extends PropertyExpression {

	public boolean isInverse();

	public Predicate getPredicate();
	
	public DataPropertyExpression getInverse();

}
