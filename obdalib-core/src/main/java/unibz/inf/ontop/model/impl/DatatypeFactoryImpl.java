package unibz.inf.ontop.model.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import unibz.inf.ontop.model.DatatypeFactory;
import unibz.inf.ontop.model.Predicate;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import unibz.inf.ontop.model.DatatypePredicate;


public class DatatypeFactoryImpl implements DatatypeFactory {

	
	// special case of literals with the specified language 
	private final DatatypePredicate RDFS_LITERAL_LANG = new DatatypePredicateImpl(RDFS.LITERAL.toString(),
									new Predicate.COL_TYPE[] { Predicate.COL_TYPE.LITERAL, Predicate.COL_TYPE.LITERAL });
	
	private final DatatypePredicate RDFS_LITERAL, XSD_STRING;
	private final DatatypePredicate XSD_INTEGER, XSD_NEGATIVE_INTEGER, XSD_NON_NEGATIVE_INTEGER;
	private final DatatypePredicate XSD_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER;
	private final DatatypePredicate XSD_INT, XSD_UNSIGNED_INT, XSD_LONG;
	private final DatatypePredicate XSD_DECIMAL;
	private final DatatypePredicate XSD_DOUBLE, XSD_FLOAT;
	private final DatatypePredicate XSD_DATETIME, XSD_DATETIME_STAMP;
	private final DatatypePredicate XSD_BOOLEAN;
	private final DatatypePredicate XSD_DATE, XSD_TIME, XSD_YEAR;
	
	private final Map<String, Predicate.COL_TYPE> mapURItoCOLTYPE = new HashMap<>();
	private final Map<Predicate.COL_TYPE, URI> mapCOLTYPEtoURI = new HashMap<>();
	private final Map<Predicate.COL_TYPE, DatatypePredicate> mapCOLTYPEtoPredicate = new HashMap<>();
	private final List<Predicate> predicateList = new LinkedList<>();
	
	DatatypeFactoryImpl() {
		RDFS_LITERAL = registerType(RDFS.LITERAL, Predicate.COL_TYPE.LITERAL); // 3 "http://www.w3.org/2000/01/rdf-schema#Literal"
		XSD_INTEGER = registerType(XMLSchema.INTEGER, Predicate.COL_TYPE.INTEGER);  //  4 "http://www.w3.org/2001/XMLSchema#integer";
		XSD_DECIMAL = registerType(XMLSchema.DECIMAL, Predicate.COL_TYPE.DECIMAL);  // 5 "http://www.w3.org/2001/XMLSchema#decimal"
		XSD_DOUBLE = registerType(XMLSchema.DOUBLE, Predicate.COL_TYPE.DOUBLE);  // 6 "http://www.w3.org/2001/XMLSchema#double"
		XSD_STRING = registerType(XMLSchema.STRING, Predicate.COL_TYPE.STRING);  // 7 "http://www.w3.org/2001/XMLSchema#string"
		XSD_DATETIME = registerType(XMLSchema.DATETIME, Predicate.COL_TYPE.DATETIME); // 8 "http://www.w3.org/2001/XMLSchema#dateTime"
		ValueFactory factory = new ValueFactoryImpl();
		URI datetimestamp = factory.createURI("http://www.w3.org/2001/XMLSchema#dateTimeStamp"); // value datetime stamp is missing in XMLSchema
		XSD_DATETIME_STAMP = registerType(datetimestamp, Predicate.COL_TYPE.DATETIME_STAMP);
		XSD_BOOLEAN = registerType(XMLSchema.BOOLEAN, Predicate.COL_TYPE.BOOLEAN);  // 9 "http://www.w3.org/2001/XMLSchema#boolean"
		XSD_DATE = registerType(XMLSchema.DATE, Predicate.COL_TYPE.DATE);  // 10 "http://www.w3.org/2001/XMLSchema#date";
		XSD_TIME = registerType(XMLSchema.TIME, Predicate.COL_TYPE.TIME);  // 11 "http://www.w3.org/2001/XMLSchema#time";
		XSD_YEAR = registerType(XMLSchema.GYEAR, Predicate.COL_TYPE.YEAR);  // 12 "http://www.w3.org/2001/XMLSchema#gYear";
		XSD_LONG = registerType(XMLSchema.LONG, Predicate.COL_TYPE.LONG);  // 13 "http://www.w3.org/2001/XMLSchema#long"
		XSD_FLOAT = registerType(XMLSchema.FLOAT, Predicate.COL_TYPE.FLOAT); // 14 "http://www.w3.org/2001/XMLSchema#float"
		XSD_NEGATIVE_INTEGER = registerType(XMLSchema.NEGATIVE_INTEGER, Predicate.COL_TYPE.NEGATIVE_INTEGER); // 15 "http://www.w3.org/2001/XMLSchema#negativeInteger";
		XSD_NON_NEGATIVE_INTEGER = registerType(XMLSchema.NON_NEGATIVE_INTEGER, Predicate.COL_TYPE.NON_NEGATIVE_INTEGER); // 16 "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"
		XSD_POSITIVE_INTEGER = registerType(XMLSchema.POSITIVE_INTEGER, Predicate.COL_TYPE.POSITIVE_INTEGER); // 17 "http://www.w3.org/2001/XMLSchema#positiveInteger"
		XSD_NON_POSITIVE_INTEGER = registerType(XMLSchema.NON_POSITIVE_INTEGER, Predicate.COL_TYPE.NON_POSITIVE_INTEGER); // 18 "http://www.w3.org/2001/XMLSchema#nonPositiveInteger"
		XSD_INT = registerType(XMLSchema.INT, Predicate.COL_TYPE.INT);  // 19 "http://www.w3.org/2001/XMLSchema#int"
		XSD_UNSIGNED_INT = registerType(XMLSchema.UNSIGNED_INT, Predicate.COL_TYPE.UNSIGNED_INT);   // 20 "http://www.w3.org/2001/XMLSchema#unsignedInt"
		
		// special case
		// used in ExpressionEvaluator only(?) use proper method there? 
		mapCOLTYPEtoPredicate.put(Predicate.COL_TYPE.LITERAL_LANG, RDFS_LITERAL_LANG);
	}
	
	private DatatypePredicate registerType(org.openrdf.model.URI uri, Predicate.COL_TYPE type) {
		String sURI = uri.toString();
		mapURItoCOLTYPE.put(sURI, type);  
		mapCOLTYPEtoURI.put(type, uri); 
		DatatypePredicate predicate = new DatatypePredicateImpl(sURI, type);
		mapCOLTYPEtoPredicate.put(type, predicate);
		predicateList.add(predicate);
		return predicate;
	}
	
	@Override
	public Predicate.COL_TYPE getDatatype(String uri) {
		return mapURItoCOLTYPE.get(uri);
	}
	
	@Override
	public Predicate.COL_TYPE getDatatype(URI uri) {
		return mapURItoCOLTYPE.get(uri.stringValue());
	}
	
	@Override
	public URI getDatatypeURI(Predicate.COL_TYPE type) {
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
