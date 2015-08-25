package it.unibz.krdb.obda.ontology;

import java.util.Collection;

public interface ImmutableOntologyVocabulary {

	/**
	 * check whether the class has been declared and return the class object
	 * 
	 * @param uri
	 * @return
	 * @throws RuntimeException if the class has not been declared
	 */
	
	public OClass getClass(String uri);
	

	/**
	 * check whether the object property has been declared and return the class object
	 * 
	 * @param uri
	 * @return
	 * @throws RuntimeException if the object property has not been declared
	 */
	
	public ObjectPropertyExpression getObjectProperty(String uri);
	

	/**
	 * check whether the data property has been declared and return the class object
	 * 
	 * @param uri
	 * @return
	 * @throws RuntimeException if the data property has not been declared
	 */
	
	public DataPropertyExpression getDataProperty(String uri);
	
	
	/**
	 * check whether the class has been declared 
	 * 
	 * @param uri
	 * @return
	 */

	public boolean containsClass(String uri);

	/**
	 * check whether the object property has been declared 
	 * 
	 * @param uri
	 * @return
	 */
	
	public boolean containsObjectProperty(String uri);
	
	/**
	 * check whether the data property has been declared 
	 * 
	 * @param uri
	 * @return
	 */
	
	public boolean containsDataProperty(String uri);
	
	/**
	 * return all declared classes
	 * 
	 * @return
	 */	
	
	public Collection<OClass> getClasses();
	
	/**
	 * return all declared object properties
	 * 
	 * @return
	 */
	
	public Collection<ObjectPropertyExpression> getObjectProperties();
	
	/**
	 * return all declared data properties
	 * 
	 * @return
	 */
	
	public Collection<DataPropertyExpression> getDataProperties();
	
	
	/**
	 * check whether the vocabulary is empty (no declared classes, object or data properties)
	 * 
	 * @return
	 */

	public boolean isEmpty();
}
