package it.unibz.krdb.obda.ontology;

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
	 * create and declare object property
	 * 
	 * @param uri property name
	 * @return
	 */
	
	public ObjectPropertyExpression createObjectProperty(String uri);

	/**
	 * create and declare data property
	 * 
	 * @param uri property name
	 * @return
	 */
	
	public DataPropertyExpression createDataProperty(String uri);
	
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
