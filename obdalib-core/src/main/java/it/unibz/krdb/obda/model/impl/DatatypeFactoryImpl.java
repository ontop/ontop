package it.unibz.krdb.obda.model.impl;

import java.util.HashMap;
import java.util.Map;

import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

public class DatatypeFactoryImpl implements DatatypeFactory {

	
	/* Data type predicate URIs */

	private static final String RDFS_LITERAL_URI = "http://www.w3.org/2000/01/rdf-schema#Literal";

	private static final String XSD_STRING_URI = "http://www.w3.org/2001/XMLSchema#string";

	private static final String XSD_INT_URI = "http://www.w3.org/2001/XMLSchema#int";

	private static final String XSD_POSITIVE_INTEGER_URI = "http://www.w3.org/2001/XMLSchema#positiveInteger";

    private static final String XSD_NEGATIVE_INTEGER_URI = "http://www.w3.org/2001/XMLSchema#negativeInteger";

    private static final String XSD_NON_POSITIVE_INTEGER_URI = "http://www.w3.org/2001/XMLSchema#nonPositiveInteger";

    private static final String XSD_UNSIGNED_INT_URI = "http://www.w3.org/2001/XMLSchema#unsignedInt";

    private static final String XSD_NON_NEGATIVE_INTEGER_URI = "http://www.w3.org/2001/XMLSchema#nonNegativeInteger";

    private static final String XSD_INTEGER_URI = "http://www.w3.org/2001/XMLSchema#integer";

    private static final String XSD_LONG_URI = "http://www.w3.org/2001/XMLSchema#long";

    private static final String XSD_DECIMAL_URI = "http://www.w3.org/2001/XMLSchema#decimal";

	private static final String XSD_FLOAT_URI = "http://www.w3.org/2001/XMLSchema#float";

	private static final String XSD_DOUBLE_URI = "http://www.w3.org/2001/XMLSchema#double";

	private static final String XSD_DATETIME_URI = "http://www.w3.org/2001/XMLSchema#dateTime";

	private static final String XSD_BOOLEAN_URI = "http://www.w3.org/2001/XMLSchema#boolean";
	
	private static final String XSD_DATE_URI = "http://www.w3.org/2001/XMLSchema#date";
	
	private static final String XSD_TIME_URI = "http://www.w3.org/2001/XMLSchema#time";

	private static final String XSD_YEAR_URI = "http://www.w3.org/2001/XMLSchema#gYear";

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
		return p == getDataTypePredicateInteger() || 
				p == getDataTypePredicateNegativeInteger() || 
                p == getDataTypePredicateInt() || 
    			p == getDataTypePredicateNonNegativeInteger() ||
                p == getDataTypePredicateUnsignedInt() || 
                p == getDataTypePredicatePositiveInteger() ||
                p == getDataTypePredicateNonPositiveInteger() || 
                p == getDataTypePredicateLong();		
	}
	
	@Override
	public boolean isFloat(Predicate p) {
		return p == getDataTypePredicateDouble() || 
				p == getDataTypePredicateFloat() || 
				p == getDataTypePredicateDecimal();
	}
	
	@Override 
	public boolean isLiteral(Predicate p) {
		return p == RDFS_LITERAL || p == RDFS_LITERAL_LANG;
	}
	
	@Override 
	public boolean isString(Predicate p) {
		return p == XSD_STRING;
	}
	
	// TODO: make this one private
	public static final Predicate RDFS_LITERAL = new DataTypePredicateImpl(
			RDFS_LITERAL_URI, new COL_TYPE[] { COL_TYPE.LITERAL });

	private static final Predicate RDFS_LITERAL_LANG = new DataTypePredicateImpl(
			RDFS_LITERAL_URI, new COL_TYPE[] { COL_TYPE.LITERAL, COL_TYPE.LITERAL });

	private static final Predicate XSD_STRING = new DataTypePredicateImpl(
			XSD_STRING_URI, COL_TYPE.STRING);

	private static final Predicate XSD_INTEGER = new DataTypePredicateImpl(
			XSD_INTEGER_URI, COL_TYPE.INTEGER);

	private static final Predicate XSD_NEGATIVE_INTEGER = new DataTypePredicateImpl(
    		XSD_NEGATIVE_INTEGER_URI, COL_TYPE.NEGATIVE_INTEGER);

	private static final Predicate XSD_INT = new DataTypePredicateImpl(
    		XSD_INT_URI, COL_TYPE.INT);

	private static final Predicate XSD_NON_NEGATIVE_INTEGER = new DataTypePredicateImpl(
    		XSD_NON_NEGATIVE_INTEGER_URI, COL_TYPE.NON_NEGATIVE_INTEGER);

	private static final Predicate XSD_UNSIGNED_INT = new DataTypePredicateImpl(
    		XSD_UNSIGNED_INT_URI, COL_TYPE.UNSIGNED_INT);

	private static final Predicate XSD_POSITIVE_INTEGER = new DataTypePredicateImpl(
    		XSD_POSITIVE_INTEGER_URI, COL_TYPE.POSITIVE_INTEGER);

	private static final Predicate XSD_NON_POSITIVE_INTEGER = new DataTypePredicateImpl(
    		XSD_NON_POSITIVE_INTEGER_URI, COL_TYPE.NON_POSITIVE_INTEGER);

	private static final Predicate XSD_LONG = new DataTypePredicateImpl(
    		XSD_LONG_URI, COL_TYPE.LONG);

	private static final Predicate XSD_DECIMAL = new DataTypePredicateImpl(
			XSD_DECIMAL_URI, COL_TYPE.DECIMAL);

	private static final Predicate XSD_DOUBLE = new DataTypePredicateImpl(
			XSD_DOUBLE_URI, COL_TYPE.DOUBLE);

	private static final Predicate XSD_FLOAT = new DataTypePredicateImpl(
    		XSD_FLOAT_URI, COL_TYPE.FLOAT);

	private static final Predicate XSD_DATETIME = new DataTypePredicateImpl(
			XSD_DATETIME_URI, COL_TYPE.DATETIME);

	private static final Predicate XSD_BOOLEAN = new DataTypePredicateImpl(
			XSD_BOOLEAN_URI, COL_TYPE.BOOLEAN);

	private static final Predicate XSD_DATE = new DataTypePredicateImpl(
			XSD_DATE_URI, COL_TYPE.DATE);

	private static final Predicate XSD_TIME = new DataTypePredicateImpl(
			XSD_TIME_URI, COL_TYPE.TIME);
	
	private static final Predicate XSD_YEAR = new DataTypePredicateImpl(
			XSD_YEAR_URI, COL_TYPE.YEAR);

	public static final Predicate[] QUEST_DATATYPE_PREDICATES = new Predicate[] {
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
			return getDataTypePredicateString();
		case INTEGER:  // 3
			return getDataTypePredicateInteger();
        case NEGATIVE_INTEGER:  // 4
            return getDataTypePredicateNegativeInteger();
        case INT:  // 5
            return getDataTypePredicateInt();
        case POSITIVE_INTEGER:  // 6
            return getDataTypePredicatePositiveInteger();
        case NON_POSITIVE_INTEGER:  // 7
            return getDataTypePredicateNonPositiveInteger();
        case NON_NEGATIVE_INTEGER: // 8
            return getDataTypePredicateNonNegativeInteger();
        case UNSIGNED_INT:  // 9
            return getDataTypePredicateUnsignedInt();
        case LONG:   // 10
            return getDataTypePredicateLong();
		case DECIMAL: // 11
			return getDataTypePredicateDecimal();
        case FLOAT:  // 12
            return getDataTypePredicateFloat();
		case DOUBLE:  // 13
			return getDataTypePredicateDouble();
		case DATETIME:  // 14
			return getDataTypePredicateDateTime();
		case BOOLEAN:  // 15
			return getDataTypePredicateBoolean();
		case DATE:   // 16
			return getDataTypePredicateDate();
		case TIME:  // 17
			return getDataTypePredicateTime();
		case YEAR: // 18
			return getDataTypePredicateYear();
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

	@Override
	public Predicate getDataTypePredicateString() {
		return XSD_STRING;
	}

	@Override
	public Predicate getDataTypePredicateInteger() {
		return XSD_INTEGER;
	}

    @Override
    public Predicate getDataTypePredicateNonNegativeInteger() {
        return XSD_NON_NEGATIVE_INTEGER;
    }

    @Override
    public Predicate getDataTypePredicateInt() {
        return XSD_INT;
    }

    @Override
    public Predicate getDataTypePredicatePositiveInteger() {
        return XSD_POSITIVE_INTEGER;
    }

    @Override
    public Predicate getDataTypePredicateNegativeInteger() {
        return XSD_NEGATIVE_INTEGER;
    }

    @Override
    public Predicate getDataTypePredicateNonPositiveInteger() {
        return XSD_NON_POSITIVE_INTEGER;
    }

    @Override
    public Predicate getDataTypePredicateUnsignedInt() {
        return XSD_UNSIGNED_INT;
    }

    @Override
    public Predicate getDataTypePredicateLong() {
        return XSD_LONG;
    }

	@Override
	public Predicate getDataTypePredicateDecimal() {
		return XSD_DECIMAL;
	}

	@Override
	public Predicate getDataTypePredicateDouble() {
		return XSD_DOUBLE;
	}

    @Override
    public Predicate getDataTypePredicateFloat() {
        return XSD_FLOAT;
    }

	@Override
	public Predicate getDataTypePredicateDateTime() {
		return XSD_DATETIME;
	}

	@Override
	public Predicate getDataTypePredicateBoolean() {
		return XSD_BOOLEAN;
	}

	@Override
	public Predicate getDataTypePredicateDate() {
		return XSD_DATE;
	}
	
	@Override
	public Predicate getDataTypePredicateYear() {
		return XSD_YEAR;
	}

	@Override
	public Predicate getDataTypePredicateTime() {
		return XSD_TIME;
	}
	
	
}
