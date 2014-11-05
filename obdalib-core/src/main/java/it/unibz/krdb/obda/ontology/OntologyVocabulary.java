package it.unibz.krdb.obda.ontology;

import java.util.Set;

public interface OntologyVocabulary {

	public void declareClass(String uri);

	public void declareObjectProperty(String uri);

	public void declareDataProperty(String uri);
	
	
	
	public Set<ObjectPropertyExpression> getObjectProperties();

	public Set<DataPropertyExpression> getDataProperties();
	
	public Set<OClass> getClasses();
	

	public void merge(OntologyVocabulary v);
	
	public boolean isEmpty();
}
