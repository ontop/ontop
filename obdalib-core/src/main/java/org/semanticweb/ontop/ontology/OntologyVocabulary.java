package org.semanticweb.ontop.ontology;

import java.util.Set;

public interface OntologyVocabulary {

	/**
	 * create and declare class
	 * 
	 * @param uri
	 * @return
	 */
	
	public OClass createClass(String uri);

	/**
	 * check whether the class has been declared and return the class object
	 * 
	 * @param uri
	 * @return
	 * @throws RuntimeException if the class has not been declared
	 */
	
	public OClass getClass(String uri);
	
	/**
	 * create and declare object property
	 * 
	 * @param uri property name
	 * @return
	 */
	
	public ObjectPropertyExpression createObjectProperty(String uri);

	/**
	 * check whether the object property has been declared and return the class object
	 * 
	 * @param uri
	 * @return
	 * @throws RuntimeException if the object property has not been declared
	 */
	
	public ObjectPropertyExpression getObjectProperty(String uri);
	
	/**
	 * create and declare data property
	 * 
	 * @param uri property name
	 * @return
	 */
	
	public DataPropertyExpression createDataProperty(String uri);

	/**
	 * check whether the data property has been declared and return the class object
	 * 
	 * @param uri
	 * @return
	 * @throws RuntimeException if the data property has not been declared
	 */
	
	public DataPropertyExpression getDataProperty(String uri);
	
	/**
	 * auxiliary properties result from NORMALIZATION
	 * @param uri
	 */

	public ObjectPropertyExpression createAuxiliaryObjectProperty();
	
	/**
	 * auxiliary properties result from NORMALIZATION
	 * @param uri
	 */
	
	public DataPropertyExpression createAuxiliaryDataProperty();
	

	public Set<OClass> getClasses();
	
	public Set<ObjectPropertyExpression> getObjectProperties();
	
	public Set<DataPropertyExpression> getDataProperties();
	
	/**
	 * auxiliary properties result from NORMALIZATION
	 */
	
	public Set<ObjectPropertyExpression> getAuxiliaryObjectProperties();
	/**
	 * auxiliary properties result from NORMALIZATION
	 */
	
	public Set<DataPropertyExpression> getAuxiliaryDataProperties();
	

	

	public void merge(OntologyVocabulary v);
	
	public boolean isEmpty();
}
