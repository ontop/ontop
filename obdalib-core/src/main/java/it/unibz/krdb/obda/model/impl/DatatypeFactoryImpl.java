package it.unibz.krdb.obda.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

public class DatatypeFactoryImpl implements DatatypeFactory {

	
	/* Data type predicate URIs */

	private final String RDFS_LITERAL_URI = RDFS.LITERAL.toString(); // "http://www.w3.org/2000/01/rdf-schema#Literal";
	private final String XSD_STRING_URI = XMLSchema.STRING.toString(); // "http://www.w3.org/2001/XMLSchema#string";
	private final String XSD_INT_URI = XMLSchema.INT.toString(); // "http://www.w3.org/2001/XMLSchema#int";
	private final String XSD_POSITIVE_INTEGER_URI = XMLSchema.POSITIVE_INTEGER.toString(); // "http://www.w3.org/2001/XMLSchema#positiveInteger";
    private final String XSD_NEGATIVE_INTEGER_URI = XMLSchema.NEGATIVE_INTEGER.toString(); // "http://www.w3.org/2001/XMLSchema#negativeInteger";
    private final String XSD_NON_POSITIVE_INTEGER_URI = XMLSchema.NON_POSITIVE_INTEGER.toString();  //"http://www.w3.org/2001/XMLSchema#nonPositiveInteger";
    private final String XSD_UNSIGNED_INT_URI = XMLSchema.UNSIGNED_INT.toString(); //"http://www.w3.org/2001/XMLSchema#unsignedInt";
    private final String XSD_NON_NEGATIVE_INTEGER_URI =  XMLSchema.NON_NEGATIVE_INTEGER.toString(); // "http://www.w3.org/2001/XMLSchema#nonNegativeInteger";
    private final String XSD_INTEGER_URI = XMLSchema.INTEGER.toString(); // "http://www.w3.org/2001/XMLSchema#integer";
    private final String XSD_LONG_URI = XMLSchema.LONG.toString(); // "http://www.w3.org/2001/XMLSchema#long";
    private final String XSD_DECIMAL_URI = XMLSchema.DECIMAL.toString(); // "http://www.w3.org/2001/XMLSchema#decimal";
	private final String XSD_FLOAT_URI = XMLSchema.FLOAT.toString(); //"http://www.w3.org/2001/XMLSchema#float";
	private final String XSD_DOUBLE_URI = XMLSchema.DOUBLE.toString(); //"http://www.w3.org/2001/XMLSchema#double";
	private final String XSD_DATETIME_URI = XMLSchema.DATETIME.toString();  //"http://www.w3.org/2001/XMLSchema#dateTime";
	private final String XSD_BOOLEAN_URI = XMLSchema.BOOLEAN.toString(); // "http://www.w3.org/2001/XMLSchema#boolean";
	private final String XSD_DATE_URI = XMLSchema.DATE.toString(); // "http://www.w3.org/2001/XMLSchema#date";
	private final String XSD_TIME_URI = XMLSchema.TIME.toString(); // "http://www.w3.org/2001/XMLSchema#time";
	private final String XSD_YEAR_URI = XMLSchema.GYEAR.toString(); // "http://www.w3.org/2001/XMLSchema#gYear";

	private final Predicate RDFS_LITERAL = new DataTypePredicateImpl(RDFS_LITERAL_URI, new COL_TYPE[] { COL_TYPE.LITERAL });
	private final Predicate RDFS_LITERAL_LANG = new DataTypePredicateImpl(RDFS_LITERAL_URI, new COL_TYPE[] { COL_TYPE.LITERAL, COL_TYPE.LITERAL });
	private final Predicate XSD_STRING = new DataTypePredicateImpl(XSD_STRING_URI, COL_TYPE.STRING);
	private final Predicate XSD_INTEGER = new DataTypePredicateImpl(XSD_INTEGER_URI, COL_TYPE.INTEGER);
	private final Predicate XSD_NEGATIVE_INTEGER = new DataTypePredicateImpl(XSD_NEGATIVE_INTEGER_URI, COL_TYPE.NEGATIVE_INTEGER);
	private final Predicate XSD_INT = new DataTypePredicateImpl(XSD_INT_URI, COL_TYPE.INT);
	private final Predicate XSD_NON_NEGATIVE_INTEGER = new DataTypePredicateImpl(XSD_NON_NEGATIVE_INTEGER_URI, COL_TYPE.NON_NEGATIVE_INTEGER);
	private final Predicate XSD_UNSIGNED_INT = new DataTypePredicateImpl(XSD_UNSIGNED_INT_URI, COL_TYPE.UNSIGNED_INT);
	private final Predicate XSD_POSITIVE_INTEGER = new DataTypePredicateImpl(XSD_POSITIVE_INTEGER_URI, COL_TYPE.POSITIVE_INTEGER);
	private final Predicate XSD_NON_POSITIVE_INTEGER = new DataTypePredicateImpl(XSD_NON_POSITIVE_INTEGER_URI, COL_TYPE.NON_POSITIVE_INTEGER);
	private final Predicate XSD_LONG = new DataTypePredicateImpl(XSD_LONG_URI, COL_TYPE.LONG);
	private final Predicate XSD_DECIMAL = new DataTypePredicateImpl(XSD_DECIMAL_URI, COL_TYPE.DECIMAL);
	private final Predicate XSD_DOUBLE = new DataTypePredicateImpl(XSD_DOUBLE_URI, COL_TYPE.DOUBLE);
	private final Predicate XSD_FLOAT = new DataTypePredicateImpl(XSD_FLOAT_URI, COL_TYPE.FLOAT);
	private final Predicate XSD_DATETIME = new DataTypePredicateImpl(XSD_DATETIME_URI, COL_TYPE.DATETIME);
	private final Predicate XSD_BOOLEAN = new DataTypePredicateImpl(XSD_BOOLEAN_URI, COL_TYPE.BOOLEAN);
	private final Predicate XSD_DATE = new DataTypePredicateImpl(XSD_DATE_URI, COL_TYPE.DATE);
	private final Predicate XSD_TIME = new DataTypePredicateImpl(XSD_TIME_URI, COL_TYPE.TIME);
	private final Predicate XSD_YEAR = new DataTypePredicateImpl(XSD_YEAR_URI, COL_TYPE.YEAR);
	
	private final Map<String, COL_TYPE> mapURItoCOLTYPE;
	private final Map<COL_TYPE, String> mapCOLTYPEtoURI;
	
	DatatypeFactoryImpl() {
		mapURItoCOLTYPE = new HashMap<String, COL_TYPE>();
		
		mapURItoCOLTYPE.put(RDFS_LITERAL_URI, COL_TYPE.LITERAL); // 1
		mapURItoCOLTYPE.put(XSD_STRING_URI, COL_TYPE.STRING);  // 2
		mapURItoCOLTYPE.put(XSD_INT_URI, COL_TYPE.INT);  // 3
		mapURItoCOLTYPE.put(XSD_POSITIVE_INTEGER_URI, COL_TYPE.POSITIVE_INTEGER); // 4
		mapURItoCOLTYPE.put(XSD_NEGATIVE_INTEGER_URI, COL_TYPE.NEGATIVE_INTEGER); // 5
		mapURItoCOLTYPE.put(XSD_NON_POSITIVE_INTEGER_URI, COL_TYPE.NON_POSITIVE_INTEGER); // 6
		mapURItoCOLTYPE.put(XSD_UNSIGNED_INT_URI, COL_TYPE.UNSIGNED_INT);   // 7
		mapURItoCOLTYPE.put(XSD_NON_NEGATIVE_INTEGER_URI, COL_TYPE.NON_NEGATIVE_INTEGER); // 8
		mapURItoCOLTYPE.put(XSD_INTEGER_URI, COL_TYPE.INTEGER);  // 9
		mapURItoCOLTYPE.put(XSD_LONG_URI, COL_TYPE.LONG);  // 10
		mapURItoCOLTYPE.put(XSD_DECIMAL_URI, COL_TYPE.DECIMAL);  // 11
		mapURItoCOLTYPE.put(XSD_FLOAT_URI, COL_TYPE.FLOAT); // 12
		mapURItoCOLTYPE.put(XSD_DOUBLE_URI, COL_TYPE.DOUBLE);  // 13
		mapURItoCOLTYPE.put(XSD_DATETIME_URI, COL_TYPE.DATETIME); // 14
		mapURItoCOLTYPE.put(XSD_BOOLEAN_URI, COL_TYPE.BOOLEAN);  // 15
		mapURItoCOLTYPE.put(XSD_DATE_URI, COL_TYPE.DATE);  // 16
		mapURItoCOLTYPE.put(XSD_TIME_URI, COL_TYPE.TIME);  // 17
		mapURItoCOLTYPE.put(XSD_YEAR_URI, COL_TYPE.YEAR);  // 18

		mapCOLTYPEtoURI = new HashMap<COL_TYPE, String>();	

		mapCOLTYPEtoURI.put(COL_TYPE.LITERAL, RDFS_LITERAL_URI); // 1
		mapCOLTYPEtoURI.put(COL_TYPE.STRING, XSD_STRING_URI);  // 2
		mapCOLTYPEtoURI.put(COL_TYPE.INT, XSD_INT_URI);  // 3
		mapCOLTYPEtoURI.put(COL_TYPE.POSITIVE_INTEGER, XSD_POSITIVE_INTEGER_URI); // 4
		mapCOLTYPEtoURI.put(COL_TYPE.NEGATIVE_INTEGER, XSD_NEGATIVE_INTEGER_URI); // 5
		mapCOLTYPEtoURI.put(COL_TYPE.NON_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER_URI); // 6
		mapCOLTYPEtoURI.put(COL_TYPE.UNSIGNED_INT, XSD_UNSIGNED_INT_URI);   // 7
		mapCOLTYPEtoURI.put(COL_TYPE.NON_NEGATIVE_INTEGER, XSD_NON_NEGATIVE_INTEGER_URI); // 8
		mapCOLTYPEtoURI.put(COL_TYPE.INTEGER, XSD_INTEGER_URI);  // 9
		mapCOLTYPEtoURI.put(COL_TYPE.LONG, XSD_LONG_URI);  // 10
		mapCOLTYPEtoURI.put(COL_TYPE.DECIMAL, XSD_DECIMAL_URI);  // 11
		mapCOLTYPEtoURI.put(COL_TYPE.FLOAT, XSD_FLOAT_URI); // 12
		mapCOLTYPEtoURI.put(COL_TYPE.DOUBLE, XSD_DOUBLE_URI);  // 13
		mapCOLTYPEtoURI.put(COL_TYPE.DATETIME, XSD_DATETIME_URI); // 14
		mapCOLTYPEtoURI.put(COL_TYPE.BOOLEAN, XSD_BOOLEAN_URI);  // 15
		mapCOLTYPEtoURI.put(COL_TYPE.DATE, XSD_DATE_URI);  // 16
		mapCOLTYPEtoURI.put(COL_TYPE.TIME, XSD_TIME_URI);  // 17
		mapCOLTYPEtoURI.put(COL_TYPE.YEAR, XSD_YEAR_URI);  // 18		
	}
	
	@Override
	public COL_TYPE getDataType(String uri) {
		return mapURItoCOLTYPE.get(uri);
	}
	
	@Override
	public String getDataTypeURI(COL_TYPE type) {
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
	
	
	public final Predicate[] QUEST_DATATYPE_PREDICATES = new Predicate[] {
			RDFS_LITERAL, XSD_STRING, XSD_INTEGER, XSD_NEGATIVE_INTEGER,
    XSD_NON_NEGATIVE_INTEGER, XSD_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER, XSD_INT,
    XSD_UNSIGNED_INT, XSD_LONG, XSD_FLOAT, XSD_DECIMAL, XSD_DOUBLE,
			XSD_DATETIME, XSD_BOOLEAN, XSD_DATE, XSD_TIME, XSD_YEAR };
	
//	public static final Predicate[] QUEST_NUMERICAL_DATATYPES = new Predicate[] {
//			XSD_INTEGER, XSD_NEGATIVE_INTEGER,
//           XSD_NON_NEGATIVE_INTEGER, XSD_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER, XSD_INT,
//            XSD_UNSIGNED_INT, XSD_FLOAT, XSD_DECIMAL, XSD_DOUBLE, XSD_LONG };
	
	
	@Override
	public Predicate getTypePredicate(Predicate.COL_TYPE type) {
		switch (type) {
		case LITERAL:          // 1
			return getDataTypePredicateLiteral();
		case STRING:   // 2
			return XSD_STRING;
		case INTEGER:  // 3
			return XSD_INTEGER;
        case NEGATIVE_INTEGER:  // 4
            return XSD_NEGATIVE_INTEGER;
        case INT:  // 5
            return XSD_INT;
        case POSITIVE_INTEGER:  // 6
            return XSD_POSITIVE_INTEGER;
        case NON_POSITIVE_INTEGER:  // 7
            return XSD_NON_POSITIVE_INTEGER;
        case NON_NEGATIVE_INTEGER: // 8
            return XSD_NON_NEGATIVE_INTEGER;
        case UNSIGNED_INT:  // 9
            return XSD_UNSIGNED_INT;
        case LONG:   // 10
            return XSD_LONG;
		case DECIMAL: // 11
			return XSD_DECIMAL;
        case FLOAT:  // 12
            return XSD_FLOAT;
		case DOUBLE:  // 13
			return XSD_DOUBLE;
		case DATETIME:  // 14
			return XSD_DATETIME;
		case BOOLEAN:  // 15
			return XSD_BOOLEAN;
		case DATE:   // 16
			return XSD_DATE;
		case TIME:  // 17
			return XSD_TIME;
		case YEAR: // 18
			return XSD_YEAR;
		case LITERAL_LANG: // used in ExpressionEvaluator only(?) use proper method here? 
			return getDataTypePredicateLiteral();
		//case OBJECT:   // different uses
		//	return getUriTemplatePredicate(1);
		//case BNODE:    // different uses			
		//	return getBNodeTemplatePredicate(1);
		default:
			return null;
			//throw new RuntimeException("Cannot get URI for unsupported type: " + type);
		}
	}

	
	
	@Override
	public Predicate getDataTypePredicateLiteral() {
		return RDFS_LITERAL;
	}
	
	@Override
	public Predicate getDataTypePredicateLiteralLang() {
		return RDFS_LITERAL_LANG;
	}

	
}
