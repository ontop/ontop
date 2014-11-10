package it.unibz.krdb.obda.ontology.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
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

public class OntologyVocabularyImpl implements OntologyVocabulary {

	private static OntologyFactory ofac;
	
	// signature
	
	private final Set<OClass> concepts = new HashSet<OClass>();

	private final Set<ObjectPropertyExpression> objectProperties = new HashSet<ObjectPropertyExpression>();

	private final Set<ObjectPropertyExpression> auxObjectProperties = new HashSet<ObjectPropertyExpression>();

	private final Set<DataPropertyExpression> dataProperties = new HashSet<DataPropertyExpression>();

	private final Set<DataPropertyExpression> auxDataProperties = new HashSet<DataPropertyExpression>();
	
	// auxiliary symbols and built-in datatypes 
	
	private final static Set<Predicate> builtinDatatypes;

	static { // static block
		ofac = OntologyFactoryImpl.getInstance();
		
		OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
		
		builtinDatatypes = new HashSet<Predicate>();
		builtinDatatypes.add(dfac.getDataTypePredicateLiteral()); //  .RDFS_LITERAL);
		builtinDatatypes.add(dfac.getDataTypePredicateString()); // .XSD_STRING);
		builtinDatatypes.add(dfac.getDataTypePredicateInteger()); //OBDAVocabulary.XSD_INTEGER);
		builtinDatatypes.add(dfac.getDataTypePredicateNegativeInteger()); // XSD_NEGATIVE_INTEGER);
		builtinDatatypes.add(dfac.getDataTypePredicateInt()); // OBDAVocabulary.XSD_INT);
		builtinDatatypes.add(dfac.getDataTypePredicateNonNegativeInteger()); //OBDAVocabulary.XSD_NON_NEGATIVE_INTEGER);
		builtinDatatypes.add(dfac.getDataTypePredicateUnsignedInt()); // OBDAVocabulary.XSD_UNSIGNED_INT);
		builtinDatatypes.add(dfac.getDataTypePredicatePositiveInteger()); //.XSD_POSITIVE_INTEGER);
		builtinDatatypes.add(dfac.getDataTypePredicateNonPositiveInteger()); // OBDAVocabulary.XSD_NON_POSITIVE_INTEGER);
		builtinDatatypes.add(dfac.getDataTypePredicateLong()); // OBDAVocabulary.XSD_LONG);
		builtinDatatypes.add(dfac.getDataTypePredicateDecimal()); // OBDAVocabulary.XSD_DECIMAL);
		builtinDatatypes.add(dfac.getDataTypePredicateDouble()); // OBDAVocabulary.XSD_DOUBLE);
		builtinDatatypes.add(dfac.getDataTypePredicateFloat()); // OBDAVocabulary.XSD_FLOAT);
		builtinDatatypes.add(dfac.getDataTypePredicateDateTime()); // OBDAVocabulary.XSD_DATETIME);
		builtinDatatypes.add(dfac.getDataTypePredicateBoolean()); // OBDAVocabulary.XSD_BOOLEAN
	}
	
	
	@Override
	public OClass createClass(String uri) {
		OClass cd = ofac.createClass(uri);
		if (!cd.isNothing() && !cd.isThing())
			concepts.add(cd);
		return cd;
	}

	@Override
	public OClass getClass(String uri) {
		OClass cd = ofac.createClass(uri);
		if (!cd.isNothing() && !cd.isThing() && !concepts.contains(cd))
			throw new RuntimeException("Class not found: " + uri);
		return cd;
	}
	
	@Override
	public ObjectPropertyExpression createObjectProperty(String uri) {
		ObjectPropertyExpression rd = ofac.createObjectProperty(uri);
		if (!rd.isBottom() && !rd.isTop())
			objectProperties.add(rd);
		return rd;
	}

	@Override
	public ObjectPropertyExpression getObjectProperty(String uri) {
		ObjectPropertyExpression rd = ofac.createObjectProperty(uri);
		if (!rd.isBottom() && !rd.isTop() && !objectProperties.contains(rd))
			throw new RuntimeException("Object property not found: " + uri);
		return rd;
	}
	
	@Override
	public DataPropertyExpression createDataProperty(String uri) {
		DataPropertyExpression rd = ofac.createDataProperty(uri);
		if (!rd.isBottom() && !rd.isTop())
			dataProperties.add(rd);
		return rd;
	}

	@Override
	public DataPropertyExpression getDataProperty(String uri) {
		DataPropertyExpression rd = ofac.createDataProperty(uri);
		if (!rd.isBottom() && !rd.isTop() && !dataProperties.contains(rd))
			throw new RuntimeException("Data property not found: " + uri);			
		return rd;
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

	
	private static final String AUXROLEURI = "ER.A-AUXROLE"; 
	private static int auxCounter = 0; // THIS IS SHARED AMONG ALL INSTANCES!
	
	@Override
	public ObjectPropertyExpression createAuxiliaryObjectProperty() {
		ObjectPropertyExpression rd = ofac.createObjectProperty(AUXROLEURI + auxCounter);
		auxCounter++ ;
		auxObjectProperties.add(rd);
		return rd;
	}
	
	@Override
	public DataPropertyExpression createAuxiliaryDataProperty() {
		DataPropertyExpression rd = createDataProperty(AUXROLEURI + auxCounter);
		auxCounter++ ;
		auxDataProperties.add(rd);
		return rd;
	}
	
	@Override
	public Set<ObjectPropertyExpression> getAuxiliaryObjectProperties() {
		return Collections.unmodifiableSet(auxObjectProperties);
	}

	@Override
	public Set<DataPropertyExpression> getAuxiliaryDataProperties() {
		return Collections.unmodifiableSet(auxDataProperties);
	}
	
	// TODO: remove static
	
	public static boolean isAuxiliaryProperty(ObjectPropertyExpression role) {
		return role.getPredicate().getName().toString().startsWith(AUXROLEURI);	
	}
	public static boolean isAuxiliaryProperty(DataPropertyExpression role) {
		return role.getPredicate().getName().toString().startsWith(AUXROLEURI);	
	}

	
	boolean addReferencedEntries(ClassExpression desc) {
		if (desc instanceof OClass) {
			OClass cl = (OClass)desc;
			if (!isBuiltIn(cl)) {
				concepts.add(cl);
				return true;
			}
		}
		else if (desc instanceof ObjectSomeValuesFrom)  {
			ObjectPropertyExpression prop = ((ObjectSomeValuesFrom) desc).getProperty();
			return addReferencedEntries(prop);
		}
		else  {
			assert (desc instanceof DataSomeValuesFrom);
			DataPropertyExpression prop = ((DataSomeValuesFrom) desc).getProperty();
			return addReferencedEntries(prop);
		}
		return false;
	}
	
	boolean addReferencedEntries(DataRangeExpression desc) {
		if (desc instanceof Datatype)  {
			// NO-OP
			// datatypes.add((Datatype) desc);
			return true;
		}
		else  {
			assert (desc instanceof DataPropertyRangeExpression);
			DataPropertyExpression prop = ((DataPropertyRangeExpression) desc).getProperty();
			return addReferencedEntries(prop);			
		}
	}
	
	boolean addReferencedEntries(ObjectPropertyExpression prop) {
		if (prop.isInverse()) {
			if (!isBuiltIn(prop.getInverse())) {
				objectProperties.add(prop.getInverse());
				return true;
			}
		}
		else {
			if (!isBuiltIn(prop)) {
				objectProperties.add(prop);
				return true;
			}			
		}
		return false;
	}
	
	boolean addReferencedEntries(DataPropertyExpression prop) {
		if (!isBuiltIn(prop)) {
			dataProperties.add(prop);
			return true;
		}
		return false;
	}
	
	private boolean isBuiltIn(OClass cl) {
		return cl.isNothing() || cl.isThing();
	}
	
	private boolean isBuiltIn(ObjectPropertyExpression prop) {
		return prop.isBottom() || prop.isTop() || auxObjectProperties.contains(prop);
	}

	private boolean isBuiltIn(DataPropertyExpression prop) {
		return prop.isBottom() || prop.isTop() || auxDataProperties.contains(prop);
	}
	
	void checkSignature(ClassExpression desc) {
		
		if (desc instanceof OClass) {
			if (!concepts.contains(desc) && !isBuiltIn((OClass)desc))
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
			checkSignature(((DataPropertyRangeExpression) desc).getProperty());
		}
	}

	void checkSignature(ObjectPropertyExpression prop) {

		if (prop.isInverse()) {
			if (!objectProperties.contains(prop.getInverse()) && !isBuiltIn(prop.getInverse())) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop.getInverse());
		}
		else {
			if (!objectProperties.contains(prop) && !isBuiltIn(prop)) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);
		}
	}

	void checkSignature(DataPropertyExpression prop) {
		if (!dataProperties.contains(prop) && !isBuiltIn(prop))
			throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);
	}
	
	
	@Override
	public void merge(OntologyVocabulary v) {
		concepts.addAll(v.getClasses());
		objectProperties.addAll(v.getObjectProperties());
		dataProperties.addAll(v.getDataProperties());
		auxObjectProperties.addAll(v.getAuxiliaryObjectProperties());
		auxDataProperties.addAll(v.getAuxiliaryDataProperties());
	}

	@Override
	public boolean isEmpty() {
		return concepts.isEmpty() && objectProperties.isEmpty() && dataProperties.isEmpty();
	}
		
	
}
