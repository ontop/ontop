package it.unibz.krdb.obda.ontology;

import java.util.Set;

public interface OntologyVocabulary {

	public OClass declareClass(String uri);

	public ObjectPropertyExpression declareObjectProperty(String uri);

	public DataPropertyExpression declareDataProperty(String uri);
	
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
