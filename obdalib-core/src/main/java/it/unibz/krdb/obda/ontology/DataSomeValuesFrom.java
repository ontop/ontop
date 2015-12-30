package it.unibz.krdb.obda.ontology;


/**
 * Represents DataSomeValuesFrom from OWL 2 Specification
 * 
 * DataSomeValuesFrom := 'DataSomeValuesFrom' '(' DataPropertyExpression { DataPropertyExpression } DataRange ')'
 * 
 */

public interface DataSomeValuesFrom extends ClassExpression {

	public DataPropertyExpression getProperty();	
	
	public Datatype getDatatype();
}
