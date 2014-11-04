package it.unibz.krdb.obda.ontology;

import java.util.Set;

public interface OntologyVocabulary {

	public void declareClass(String uri);

	public void declareObjectProperty(String uri);

	/**
	 * auxiliary properties result from NORMALIZATION
	 * @param uri
	 */

	public ObjectPropertyExpression createAuxiliaryObjectProperty();
	
	public void declareDataProperty(String uri);
	
	/**
	 * auxiliary properties result from NORMALIZATION
	 * @param uri
	 */
	
	public DataPropertyExpression createAuxiliaryDataProperty();
	
	
	public Set<ObjectPropertyExpression> getObjectProperties();

	/**
	 * auxiliary properties result from NORMALIZATION
	 */
	
	public Set<ObjectPropertyExpression> getAuxiliaryObjectProperties();
	
	public Set<DataPropertyExpression> getDataProperties();
	
	/**
	 * auxiliary properties result from NORMALIZATION
	 */
	
	public Set<DataPropertyExpression> getAuxiliaryDataProperties();
	
	public Set<OClass> getClasses();
	

	public void merge(OntologyVocabulary v);
	
	public boolean isEmpty();
}
