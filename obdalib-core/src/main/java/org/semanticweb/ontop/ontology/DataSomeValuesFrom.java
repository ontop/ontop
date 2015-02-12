package org.semanticweb.ontop.ontology;


/**
 * Represents DataSomeValuesFrom from OWL 2 Specification
 * 
 * DataSomeValuesFrom := 'DataSomeValuesFrom' '(' DataPropertyExpression { DataPropertyExpression } DataRange ')'
 * 
 * DataRange is always assumed to be owl:TopDataProperty due to the NORMALIZATION of the ontology
 * (auxiliary data sub-properties are introduced if needed)
 */

public interface DataSomeValuesFrom extends ClassExpression {

	public DataPropertyExpression getProperty();	
}
