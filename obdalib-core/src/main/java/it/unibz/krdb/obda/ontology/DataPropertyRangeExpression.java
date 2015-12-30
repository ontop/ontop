package it.unibz.krdb.obda.ontology;


/**
 * Represents the first argument of DataPropertyRange axiom
 * 
 * @author roman
 *
 */

public interface DataPropertyRangeExpression extends DataRangeExpression {

	public DataPropertyExpression getProperty();
	
}
