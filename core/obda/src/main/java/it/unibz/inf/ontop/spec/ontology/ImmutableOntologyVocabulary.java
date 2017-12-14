package it.unibz.inf.ontop.spec.ontology;

import java.util.Collection;

public interface ImmutableOntologyVocabulary {

	/**
	 * check whether the class has been declared and return the class object
	 * 
	 * @param uri
	 * @return
	 * @throws RuntimeException if the class has not been declared
	 */
	
	OClass getClass(String uri);
	

	/**
	 * check whether the object property has been declared and return the class object
	 * 
	 * @param uri
	 * @return
	 * @throws RuntimeException if the object property has not been declared
	 */
	
	ObjectPropertyExpression getObjectProperty(String uri);
	

	/**
	 * check whether the data property has been declared and return the class object
	 * 
	 * @param uri
	 * @return
	 * @throws RuntimeException if the data property has not been declared
	 */
	
	DataPropertyExpression getDataProperty(String uri);

	/**
	 * check whether the annotation property has been declared and return the class object
	 *
	 * @param uri
	 * @return
	 * @throws RuntimeException if the data property has not been declared
	 */

	AnnotationProperty getAnnotationProperty(String uri);
	
	
	Datatype getDatatype(String uri);
	
	
	/**
	 * check whether the class has been declared 
	 * 
	 * @param uri
	 * @return
	 */

	boolean containsClass(String uri);

	/**
	 * check whether the object property has been declared 
	 * 
	 * @param uri
	 * @return
	 */
	
	boolean containsObjectProperty(String uri);
	
	/**
	 * check whether the data property has been declared 
	 * 
	 * @param uri
	 * @return
	 */
	
	boolean containsDataProperty(String uri);

	/**
	 * check whether the data property has been declared
	 *
	 * @param uri
	 * @return
	 */

	boolean containsAnnotationProperty(String uri);

	/**
	 * return all declared classes
	 * 
	 * @return
	 */	
	
	Collection<OClass> getClasses();
	
	/**
	 * return all declared object properties
	 * 
	 * @return
	 */
	
	Collection<ObjectPropertyExpression> getObjectProperties();
	
	/**
	 * return all declared data properties
	 * 
	 * @return
	 */
	
	Collection<DataPropertyExpression> getDataProperties();

	/**
	 * return all declared annotation properties
	 *
	 * @return
	 */

	Collection<AnnotationProperty> getAnnotationProperties();

}
