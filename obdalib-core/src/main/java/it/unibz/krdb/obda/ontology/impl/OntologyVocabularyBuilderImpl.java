package it.unibz.krdb.obda.ontology.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.OntologyVocabularyBuilder;

public class OntologyVocabularyBuilderImpl implements OntologyVocabularyBuilder {

	private static OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	final Map<String, OClass> concepts = new HashMap<>();

	final Map<String, ObjectPropertyExpression> objectProperties = new HashMap<>();

	final Map<String, DataPropertyExpression> dataProperties = new HashMap<>();
		
	@Override
	public OClass declareClass(String uri) {
		OClass cd = ofac.createClass(uri);
		if (!cd.isNothing() && !cd.isThing())
			concepts.put(uri, cd);
		return cd;
	}

	@Override
	public DataPropertyExpression declareDataProperty(String uri) {
		DataPropertyExpression rd = ofac.createDataProperty(uri);
		if (!rd.isBottom() && !rd.isTop()) 
			dataProperties.put(uri, rd);
		return rd;
	}

	@Override
	public ObjectPropertyExpression declareObjectProperty(String uri) {
		ObjectPropertyExpression rd = ofac.createObjectProperty(uri);
		if (!rd.isBottom() && !rd.isTop()) 
			objectProperties.put(uri, rd);
		return rd;
	}

	@Override
	public void merge(OntologyVocabulary v) {
		concepts.putAll(((OntologyVocabularyImpl)v).concepts);
		objectProperties.putAll(((OntologyVocabularyImpl)v).objectProperties);
		dataProperties.putAll(((OntologyVocabularyImpl)v).dataProperties);
//		auxObjectProperties.addAll(v.getAuxiliaryObjectProperties());
//		auxDataProperties.addAll(v.getAuxiliaryDataProperties());
	}

		
}
