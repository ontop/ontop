package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;

public interface ObjectPropertyExpression extends Description {

	public boolean isInverse();

	public Predicate getPredicate();
	
	public ObjectPropertyExpression getInverse();

	public ObjectSomeValuesFrom getDomain();
	
	public ObjectSomeValuesFrom getRange();
	
}
