package it.unibz.krdb.obda.ontology;

import java.util.Collection;

public interface OntologyVocabulary {


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
	
	
	
	
	
	/**
	 * declare class
	 * 
	 * @param uri
	 * @return
	 */
	
	public OClass createClass(String uri);

	/**
	 * declare object property
	 * 
	 * @param uri property name
	 * @return
	 */
	
	public ObjectPropertyExpression createObjectProperty(String uri);

	/**
	 * declare data property
	 * 
	 * @param uri property name
	 * @return
	 */
	
	public DataPropertyExpression createDataProperty(String uri);

	
		
	
	
	
	public void merge(OntologyVocabulary v);

	
	public boolean removeClass(String classname);

	public boolean removeObjectProperty(String property);

	public boolean removeDataProperty(String property);
	
	
}
