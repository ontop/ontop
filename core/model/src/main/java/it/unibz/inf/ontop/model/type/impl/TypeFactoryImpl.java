package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.functionsymbol.DatatypePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.term.impl.DatatypePredicateImpl;
import it.unibz.inf.ontop.model.type.LanguageTag;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TypeFactoryImpl implements TypeFactory {

	private static final TypeFactory INSTANCE = new TypeFactoryImpl();


	public static TypeFactory getInstance() {
		return INSTANCE;
	}

	
	// special case of literals with the specified language
	private final DatatypePredicate RDF_LANG_STRING;
	
	private final DatatypePredicate  XSD_STRING;
//	private final DatatypePredicate RDFS_LITERAL;
	private final DatatypePredicate XSD_INTEGER, XSD_NEGATIVE_INTEGER, XSD_NON_NEGATIVE_INTEGER;
	private final DatatypePredicate XSD_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER;
	private final DatatypePredicate XSD_INT, XSD_UNSIGNED_INT, XSD_LONG;
	private final DatatypePredicate XSD_DECIMAL;
	private final DatatypePredicate XSD_DOUBLE, XSD_FLOAT;
	private final DatatypePredicate XSD_DATETIME, XSD_DATETIME_STAMP;
	private final DatatypePredicate XSD_BOOLEAN;
	private final DatatypePredicate XSD_DATE, XSD_TIME, XSD_YEAR;
	
	private final Map<String, COL_TYPE> mapURItoCOLTYPE = new HashMap<>();
	private final Map<COL_TYPE, IRI> mapCOLTYPEtoURI = new HashMap<>();
	private final Map<COL_TYPE, DatatypePredicate> mapCOLTYPEtoPredicate = new HashMap<>();
	private final List<Predicate> predicateList = new LinkedList<>();

	// Only builds these TermTypes once.
	private final Map<COL_TYPE, TermType> termTypeCache = new ConcurrentHashMap<>();

	private TypeFactoryImpl() {

		XSD_INTEGER = registerType(XMLSchema.INTEGER, COL_TYPE.INTEGER);  //  4 "http://www.w3.org/2001/XMLSchema#integer";
		XSD_DECIMAL = registerType(XMLSchema.DECIMAL, COL_TYPE.DECIMAL);  // 5 "http://www.w3.org/2001/XMLSchema#decimal"
		XSD_DOUBLE = registerType(XMLSchema.DOUBLE, COL_TYPE.DOUBLE);  // 6 "http://www.w3.org/2001/XMLSchema#double"
		XSD_STRING = registerType(XMLSchema.STRING, COL_TYPE.STRING);  // 7 "http://www.w3.org/2001/XMLSchema#string"
		XSD_DATETIME = registerType(XMLSchema.DATETIME, COL_TYPE.DATETIME); // 8 "http://www.w3.org/2001/XMLSchema#dateTime"
		ValueFactory factory = SimpleValueFactory.getInstance();
		IRI datetimestamp = factory.createIRI("http://www.w3.org/2001/XMLSchema#dateTimeStamp"); // value datetime stamp is missing in XMLSchema
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
		RDF_LANG_STRING = new DatatypePredicateImpl(RDF.LANGSTRING.toString(), new COL_TYPE[] { COL_TYPE.STRING, COL_TYPE.STRING });
		registerType(RDF.LANGSTRING, COL_TYPE.LANG_STRING, RDF_LANG_STRING);
//		RDFS_LITERAL = new DatatypePredicateImpl(RDFS.LITERAL.toString(), new COL_TYPE[] { COL_TYPE.LITERAL });
//		registerUnsupportedType(RDFS.LITERAL, COL_TYPE.LITERAL, RDFS_LITERAL);
	}

	private DatatypePredicate registerType(org.eclipse.rdf4j.model.IRI uri, COL_TYPE type) {
		String sURI = uri.toString();
		DatatypePredicate predicate = new DatatypePredicateImpl(sURI, type);
		return registerType(uri, type, predicate);
	}

	private DatatypePredicate registerType(org.eclipse.rdf4j.model.IRI uri, COL_TYPE type,
										   DatatypePredicate predicate) {
		String sURI = uri.toString();
		mapURItoCOLTYPE.put(sURI, type);
		mapCOLTYPEtoURI.put(type, uri);
		mapCOLTYPEtoPredicate.put(type, predicate);
		predicateList.add(predicate);
		return predicate;
	}

	//datatype supported only for ontology and r2rml mapping conversion. Not acceted in obda file
	private DatatypePredicate registerUnsupportedType(org.eclipse.rdf4j.model.IRI uri, COL_TYPE type,
										   DatatypePredicate predicate) {
		String sURI = uri.toString();
		mapURItoCOLTYPE.put(sURI, type);
		return predicate;
	}
	
	@Override
	public Optional<COL_TYPE> getDatatype(String uri) {
		return Optional.ofNullable(mapURItoCOLTYPE.get(uri));
	}
	
	@Override
	public COL_TYPE getDatatype(IRI uri) {
		return mapURItoCOLTYPE.get(uri.stringValue());
	}
	
	@Override
	public IRI getDatatypeURI(COL_TYPE type) {
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
	
//	@Override
//	public boolean isLiteral(Predicate p) {
//		return p == RDFS_LITERAL ;
//	}
	
	@Override 
	public boolean isString(Predicate p) {
		return p == XSD_STRING || p == RDF_LANG_STRING;
	}

	@Override
	public List<Predicate> getDatatypePredicates() {
		return Collections.unmodifiableList(predicateList);
	}

	@Override
	public Optional<COL_TYPE> getInternalType(DatatypePredicate predicate) {
		return Optional.ofNullable(mapURItoCOLTYPE.get(predicate.getName()));
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
	public DatatypePredicate getTypePredicate(Predicate.COL_TYPE type) {
		return mapCOLTYPEtoPredicate.get(type);
		
		//case OBJECT:   // different uses
		//	return getUriTemplatePredicate(1);
		//case BNODE:    // different uses			
		//	return getBNodeTemplatePredicate(1);
	}

	private LanguageTag getLanguageTag(String languageTagString) {
		return new LanguageTagImpl(languageTagString);
	}

	@Override
	public TermType getTermType(COL_TYPE type) {
		TermType cachedType = termTypeCache.get(type);
		if (cachedType != null) {
			return cachedType;
		}
		else {
			TermType termType = new TermTypeImpl(type);
			termTypeCache.put(type, termType);
			return termType;
		}
	}

	@Override
	public TermType getTermType(String languageTagString) {
		return new TermTypeImpl(getLanguageTag(languageTagString));
	}

	@Override
	public TermType getTermType(Term languageTagTerm) {
		return languageTagTerm instanceof Constant
				? getTermType(((Constant) languageTagTerm).getValue())
				: new TermTypeImpl(languageTagTerm);
	}

}
