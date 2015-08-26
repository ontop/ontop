package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.ValueConstant;

public interface AssertionFactory {

	/**
	 * creates a class assertion 
	 * 
	 * @param concept
	 * @param o
	 * @return
	 */
	
	public ClassAssertion createClassAssertion(String className, ObjectConstant o);

	/**
	 * creates an object property assertion 
	 * (ensures that the property is not inverse by swapping arguments if necessary)
	 * 
	 * @param prop
	 * @param o1
	 * @param o2
	 * @return
	 */
	
	public ObjectPropertyAssertion createObjectPropertyAssertion(String propertyName, ObjectConstant o1, ObjectConstant o2);

	/**
	 * creates a data property assertion 
	 * 
	 * @param prop
	 * @param o1
	 * @param o2
	 * @return
	 */
	
	public DataPropertyAssertion createDataPropertyAssertion(String propertyName, ObjectConstant o1, ValueConstant o2);
		
}
