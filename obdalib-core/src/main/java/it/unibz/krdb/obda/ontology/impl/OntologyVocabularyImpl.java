package it.unibz.krdb.obda.ontology.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.ImmutableOntologyVocabulary;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;

public class OntologyVocabularyImpl implements OntologyVocabulary {

	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	final Map<String, OClass> concepts = new HashMap<>();
	final Map<String, ObjectPropertyExpression> objectProperties = new HashMap<>();
	final Map<String, DataPropertyExpression> dataProperties = new HashMap<>();

	
	private static final String CLASS_NOT_FOUND = "Class not found: ";	
	private static final String OBJECT_PROPERTY_NOT_FOUND = "ObjectProperty not found: ";
	private static final String DATA_PROPERTY_NOT_FOUND = "DataProperty not found: ";
	private static final String DATATYPE_NOT_FOUND = "Datatype not found: ";
	
	public OntologyVocabularyImpl() {		
	}

	@Override
	public OClass getClass(String uri) {
		OClass oc = concepts.get(uri);
		if (oc != null) 
			return oc;
		else if (uri.equals(ClassImpl.owlThingIRI))
			return ClassImpl.owlThing;
		else if (uri.equals(ClassImpl.owlNothingIRI))
			return ClassImpl.owlNothing;
		else
			throw new RuntimeException(CLASS_NOT_FOUND + uri);
	}
	

	@Override
	public ObjectPropertyExpression getObjectProperty(String uri) {
		ObjectPropertyExpression ope = objectProperties.get(uri);
		if (ope != null) 
			return ope;
		else if (uri.equals(ObjectPropertyExpressionImpl.owlBottomObjectPropertyIRI))
			return ObjectPropertyExpressionImpl.owlBottomObjectProperty;
		else if (uri.equals(ObjectPropertyExpressionImpl.owlTopObjectPropertyIRI))
			return ObjectPropertyExpressionImpl.owlTopObjectProperty;
		else
			throw new RuntimeException(OBJECT_PROPERTY_NOT_FOUND + uri);
	}
	
	@Override
	public DataPropertyExpression getDataProperty(String uri) {
		DataPropertyExpression dpe = dataProperties.get(uri);
		if (dpe != null) 
			return dpe;
		else if (uri.equals(DataPropertyExpressionImpl.owlBottomDataPropertyIRI))
			return DataPropertyExpressionImpl.owlBottomDataProperty;
		else if (uri.equals(DataPropertyExpressionImpl.owlTopDataPropertyIRI))
			return DataPropertyExpressionImpl.owlTopDataProperty;
		else
			throw new RuntimeException(DATA_PROPERTY_NOT_FOUND + uri);
	}

	@Override
	public Datatype getDatatype(String uri) {
		Datatype dt = OntologyImpl.OWL2QLDatatypes.get(uri);
		if (dt == null)
			throw new RuntimeException(DATATYPE_NOT_FOUND + uri);
		return dt;
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
		Predicate classp = fac.getClassPredicate(uri);
		OClass cd = new ClassImpl(classp);
		if (!cd.isNothing() && !cd.isThing())
			concepts.put(uri, cd);
		return cd;
	}

	@Override
	public DataPropertyExpression createDataProperty(String uri) {
		Predicate prop = fac.getDataPropertyPredicate(uri);
		DataPropertyExpression rd = new DataPropertyExpressionImpl(prop);
		if (!rd.isBottom() && !rd.isTop()) 
			dataProperties.put(uri, rd);
		return rd;
	}

	@Override
	public ObjectPropertyExpression createObjectProperty(String uri) {
		Predicate prop = fac.getObjectPropertyPredicate(uri);
		ObjectPropertyExpression rd = new ObjectPropertyExpressionImpl(prop);
		if (!rd.isBottom() && !rd.isTop()) 
			objectProperties.put(uri, rd);
		return rd;
	}

	@Override
	public void merge(ImmutableOntologyVocabulary v) {
		if (v instanceof OntologyVocabularyImpl) {
			OntologyVocabularyImpl vi = (OntologyVocabularyImpl)v;
			
			concepts.putAll(vi.concepts);
			objectProperties.putAll(vi.objectProperties);
			dataProperties.putAll(vi.dataProperties);
		}
		else {
			for (OClass oc : v.getClasses())
				if (!oc.isThing() && !oc.isNothing())
					concepts.put(oc.getPredicate().getName(), oc);
			for (ObjectPropertyExpression ope : v.getObjectProperties())
				if (!ope.isTop() && !ope.isBottom())
					objectProperties.put(ope.getPredicate().getName(), ope);
			for (DataPropertyExpression dpe : v.getDataProperties())
				if (!dpe.isTop() && !dpe.isBottom())
					dataProperties.put(dpe.getPredicate().getName(), dpe);
		}
	}
	
	@Override
	public void removeClass(String classname) {
		concepts.remove(classname);
	}

	@Override
	public void removeObjectProperty(String property) {
		objectProperties.remove(property);
	}

	@Override
	public void removeDataProperty(String property) {
		dataProperties.remove(property);
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
