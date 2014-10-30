package it.unibz.krdb.obda.ontology.impl;

import java.util.HashSet;
import java.util.Set;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.ontology.SomeValuesFrom;

public class OntologyVocabularyImpl implements OntologyVocabulary {

	private static OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	// signature
	
	private final Set<OClass> concepts = new HashSet<OClass>();

	// private final Set<Datatype> datatypes = new HashSet<Datatype>();
	
	private final Set<ObjectPropertyExpression> objectProperties = new HashSet<ObjectPropertyExpression>();

	private final Set<DataPropertyExpression> dataProperties = new HashSet<DataPropertyExpression>();
	
	// auxiliary symbols and built-in datatypes 
	
	private final static Set<Predicate> builtinDatatypes = initializeReserved();

	private static Set<Predicate> initializeReserved() { // static block
		Set<Predicate> datatypes = new HashSet<Predicate>();
		datatypes.add(OBDAVocabulary.RDFS_LITERAL);
		datatypes.add(OBDAVocabulary.XSD_STRING);
		datatypes.add(OBDAVocabulary.XSD_INTEGER);
		datatypes.add(OBDAVocabulary.XSD_NEGATIVE_INTEGER);
		datatypes.add(OBDAVocabulary.XSD_NON_NEGATIVE_INTEGER);
		datatypes.add(OBDAVocabulary.XSD_POSITIVE_INTEGER);
		datatypes.add(OBDAVocabulary.XSD_NON_POSITIVE_INTEGER);
		datatypes.add(OBDAVocabulary.XSD_INT);
		datatypes.add(OBDAVocabulary.XSD_UNSIGNED_INT);
		datatypes.add(OBDAVocabulary.XSD_FLOAT);
		datatypes.add(OBDAVocabulary.XSD_LONG);
		datatypes.add(OBDAVocabulary.XSD_DECIMAL);
		datatypes.add(OBDAVocabulary.XSD_DOUBLE);
		datatypes.add(OBDAVocabulary.XSD_DATETIME);
		datatypes.add(OBDAVocabulary.XSD_BOOLEAN);
		return datatypes;
	}
	
	@Override
	public void declareClass(String uri) {
		OClass cd = ofac.createClass(uri);
		concepts.add(cd);
	}

	@Override
	public void declareObjectProperty(String uri) {
		ObjectPropertyExpression rd = ofac.createObjectProperty(uri);
		objectProperties.add(rd);
	}
	
	@Override
	public void declareDataProperty(String uri) {
		DataPropertyExpression rd = ofac.createDataProperty(uri);
		dataProperties.add(rd);
	}

	void declareClass(OClass cd) {
		concepts.add(cd);
	}

	void declareObjectProperty(ObjectPropertyExpression rd) {
		if (rd.isInverse())
			objectProperties.add(rd.getInverse());
		else
			objectProperties.add(rd);
	}
	
	void declareDataProperty(DataPropertyExpression rd) {
		if (rd.isInverse())
			dataProperties.add(rd.getInverse());
		else
			dataProperties.add(rd);
	}
	
	@Override
	public Set<OClass> getClasses() {
		return concepts;
	}

	@Override
	public Set<ObjectPropertyExpression> getObjectProperties() {
		return objectProperties;
	}

	@Override
	public Set<DataPropertyExpression> getDataProperties() {
		return dataProperties;
	}
	
	public static final String AUXROLEURI = "ER.A-AUXROLE"; // TODO: make private
	
	public static boolean isAuxiliaryProperty(PropertyExpression role) {
		return role.getPredicate().getName().toString().startsWith(AUXROLEURI);	
	}

	
	
	
	void checkSignature(BasicClassDescription desc) {
		
		if (desc instanceof OClass) {
			if (!concepts.contains((OClass) desc))
				throw new IllegalArgumentException("Class predicate is unknown: " + desc);
		}	
		else if (desc instanceof Datatype) {
			Predicate pred = ((Datatype) desc).getPredicate();
			if (!builtinDatatypes.contains(pred)) 
				throw new IllegalArgumentException("Datatype predicate is unknown: " + pred);
		}
		else if (desc instanceof SomeValuesFrom) {
			checkSignature(((SomeValuesFrom) desc).getProperty());
		}
		else if (desc instanceof DataPropertyRangeExpression) {
			checkSignature(((DataPropertyRangeExpression) desc).getProperty());
		}
		else 
			throw new UnsupportedOperationException("Cant understand: " + desc);
	}

	void checkSignature(PropertyExpression prop) {
		// Make sure we never validate against auxiliary roles introduced by
		// the translation of the OWL ontology
		if (isAuxiliaryProperty(prop)) 
			return;

		if (prop.isInverse()) {
			if ((prop instanceof ObjectPropertyExpression) && !objectProperties.contains(prop.getInverse())) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);
			if ((prop instanceof DataPropertyExpression) && !dataProperties.contains(prop.getInverse())) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);
		}
		else {
			if ((prop instanceof ObjectPropertyExpression) && !objectProperties.contains(prop)) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);			
			if ((prop instanceof DataPropertyExpression) && !dataProperties.contains(prop)) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);			
		}
	}

	@Override
	public void merge(OntologyVocabulary v) {
		concepts.addAll(v.getClasses());
		objectProperties.addAll(v.getObjectProperties());
		dataProperties.addAll(v.getDataProperties());
	}

	@Override
	public boolean isEmpty() {
		return concepts.isEmpty() && objectProperties.isEmpty() && dataProperties.isEmpty();
	}
		
	
}
