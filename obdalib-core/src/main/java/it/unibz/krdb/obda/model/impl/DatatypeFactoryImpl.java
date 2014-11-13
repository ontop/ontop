package it.unibz.krdb.obda.model.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.utils.JdbcTypeMapper;

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
	private final DataTypePredicateImpl XSD_DATETIME;
	private final DataTypePredicateImpl XSD_BOOLEAN;
	private final DataTypePredicateImpl XSD_DATE, XSD_TIME, XSD_YEAR;
	
	private final Map<String, COL_TYPE> mapURItoCOLTYPE = new HashMap<String, COL_TYPE>();
	private final Map<COL_TYPE, String> mapCOLTYPEtoURI = new HashMap<COL_TYPE, String>();
	private final Map<COL_TYPE, DataTypePredicateImpl> mapCOLTYPEtoPredicate = new HashMap<COL_TYPE, DataTypePredicateImpl>();
	private final List<Predicate> predicateList = new LinkedList<Predicate>();
	
	DatatypeFactoryImpl() {
		RDFS_LITERAL = registerType(RDFS.LITERAL, COL_TYPE.LITERAL); // "http://www.w3.org/2000/01/rdf-schema#Literal"
		XSD_STRING = registerType(XMLSchema.STRING, COL_TYPE.STRING);  // "http://www.w3.org/2001/XMLSchema#string"
		XSD_INT = registerType(XMLSchema.INT, COL_TYPE.INT);  // "http://www.w3.org/2001/XMLSchema#int"
		XSD_POSITIVE_INTEGER = registerType(XMLSchema.POSITIVE_INTEGER, COL_TYPE.POSITIVE_INTEGER); // "http://www.w3.org/2001/XMLSchema#positiveInteger"
		XSD_NEGATIVE_INTEGER = registerType(XMLSchema.NEGATIVE_INTEGER, COL_TYPE.NEGATIVE_INTEGER); // "http://www.w3.org/2001/XMLSchema#negativeInteger";
		XSD_NON_POSITIVE_INTEGER = registerType(XMLSchema.NON_POSITIVE_INTEGER, COL_TYPE.NON_POSITIVE_INTEGER); // "http://www.w3.org/2001/XMLSchema#nonPositiveInteger"
		XSD_UNSIGNED_INT = registerType(XMLSchema.UNSIGNED_INT, COL_TYPE.UNSIGNED_INT);   // "http://www.w3.org/2001/XMLSchema#unsignedInt"
		XSD_NON_NEGATIVE_INTEGER = registerType(XMLSchema.NON_NEGATIVE_INTEGER, COL_TYPE.NON_NEGATIVE_INTEGER); //  "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"
		XSD_INTEGER = registerType(XMLSchema.INTEGER, COL_TYPE.INTEGER);  // "http://www.w3.org/2001/XMLSchema#integer";
		XSD_LONG = registerType(XMLSchema.LONG, COL_TYPE.LONG);  // "http://www.w3.org/2001/XMLSchema#long"
		XSD_DECIMAL = registerType(XMLSchema.DECIMAL, COL_TYPE.DECIMAL);  // "http://www.w3.org/2001/XMLSchema#decimal"
		XSD_FLOAT = registerType(XMLSchema.FLOAT, COL_TYPE.FLOAT); // "http://www.w3.org/2001/XMLSchema#float"
		XSD_DOUBLE = registerType(XMLSchema.DOUBLE, COL_TYPE.DOUBLE);  // "http://www.w3.org/2001/XMLSchema#double"
		XSD_DATETIME = registerType(XMLSchema.DATETIME, COL_TYPE.DATETIME); // "http://www.w3.org/2001/XMLSchema#dateTime"
		XSD_BOOLEAN = registerType(XMLSchema.BOOLEAN, COL_TYPE.BOOLEAN);  //  "http://www.w3.org/2001/XMLSchema#boolean"
		XSD_DATE = registerType(XMLSchema.DATE, COL_TYPE.DATE);  // "http://www.w3.org/2001/XMLSchema#date";
		XSD_TIME = registerType(XMLSchema.TIME, COL_TYPE.TIME);  // "http://www.w3.org/2001/XMLSchema#time";
		XSD_YEAR = registerType(XMLSchema.GYEAR, COL_TYPE.YEAR);  // "http://www.w3.org/2001/XMLSchema#gYear";
		
		// special case
		// used in ExpressionEvaluator only(?) use proper method there? 
		mapCOLTYPEtoPredicate.put(COL_TYPE.LITERAL_LANG, RDFS_LITERAL_LANG);
	}
	
	private final DataTypePredicateImpl registerType(org.openrdf.model.URI uri, COL_TYPE type) {
		String sURI = uri.toString();
		mapURItoCOLTYPE.put(sURI, type);  
		mapCOLTYPEtoURI.put(type, sURI); 
		DataTypePredicateImpl predicate = new DataTypePredicateImpl(sURI, type);
		mapCOLTYPEtoPredicate.put(type, predicate);
		predicateList.add(predicate);
		return predicate;
	}
	
	private final QuestTypeMapper questTypeMapper = new QuestTypeMapper();
	private final JdbcTypeMapper jdbcTypeMapper =  new JdbcTypeMapper(this); 
	
	@Override
	public QuestTypeMapper getQuestTypeMapper() {
		return questTypeMapper;
	}
	
	@Override 
	public JdbcTypeMapper getJdbcTypeMapper() {
		return jdbcTypeMapper;
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
