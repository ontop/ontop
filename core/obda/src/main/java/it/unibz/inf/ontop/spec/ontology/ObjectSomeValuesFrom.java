package it.unibz.inf.ontop.spec.ontology;

/**
 * Represents ObjectSomeValuesFrom from OWl 2 Specification
 * 
 * A non-qualified property some restriction. 
 * 
 * Corresponds to DL "exists Property"
 */



public interface ObjectSomeValuesFrom extends ClassExpression {
	
	ObjectPropertyExpression getProperty();

}
