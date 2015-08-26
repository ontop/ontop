package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.ValueConstant;

/**
 * factory for ABox assertions 
 * 
 * IMPORTANT: this factory does NOT check whether the class / property has been declared
 *            it also does not check whether it is top/bottom class / property
 * 
 * @author roman
 *
 */

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
