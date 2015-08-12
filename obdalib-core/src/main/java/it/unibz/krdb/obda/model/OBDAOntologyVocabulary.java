package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;

import java.util.Collection;

public interface OBDAOntologyVocabulary {

	public Collection<OClass> getClasses();

	public Collection<ObjectPropertyExpression> getObjectProperties();

	public Collection<DataPropertyExpression> getDataProperties();

	
	
	public OClass createClass(String classname);

	public ObjectPropertyExpression createObjectProperty(String property);

	public DataPropertyExpression createDataProperty(String property);

	
	
	public void declareAll(OntologyVocabulary vocabulary);

	
	
	public boolean removeClass(String classname);

	public boolean removeObjectProperty(String property);

	public boolean removeDataProperty(String property);
	
	

	public OClass getClass(String classname);

	public ObjectPropertyExpression getObjectProperty(String property);

	public DataPropertyExpression getDataProperty(String property);
}