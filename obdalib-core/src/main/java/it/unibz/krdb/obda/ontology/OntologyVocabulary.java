package it.unibz.krdb.obda.ontology;

import java.util.Set;

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
	

	public boolean isEmpty();
}
