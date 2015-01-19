package it.unibz.krdb.obda.model.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

public class DatatypeFactoryImpl implements DatatypeFactory {

	
	// special case of literals with the specified language 
	private final DataTypePredicateImpl RDFS_LITERAL_LANG = new DataTypePredicateImpl(RDFS.LITERAL.toString(), 
									new COL_TYPE[] { COL_TYPE.LITERAL, COL_TYPE.LITERAL });
	
	private final DataTypePredicateImpl RDFS_LITERAL, XSD_STRING;
	private final DataTypePredicateImpl XSD_INTEGER, XSD_NEGATIVE_INTEGER, XSD_NON_NEGATIVE_INTEGER;
	private final DataTypePredicateImpl XSD_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER;
	private final DataTypePredicateImpl XSD_INT, XSD_UNSIGNED_INT, XSD_LONG;
	private final DataTypePredicateImpl XSD_DECIMAL;
	private final DataTypePredicateImpl XSD_DOUBLE, XSD_FLOAT;
	private final DataTypePredicateImpl XSD_DATETIME, XSD_DATETIME_STAMP;
	private final DataTypePredicateImpl XSD_BOOLEAN;
	private final DataTypePredicateImpl XSD_DATE, XSD_TIME, XSD_YEAR;
	
	private final Map<String, COL_TYPE> mapURItoCOLTYPE = new HashMap<String, COL_TYPE>();
	private final Map<COL_TYPE, URI> mapCOLTYPEtoURI = new HashMap<COL_TYPE, URI>();
	private final Map<COL_TYPE, DataTypePredicateImpl> mapCOLTYPEtoPredicate = new HashMap<COL_TYPE, DataTypePredicateImpl>();
	private final List<Predicate> predicateList = new LinkedList<Predicate>();
	
	DatatypeFactoryImpl() {
		RDFS_LITERAL = registerType(RDFS.LITERAL, COL_TYPE.LITERAL); // 3 "http://www.w3.org/2000/01/rdf-schema#Literal"
		XSD_INTEGER = registerType(XMLSchema.INTEGER, COL_TYPE.INTEGER);  //  4 "http://www.w3.org/2001/XMLSchema#integer";
		XSD_DECIMAL = registerType(XMLSchema.DECIMAL, COL_TYPE.DECIMAL);  // 5 "http://www.w3.org/2001/XMLSchema#decimal"
		XSD_DOUBLE = registerType(XMLSchema.DOUBLE, COL_TYPE.DOUBLE);  // 6 "http://www.w3.org/2001/XMLSchema#double"
		XSD_STRING = registerType(XMLSchema.STRING, COL_TYPE.STRING);  // 7 "http://www.w3.org/2001/XMLSchema#string"
		XSD_DATETIME = registerType(XMLSchema.DATETIME, COL_TYPE.DATETIME); // 8 "http://www.w3.org/2001/XMLSchema#dateTime"
		ValueFactory factory = new ValueFactoryImpl();
		URI datetimestamp = factory.createURI("http://www.w3.org/2001/XMLSchema#dateTimeStamp"); // value datetime stamp is missing in XMLSchema
		XSD_DATETIME_STAMP = registerType(datetimestamp, COL_TYPE.DATETIME_STAMP);
		XSD_BOOLEAN = registerType(XMLSchema.BOOLEAN, COL_TYPE.BOOLEAN);  // 9 "http://www.w3.org/2001/XMLSchema#boolean"
		XSD_DATE = registerType(XMLSchema.DATE, COL_TYPE.DATE);  // 10 "http://www.w3.org/2001/XMLSchema#date";
		XSD_TIME = registerType(XMLSchema.TIME, COL_TYPE.TIME);  // 11 "http://www.w3.org/2001/XMLSchema#time";
		XSD_YEAR = registerType(XMLSchema.GYEAR, COL_TYPE.YEAR);  // 12 "http://www.w3.org/2001/XMLSchema#gYear";
		XSD_LONG = registerType(XMLSchema.LONG, COL_TYPE.LONG);  // 13 "http://www.w3.org/2001/XMLSchema#long"
		XSD_FLOAT = registerType(XMLSchema.FLOAT, COL_TYPE.FLOAT); // 14 "http://www.w3.org/2001/XMLSchema#float"
		XSD_NEGATIVE_INTEGER = registerType(XMLSchema.NEGATIVE_INTEGER, COL_TYPE.NEGATIVE_INTEGER); // 15 "http://www.w3.org/2001/XMLSchema#negativeInteger";
		XSD_NON_NEGATIVE_INTEGER = registerType(XMLSchema.NON_NEGATIVE_INTEGER, COL_TYPE.NON_NEGATIVE_INTEGER); // 16 "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"
		XSD_POSITIVE_INTEGER = registerType(XMLSchema.POSITIVE_INTEGER, COL_TYPE.POSITIVE_INTEGER); // 17 "http://www.w3.org/2001/XMLSchema#positiveInteger"
		XSD_NON_POSITIVE_INTEGER = registerType(XMLSchema.NON_POSITIVE_INTEGER, COL_TYPE.NON_POSITIVE_INTEGER); // 18 "http://www.w3.org/2001/XMLSchema#nonPositiveInteger"
		XSD_INT = registerType(XMLSchema.INT, COL_TYPE.INT);  // 19 "http://www.w3.org/2001/XMLSchema#int"
		XSD_UNSIGNED_INT = registerType(XMLSchema.UNSIGNED_INT, COL_TYPE.UNSIGNED_INT);   // 20 "http://www.w3.org/2001/XMLSchema#unsignedInt"

		
		// special case
		// used in ExpressionEvaluator only(?) use proper method there? 
		mapCOLTYPEtoPredicate.put(COL_TYPE.LITERAL_LANG, RDFS_LITERAL_LANG);


	}
	
	private final DataTypePredicateImpl registerType(org.openrdf.model.URI uri, COL_TYPE type) {
		String sURI = uri.toString();
		mapURItoCOLTYPE.put(sURI, type);  
		mapCOLTYPEtoURI.put(type, uri); 
		DataTypePredicateImpl predicate = new DataTypePredicateImpl(sURI, type);
		mapCOLTYPEtoPredicate.put(type, predicate);
		predicateList.add(predicate);
		return predicate;
	}
	
	@Override
	public COL_TYPE getDataType(String uri) {
		return mapURItoCOLTYPE.get(uri);
	}
	
	@Override
	public COL_TYPE getDataType(URI uri) {
		return mapURItoCOLTYPE.get(uri.stringValue());
	}
	
	@Override
	public URI getDataTypeURI(COL_TYPE type) {
		return mapCOLTYPEtoURI.get(type);
	}
	
	@Override
	public boolean isBoolean(Predicate pred) {
		return pred == XSD_BOOLEAN;
	}
	
	@Override
	public boolean isInteger(Predicate p) {
		return p == XSD_INTEGER || p == XSD_NEGATIVE_INTEGER || p == XSD_INT || p == XSD_NON_NEGATIVE_INTEGER ||
                p == XSD_UNSIGNED_INT || p == XSD_POSITIVE_INTEGER || p == XSD_NON_POSITIVE_INTEGER || p == XSD_LONG;		
	}
	
	@Override
	public boolean isFloat(Predicate p) {
		return p == XSD_DOUBLE || p == XSD_FLOAT || p == XSD_DECIMAL;
	}
	
	@Override 
	public boolean isLiteral(Predicate p) {
		return p == RDFS_LITERAL || p == RDFS_LITERAL_LANG;
	}
	
	@Override 
	public boolean isString(Predicate p) {
		return p == XSD_STRING;
	}

	@Override
	public List<Predicate> getDatatypePredicates() {
		return Collections.unmodifiableList(predicateList);
	}
	
//	public final Predicate[] QUEST_DATATYPE_PREDICATES = new Predicate[] {
//			RDFS_LITERAL, XSD_STRING, XSD_INTEGER, XSD_NEGATIVE_INTEGER,
 //   XSD_NON_NEGATIVE_INTEGER, XSD_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER, XSD_INT,
//    XSD_UNSIGNED_INT, XSD_LONG, XSD_FLOAT, XSD_DECIMAL, XSD_DOUBLE,
//			XSD_DATETIME, XSD_BOOLEAN, XSD_DATE, XSD_TIME, XSD_YEAR };
	
//	public static final Predicate[] QUEST_NUMERICAL_DATATYPES = new Predicate[] {
//			XSD_INTEGER, XSD_NEGATIVE_INTEGER,
//           XSD_NON_NEGATIVE_INTEGER, XSD_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER, XSD_INT,
//            XSD_UNSIGNED_INT, XSD_FLOAT, XSD_DECIMAL, XSD_DOUBLE, XSD_LONG };
	
	
	@Override
	public Predicate getTypePredicate(Predicate.COL_TYPE type) {
		return mapCOLTYPEtoPredicate.get(type);
		
		//case OBJECT:   // different uses
		//	return getUriTemplatePredicate(1);
		//case BNODE:    // different uses			
		//	return getBNodeTemplatePredicate(1);
	}

	
		
}
