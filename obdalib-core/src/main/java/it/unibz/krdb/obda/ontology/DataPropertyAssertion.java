package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.ValueConstant;

public interface DataPropertyAssertion extends Assertion {

	public DataPropertyExpression getProperty();
	
	public ObjectConstant getSubject();
	
	public ValueConstant getValue();	

}
