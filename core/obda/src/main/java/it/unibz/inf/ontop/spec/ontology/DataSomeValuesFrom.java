package it.unibz.inf.ontop.spec.ontology;


/**
 * Represents DataSomeValuesFrom from OWL 2 Specification
 * 
 * DataSomeValuesFrom := 'DataSomeValuesFrom' '(' DataPropertyExpression { DataPropertyExpression } DataRange ')'
 * 
 */

public interface DataSomeValuesFrom extends ClassExpression {

	DataPropertyExpression getProperty();
	
	Datatype getDatatype();
}
