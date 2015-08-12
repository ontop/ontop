package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.OBDAOntologyVocabulary;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class OBDAOntologyVocabularyImpl implements OBDAOntologyVocabulary {

	private final Set<OClass> declaredClasses = new LinkedHashSet<OClass>();

	private final Set<ObjectPropertyExpression> declaredObjectProperties = new LinkedHashSet<ObjectPropertyExpression>();

	private final Set<DataPropertyExpression> declaredDataProperties = new LinkedHashSet<DataPropertyExpression>();

	@Override
	public Set<OClass> getClasses() {
		return Collections.unmodifiableSet(declaredClasses);
	}

	@Override
	public Set<ObjectPropertyExpression> getObjectProperties() {
		return Collections.unmodifiableSet(declaredObjectProperties);
	}

	@Override
	public Set<DataPropertyExpression> getDataProperties() {
		return Collections.unmodifiableSet(declaredDataProperties);
	}
	
	@Override
	public boolean declareClass(OClass classname) {
		return declaredClasses.add(classname);
	}

	@Override
	public boolean declareObjectProperty(ObjectPropertyExpression property) {
		return declaredObjectProperties.add(property);
	}

	@Override
	public boolean declareDataProperty(DataPropertyExpression property) {
		return declaredDataProperties.add(property);
	}
	
	@Override
	public void declareAll(OntologyVocabulary vocabulary) {
		for (OClass p : vocabulary.getClasses()) 
			declareClass(p);
		
		for (ObjectPropertyExpression p : vocabulary.getObjectProperties()) 
			declareObjectProperty(p);
		
		for (DataPropertyExpression p : vocabulary.getDataProperties()) 
			declareDataProperty(p);
	}
	
	@Override
	public boolean unDeclareClass(OClass classname) {
		return declaredClasses.remove(classname);
	}

	@Override
	public boolean unDeclareObjectProperty(ObjectPropertyExpression property) {
		return declaredObjectProperties.remove(property);
	}

	@Override
	public boolean unDeclareDataProperty(DataPropertyExpression property) {
		return declaredDataProperties.remove(property);
	}
	
	@Override
	public boolean isDeclaredClass(OClass classname) {
		return declaredClasses.contains(classname);
	}

	@Override
	public boolean isDeclaredObjectProperty(ObjectPropertyExpression property) {
		return declaredObjectProperties.contains(property);
	}

	@Override
	public boolean isDeclaredDataProperty(DataPropertyExpression property) {
		return declaredDataProperties.contains(property);
	}
	
	
}
