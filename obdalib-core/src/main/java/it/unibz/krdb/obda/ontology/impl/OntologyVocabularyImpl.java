package it.unibz.krdb.obda.ontology.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;

public class OntologyVocabularyImpl implements OntologyVocabulary {

	private static final OntologyFactory ofac;
	
	// signature
	
	final Map<String, OClass> concepts = new HashMap<>();

	final Map<String, ObjectPropertyExpression> objectProperties = new HashMap<>();

	final Map<String, DataPropertyExpression> dataProperties = new HashMap<>();

	
	// auxiliary symbols and built-in datatypes 
	
	final static Set<Predicate> builtinDatatypes;

	static { // static block
		ofac = OntologyFactoryImpl.getInstance();
		
		DatatypeFactory dfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();
		
		builtinDatatypes = new HashSet<>();
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.LITERAL)); //  .RDFS_LITERAL);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.STRING)); // .XSD_STRING);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.INTEGER)); //OBDAVocabulary.XSD_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.NEGATIVE_INTEGER)); // XSD_NEGATIVE_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.INT)); // OBDAVocabulary.XSD_INT);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.NON_NEGATIVE_INTEGER)); //OBDAVocabulary.XSD_NON_NEGATIVE_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.UNSIGNED_INT)); // OBDAVocabulary.XSD_UNSIGNED_INT);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.POSITIVE_INTEGER)); //.XSD_POSITIVE_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.NON_POSITIVE_INTEGER)); // OBDAVocabulary.XSD_NON_POSITIVE_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.LONG)); // OBDAVocabulary.XSD_LONG);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.DECIMAL)); // OBDAVocabulary.XSD_DECIMAL);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.DOUBLE)); // OBDAVocabulary.XSD_DOUBLE);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.FLOAT)); // OBDAVocabulary.XSD_FLOAT);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.DATETIME)); // OBDAVocabulary.XSD_DATETIME);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.BOOLEAN)); // OBDAVocabulary.XSD_BOOLEAN
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.DATETIME_STAMP)); // OBDAVocabulary.XSD_DATETIME_STAMP
	}
	
	public OntologyVocabularyImpl() {		
	}

	@Override
	public OClass getClass(String uri) {
		OClass cd0 = concepts.get(uri);
		if (cd0 == null) {
			OClass cd = ofac.createClass(uri);
			if (!cd.isNothing() && !cd.isThing())
				throw new RuntimeException("Class not found: " + uri);
			return cd;
		}
		return cd0;
	}
	

	@Override
	public ObjectPropertyExpression getObjectProperty(String uri) {
		ObjectPropertyExpression rd0 = objectProperties.get(uri);
		if (rd0 == null) {
			ObjectPropertyExpression rd = ofac.createObjectProperty(uri);
			if (!rd.isBottom() && !rd.isTop())
				throw new RuntimeException("Object property not found: " + uri);
			return rd;
		}
		return rd0;
	}
	
	@Override
	public DataPropertyExpression getDataProperty(String uri) {
		DataPropertyExpression rd0 = dataProperties.get(uri);
		if (rd0 == null) {
			DataPropertyExpression rd = ofac.createDataProperty(uri);
			if (!rd.isBottom() && !rd.isTop())
				throw new RuntimeException("Data property not found: " + uri);		
			return rd;
		}
		return rd0;
	}

	@Override
	public Collection<OClass> getClasses() {
		return concepts.values();
	}

	@Override
	public Collection<ObjectPropertyExpression> getObjectProperties() {
		return objectProperties.values();
	}

	@Override
	public Collection<DataPropertyExpression> getDataProperties() {
		return dataProperties.values();
	}

	
	
	
	@Override
	public boolean isEmpty() {
		return concepts.isEmpty() && objectProperties.isEmpty() && dataProperties.isEmpty();
	}
		
	
	
	
	@Override
	public OClass createClass(String uri) {
		OClass cd = ofac.createClass(uri);
		if (!cd.isNothing() && !cd.isThing())
			concepts.put(uri, cd);
		return cd;
	}

	@Override
	public DataPropertyExpression createDataProperty(String uri) {
		DataPropertyExpression rd = ofac.createDataProperty(uri);
		if (!rd.isBottom() && !rd.isTop()) 
			dataProperties.put(uri, rd);
		return rd;
	}

	@Override
	public ObjectPropertyExpression createObjectProperty(String uri) {
		ObjectPropertyExpression rd = ofac.createObjectProperty(uri);
		if (!rd.isBottom() && !rd.isTop()) 
			objectProperties.put(uri, rd);
		return rd;
	}

	@Override
	public void merge(OntologyVocabulary v) {
		OntologyVocabularyImpl vi = (OntologyVocabularyImpl)v;
		
		concepts.putAll(vi.concepts);
		objectProperties.putAll(vi.objectProperties);
		dataProperties.putAll(vi.dataProperties);
	}
	
	@Override
	public boolean removeClass(String classname) {
		return concepts.remove(classname) != null;
	}

	@Override
	public boolean removeObjectProperty(String property) {
		return objectProperties.remove(property) != null;
	}

	@Override
	public boolean removeDataProperty(String property) {
		return dataProperties.remove(property) != null;
	}

	@Override
	public boolean containsClass(String uri) {
		return concepts.containsKey(uri);
	}

	@Override
	public boolean containsObjectProperty(String uri) {
		return objectProperties.containsKey(uri);
	}

	@Override
	public boolean containsDataProperty(String uri) {
		return dataProperties.containsKey(uri);
	}
	
}
