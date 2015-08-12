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
	

	public Collection<OClass> getClasses();
	
	public Collection<ObjectPropertyExpression> getObjectProperties();
	
	public Collection<DataPropertyExpression> getDataProperties();
	
	

	public boolean isEmpty();
	
	
	
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
	
	
	/**
	 * auxiliary properties result from NORMALIZATION
	 */
	
	public Collection<ObjectPropertyExpression> getAuxiliaryObjectProperties();

	/**
	 * auxiliary properties result from NORMALIZATION
	 */
	
	public Collection<DataPropertyExpression> getAuxiliaryDataProperties();
}
