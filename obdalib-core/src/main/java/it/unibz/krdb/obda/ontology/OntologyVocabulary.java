package it.unibz.krdb.obda.ontology;


public interface OntologyVocabulary extends ImmutableOntologyVocabulary {

	/**
	 * declare class
	 * 
	 * @param uri
	 * @return class object
	 */
	
	public OClass createClass(String uri);

	/**
	 * declare object property
	 * 
	 * @param uri property name
	 * @return property object
	 */
	
	public ObjectPropertyExpression createObjectProperty(String uri);

	/**
	 * declare data property
	 * 
	 * @param uri property name
	 * @return property object
	 */
	
	public DataPropertyExpression createDataProperty(String uri);


	/**
	 * remove class from the vocabulary
	 * 
	 * @param uri property name
	 */
	
	public void removeClass(String classname);

	/**
	 * remove object property from the vocabulary
	 * 
	 * @param uri property name
	 */
	
	public void removeObjectProperty(String property);

	/**
	 * remove data property from the vocabulary
	 * 
	 * @param uri property name
	 */
	
	public void removeDataProperty(String property);
	
	
	/**
	 * copy all classes and properties from a given vocabulary 
	 * 
	 * @param v vocabulary to be copied from
	 */
	
	public void merge(ImmutableOntologyVocabulary v);
}
