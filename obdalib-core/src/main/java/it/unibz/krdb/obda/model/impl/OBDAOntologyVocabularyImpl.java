package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.OBDAOntologyVocabulary;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class OBDAOntologyVocabularyImpl implements OBDAOntologyVocabulary {

	private final Map<String, OClass> declaredClasses = new HashMap<>();
	private final Map<String, ObjectPropertyExpression> declaredObjectProperties = new HashMap<>();
	private final Map<String, DataPropertyExpression> declaredDataProperties = new HashMap<>();

	private static OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	@Override
	public Collection<OClass> getClasses() {
		return declaredClasses.values();
	}

	@Override
	public Collection<ObjectPropertyExpression> getObjectProperties() {
		return declaredObjectProperties.values();
	}

	@Override
	public Collection<DataPropertyExpression> getDataProperties() {
		return declaredDataProperties.values();
	}
	
	@Override
	public OClass createClass(String classname) {
		OClass cl = ofac.createClass(classname);
		declaredClasses.put(classname, cl);
		return cl;
	}

	@Override
	public ObjectPropertyExpression createObjectProperty(String property) {
		ObjectPropertyExpression p = ofac.createObjectProperty(property);
		declaredObjectProperties.put(property, p);
		return p;
	}

	@Override
	public DataPropertyExpression createDataProperty(String property) {
		DataPropertyExpression p = ofac.createDataProperty(property);
		declaredDataProperties.put(property, p);
		return p;
	}
	
	@Override
	public void declareAll(OntologyVocabulary vocabulary) {
		for (OClass cl : vocabulary.getClasses()) 
			declaredClasses.put(cl.getPredicate().getName(), cl);
		
		for (ObjectPropertyExpression p : vocabulary.getObjectProperties()) 
			declaredObjectProperties.put(p.getPredicate().getName(), p);
		
		for (DataPropertyExpression p : vocabulary.getDataProperties()) 
			declaredDataProperties.put(p.getPredicate().getName(), p);
	}
	
	
	
	
	@Override
	public boolean removeClass(String classname) {
		return declaredClasses.remove(classname) != null;
	}

	@Override
	public boolean removeObjectProperty(String property) {
		return declaredObjectProperties.remove(property) != null;
	}

	@Override
	public boolean removeDataProperty(String property) {
		return declaredDataProperties.remove(property) != null;
	}
	
	
	
	@Override
	public OClass getClass(String classname) {
		return declaredClasses.get(classname);
	}

	@Override
	public ObjectPropertyExpression getObjectProperty(String property) {
		return declaredObjectProperties.get(property);
	}

	@Override
	public DataPropertyExpression getDataProperty(String property) {
		return declaredDataProperties.get(property);
	}
	
	
}
