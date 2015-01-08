package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.ObjectConstant;

public interface ObjectPropertyAssertion extends Assertion {

	public ObjectPropertyExpression getProperty();
	
	public ObjectConstant getSubject();
	
	public ObjectConstant getObject();	

}
