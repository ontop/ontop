package it.unibz.krdb.obda.ontology.impl;

import java.util.HashSet;
import java.util.Set;

import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.OntologyVocabularyBuilder;

public class OntologyVocabularyBuilderImpl implements OntologyVocabularyBuilder {

	private static OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	final Set<OClass> concepts = new HashSet<OClass>();

	final Set<ObjectPropertyExpression> objectProperties = new HashSet<ObjectPropertyExpression>();

	final Set<DataPropertyExpression> dataProperties = new HashSet<DataPropertyExpression>();
		
	@Override
	public OClass declareClass(String uri) {
		OClass cd = ofac.createClass(uri);
		if (!cd.isNothing() && !cd.isThing())
			concepts.add(cd);
		return cd;
	}

	@Override
	public DataPropertyExpression declareDataProperty(String uri) {
		DataPropertyExpression rd = ofac.createDataProperty(uri);
		if (!rd.isBottom() && !rd.isTop()) 
			dataProperties.add(rd);
		return rd;
	}

	@Override
	public ObjectPropertyExpression declareObjectProperty(String uri) {
		ObjectPropertyExpression rd = ofac.createObjectProperty(uri);
		if (!rd.isBottom() && !rd.isTop()) 
			objectProperties.add(rd);
		return rd;
	}

	@Override
	public void merge(OntologyVocabulary v) {
		concepts.addAll(v.getClasses());
		objectProperties.addAll(v.getObjectProperties());
		dataProperties.addAll(v.getDataProperties());
//		auxObjectProperties.addAll(v.getAuxiliaryObjectProperties());
//		auxDataProperties.addAll(v.getAuxiliaryDataProperties());
	}

		
}
