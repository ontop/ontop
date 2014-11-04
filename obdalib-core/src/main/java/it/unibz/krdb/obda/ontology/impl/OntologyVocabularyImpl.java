package it.unibz.krdb.obda.ontology.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
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
import it.unibz.krdb.obda.ontology.PropertyExpression;

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
	
	public static final OClass owlThing = ofac.createClass("http://www.w3.org/2002/07/owl#Thing");
	public static final OClass owlNothing = ofac.createClass("http://www.w3.org/2002/07/owl#Nothing");
	public static final ObjectPropertyExpression owlTopObjectProperty = ofac.createObjectProperty("http://www.w3.org/2002/07/owl#topObjectProperty");
	public static final ObjectPropertyExpression owlBottomObjectProperty = ofac.createObjectProperty("http://www.w3.org/2002/07/owl#bottomObjectProperty");
	public static final DataPropertyExpression owlTopDataProperty = ofac.createDataProperty("http://www.w3.org/2002/07/owl#topDataProperty");
	public static final DataPropertyExpression owlBottomDataProperty = ofac.createDataProperty("http://www.w3.org/2002/07/owl#bottomDataProperty");
	
	
	@Override
	public void declareClass(String uri) {
		OClass cd = ofac.createClass(uri);
		if (!cd.equals(owlThing) && !cd.equals(owlNothing))
			concepts.add(cd);
	}

	@Override
	public void declareObjectProperty(String uri) {
		ObjectPropertyExpression rd = ofac.createObjectProperty(uri);
		if (!rd.equals(owlTopObjectProperty) && !rd.equals(owlBottomObjectProperty))
			objectProperties.add(rd);
	}
	
	@Override
	public void declareDataProperty(String uri) {
		DataPropertyExpression rd = ofac.createDataProperty(uri);
		if (!rd.equals(owlTopDataProperty) && !rd.equals(owlBottomDataProperty))
			dataProperties.add(rd);
	}

	@Override
	public Set<OClass> getClasses() {
		return Collections.unmodifiableSet(concepts);
	}

	@Override
	public Set<ObjectPropertyExpression> getObjectProperties() {
		return Collections.unmodifiableSet(objectProperties);
	}

	@Override
	public Set<DataPropertyExpression> getDataProperties() {
		return Collections.unmodifiableSet(dataProperties);
	}
	
	public static final String AUXROLEURI = "ER.A-AUXROLE"; // TODO: make private
	
	public static boolean isAuxiliaryProperty(PropertyExpression role) {
		return role.getPredicate().getName().toString().startsWith(AUXROLEURI);	
	}

	
	void addReferencedEntries(ClassExpression desc) {
		if (desc instanceof OClass) {
			OClass cl = (OClass)desc;
			if (!cl.equals(owlThing) && !cl.equals(owlNothing))
				concepts.add(cl);
		}
		else if (desc instanceof ObjectSomeValuesFrom)  {
			ObjectPropertyExpression prop = ((ObjectSomeValuesFrom) desc).getProperty();
			addReferencedEntries(prop);
		}
		else  {
			assert (desc instanceof DataSomeValuesFrom);
			DataPropertyExpression prop = ((DataSomeValuesFrom) desc).getProperty();
			addReferencedEntries(prop);
		}
	}
	void addReferencedEntries(DataRangeExpression desc) {
		if (desc instanceof Datatype)  {
			// NO-OP
			// datatypes.add((Datatype) desc);
		}
		else  {
			assert (desc instanceof DataPropertyRangeExpression);
			addReferencedEntries(((DataPropertyRangeExpression) desc).getProperty());			
		}
	}
	
	void addReferencedEntries(ObjectPropertyExpression prop) {
		if (prop.isInverse())
			prop = prop.getInverse();
		if (!prop.equals(owlTopObjectProperty) && !prop.equals(owlBottomObjectProperty))
			objectProperties.add(prop);
	}
	
	void addReferencedEntries(DataPropertyExpression prop) {
		if (!prop.equals(owlTopDataProperty) && !prop.equals(owlBottomDataProperty))
			dataProperties.add(prop);
	}
	
	
	
	void checkSignature(ClassExpression desc) {
		
		if (desc instanceof OClass) {
			if (!concepts.contains(desc) && !desc.equals(owlThing) && !desc.equals(owlNothing))
				throw new IllegalArgumentException("Class predicate is unknown: " + desc);
		}	
		else if (desc instanceof ObjectSomeValuesFrom) {
			checkSignature(((ObjectSomeValuesFrom) desc).getProperty());
		}
		else  {
			assert (desc instanceof DataSomeValuesFrom);
			checkSignature(((DataSomeValuesFrom) desc).getProperty());
		}
	}	
	
	void checkSignature(DataRangeExpression desc) {
		
		if (desc instanceof Datatype) {
			Predicate pred = ((Datatype) desc).getPredicate();
			if (!builtinDatatypes.contains(pred)) 
				throw new IllegalArgumentException("Datatype predicate is unknown: " + pred);
		}
		else {
			assert (desc instanceof DataPropertyRangeExpression);
			checkSignature(((DataPropertyExpressionImpl)((DataPropertyRangeExpression) desc).getProperty()).inverseProperty);
		}
	}

	void checkSignature(ObjectPropertyExpression prop) {

		if (prop.isInverse()) {
			checkSignature(prop.getInverse());
		}
		else {
			// Make sure we never validate against auxiliary roles introduced by
			// the translation of the OWL ontology
			if (isAuxiliaryProperty(prop)) 
				return;
			
			if (!objectProperties.contains(prop) && 
					!prop.equals(owlTopObjectProperty) && !prop.equals(owlBottomObjectProperty)) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);
		}
	}

	void checkSignature(DataPropertyExpression prop) {
		// Make sure we never validate against auxiliary roles introduced by
		// the translation of the OWL ontology
		if (isAuxiliaryProperty(prop)) 
			return;
		
		if (!dataProperties.contains(prop) &&
				!prop.equals(owlTopDataProperty) && !prop.equals(owlBottomDataProperty)) 
			throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);
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
