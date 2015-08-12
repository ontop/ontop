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

	
	
	public OClass declareClass(String classname);

	public ObjectPropertyExpression declareObjectProperty(String property);

	public DataPropertyExpression declareDataProperty(String property);

	
	
	public void declareAll(OntologyVocabulary vocabulary);

	
	
	public boolean unDeclareClass(OClass classname);

	public boolean unDeclareObjectProperty(ObjectPropertyExpression property);

	public boolean unDeclareDataProperty(DataPropertyExpression property);
	
	

	public OClass getClass(String classname);

	public ObjectPropertyExpression getObjectProperty(String property);

	public DataPropertyExpression getDataProperty(String property);
}