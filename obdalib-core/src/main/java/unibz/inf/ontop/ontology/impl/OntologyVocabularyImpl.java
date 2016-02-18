package unibz.inf.ontop.ontology.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import unibz.inf.ontop.model.DatatypeFactory;
import unibz.inf.ontop.model.Predicate;
import unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import unibz.inf.ontop.ontology.ClassExpression;
import unibz.inf.ontop.ontology.DataPropertyExpression;
import unibz.inf.ontop.ontology.DataPropertyRangeExpression;
import unibz.inf.ontop.ontology.DataRangeExpression;
import unibz.inf.ontop.ontology.DataSomeValuesFrom;
import unibz.inf.ontop.ontology.Datatype;
import unibz.inf.ontop.ontology.OClass;
import unibz.inf.ontop.ontology.ObjectPropertyExpression;
import unibz.inf.ontop.ontology.ObjectSomeValuesFrom;
import unibz.inf.ontop.ontology.OntologyFactory;
import unibz.inf.ontop.ontology.OntologyVocabulary;

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
		
		DatatypeFactory dfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();
		
		builtinDatatypes = new HashSet<Predicate>();
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.LITERAL)); //  .RDFS_LITERAL);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.STRING)); // .XSD_STRING);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.INTEGER)); //OBDAVocabulary.XSD_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.NEGATIVE_INTEGER)); // XSD_NEGATIVE_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.INT)); // OBDAVocabulary.XSD_INT);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.NON_NEGATIVE_INTEGER)); //OBDAVocabulary.XSD_NON_NEGATIVE_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.UNSIGNED_INT)); // OBDAVocabulary.XSD_UNSIGNED_INT);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.POSITIVE_INTEGER)); //.XSD_POSITIVE_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.NON_POSITIVE_INTEGER)); // OBDAVocabulary.XSD_NON_POSITIVE_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.LONG)); // OBDAVocabulary.XSD_LONG);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.DECIMAL)); // OBDAVocabulary.XSD_DECIMAL);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.DOUBLE)); // OBDAVocabulary.XSD_DOUBLE);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.FLOAT)); // OBDAVocabulary.XSD_FLOAT);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.DATETIME)); // OBDAVocabulary.XSD_DATETIME);
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.BOOLEAN)); // OBDAVocabulary.XSD_BOOLEAN
		builtinDatatypes.add(dfac.getTypePredicate(Predicate.COL_TYPE.DATETIME_STAMP)); // OBDAVocabulary.XSD_DATETIME_STAMP
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
		if (!rd.isBottom() && !rd.isTop()) {
			if (isAuxiliaryProperty(rd))
				auxObjectProperties.add(rd);
			else
				objectProperties.add(rd);
		}
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
		if (!rd.isBottom() && !rd.isTop()) {
			if (isAuxiliaryProperty(rd))
				auxDataProperties.add(rd);
			else
				dataProperties.add(rd);
		}
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
	
	@Deprecated
	public static boolean isAuxiliaryProperty(ObjectPropertyExpression role) {
		return role.getPredicate().getName().toString().startsWith(AUXROLEURI);	
	}
	@Deprecated
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
				ObjectPropertyExpression p = prop.getInverse();
				if (isAuxiliaryProperty(p))
					auxObjectProperties.add(p);
				else
					objectProperties.add(p);
				return true;
			}
		}
		else {
			if (!isBuiltIn(prop)) {
				if (isAuxiliaryProperty(prop))
					auxObjectProperties.add(prop);
				else
					objectProperties.add(prop);
				return true;
			}			
		}
		return false;
	}
	
	boolean addReferencedEntries(DataPropertyExpression prop) {
		if (!isBuiltIn(prop)) {
			if (isAuxiliaryProperty(prop))
				auxDataProperties.add(prop);
			else
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
