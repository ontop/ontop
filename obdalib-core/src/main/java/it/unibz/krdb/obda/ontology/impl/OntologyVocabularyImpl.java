package it.unibz.krdb.obda.ontology.impl;

import java.util.Collection;
import java.util.Collections;
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

	private static OntologyFactory ofac;
	
	// signature
	
	final Map<String, OClass> concepts = new HashMap<>();

	final Map<String, ObjectPropertyExpression> objectProperties = new HashMap<>();

	final Map<String, DataPropertyExpression> dataProperties = new HashMap<>();

	private final Set<ObjectPropertyExpression> auxObjectProperties = new HashSet<>();

	private final Set<DataPropertyExpression> auxDataProperties = new HashSet<>();
	
	
	// auxiliary symbols and built-in datatypes 
	
	private final static Set<Predicate> builtinDatatypes;

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
		DataPropertyExpression rd = ofac.createDataProperty(AUXROLEURI + auxCounter);
		auxCounter++ ;
		auxDataProperties.add(rd);
		return rd;
	}
	
	@Override
	public Collection<ObjectPropertyExpression> getAuxiliaryObjectProperties() {
		return Collections.unmodifiableSet(auxObjectProperties);
	}

	@Override
	public Collection<DataPropertyExpression> getAuxiliaryDataProperties() {
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
				concepts.put(cl.getPredicate().getName(), cl);
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
					objectProperties.put(p.getPredicate().getName(), p);
				return true;
			}
		}
		else {
			if (!isBuiltIn(prop)) {
				if (isAuxiliaryProperty(prop))
					auxObjectProperties.add(prop);
				else
					objectProperties.put(prop.getPredicate().getName(), prop);
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
				dataProperties.put(prop.getPredicate().getName(), prop);
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
			OClass cl = (OClass) desc;
			if (!concepts.containsKey(cl.getPredicate().getName()) && !isBuiltIn(cl))
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
			if (!objectProperties.containsKey(prop.getInverse().getPredicate().getName()) && !isBuiltIn(prop.getInverse())) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop.getInverse());
		}
		else {
			if (!objectProperties.containsKey(prop.getPredicate().getName()) && !isBuiltIn(prop)) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);
		}
	}

	void checkSignature(DataPropertyExpression prop) {
		if (!dataProperties.containsKey(prop.getPredicate().getName()) && !isBuiltIn(prop))
			throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);
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
		auxObjectProperties.addAll(vi.auxObjectProperties);
		auxDataProperties.addAll(vi.auxDataProperties);
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
	
}
