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

	
	
	public boolean declareClass(OClass classname);

	public boolean declareObjectProperty(ObjectPropertyExpression property);

	public boolean declareDataProperty(DataPropertyExpression property);

	public void declareAll(OntologyVocabulary vocabulary);

	public boolean unDeclareClass(OClass classname);

	public boolean unDeclareObjectProperty(ObjectPropertyExpression property);

	public boolean unDeclareDataProperty(DataPropertyExpression property);

	public boolean isDeclaredClass(OClass classname);

	public boolean isDeclaredObjectProperty(ObjectPropertyExpression property);

	public  boolean isDeclaredDataProperty(DataPropertyExpression property);
}