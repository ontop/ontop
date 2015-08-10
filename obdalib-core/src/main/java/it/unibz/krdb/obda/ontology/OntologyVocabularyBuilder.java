package it.unibz.krdb.obda.ontology;

public interface OntologyVocabularyBuilder {

	/**
	 * declare class
	 * 
	 * @param uri
	 * @return
	 */
	
	public OClass declareClass(String uri);

	/**
	 * declare data property
	 * 
	 * @param uri property name
	 * @return
	 */
	
	public DataPropertyExpression declareDataProperty(String uri);

	/**
	 * declare object property
	 * 
	 * @param uri property name
	 * @return
	 */
	
	public ObjectPropertyExpression declareObjectProperty(String uri);
	
		

	public void merge(OntologyVocabulary v);
	
}
