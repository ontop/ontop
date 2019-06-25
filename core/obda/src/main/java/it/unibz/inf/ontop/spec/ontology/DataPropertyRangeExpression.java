package it.unibz.inf.ontop.spec.ontology;


/**
 * Represents the first argument of DataPropertyRange axiom
 * 
 * @author roman
 *
 */

public interface DataPropertyRangeExpression extends DataRangeExpression {

	DataPropertyExpression getProperty();
	
}
