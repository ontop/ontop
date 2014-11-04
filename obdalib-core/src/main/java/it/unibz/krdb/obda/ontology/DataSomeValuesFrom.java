package it.unibz.krdb.obda.ontology;


/**
 * Represents DataSomeValuesFrom from OWL 2 Specification
 * 
 * A non-qualified property some restriction. 
 * 
 * Corresponds to DL "exists Property"
 */

public interface DataSomeValuesFrom extends ClassExpression {

	public DataPropertyExpression getProperty();	

}
