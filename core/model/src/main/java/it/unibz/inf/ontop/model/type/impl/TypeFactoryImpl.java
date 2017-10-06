package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.term.functionsymbol.DatatypePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.term.impl.DatatypePredicateImpl;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static it.unibz.inf.ontop.model.type.impl.SimpleRDFDatatype.createSimpleRDFDatatype;

@Singleton
public class TypeFactoryImpl implements TypeFactory {
	
	// special case of literals with the specified language
	private final DatatypePredicate RDF_LANG_STRING;


	private static final IRI ONTOP_NUMERIC = SimpleValueFactory.getInstance().createIRI("urn:it:unibz:inf:ontop:internal:numeric");
	private static final IRI OWL_REAL =  SimpleValueFactory.getInstance().createIRI(OWL.NAMESPACE, "real");
	private static final IRI OWL_RATIONAL =  SimpleValueFactory.getInstance().createIRI(OWL.NAMESPACE, "rational");

	private static final TypeFactory INSTANCE = new TypeFactoryImpl();

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
	private final Map<COL_TYPE, RDFTermType> termTypeCache = new ConcurrentHashMap<>();
	private final Map<String, RDFDatatype> langTypeCache = new ConcurrentHashMap<>();

	private final TermType rootTermType;
	private final RDFTermType rootRDFTermType;
	private final UnboundRDFTermType unboundRDFTermType;
	private final RDFTermType iriTermType, blankNodeTermType;
	private final RDFDatatype rdfsLiteralDatatype;
	private final RDFDatatype numericDatatype, owlRealDatatype, owlRationalDatatype, xsdDecimalDatatype;
	private final RDFDatatype xsdDoubleDatatype, xsdFloatDatatype;
	private final RDFDatatype xsdIntegerDatatype, xsdLongDatatype, xsdIntDatatype, xsdShortDatatype, xsdByteDatatype;
	private final RDFDatatype xsdNonPositiveIntegerDatatype, xsdNegativeIntegerDatatype;
	private final RDFDatatype xsdNonNegativeIntegerDatatype, xsdPositiveIntegerDatatype;
	private final RDFDatatype xsdUnsignedLongDatatype, xsdUnsignedIntDatatype, xsdUnsignedShortDatatype, xsdUnsignedByteDatatype;
	private final RDFDatatype defaultUnsupportedDatatype, xsdStringDatatype, xsdBooleanDatatype;
	private final RDFDatatype xsdTimeDatatype, xsdDateDatatype, xsdDatetimeDatatype, xsdDatetimeStampDatatype, xsdGYearDatatype;


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

		rootTermType = TermTypeImpl.createOriginTermType();
		rootRDFTermType = RDFTermTypeImpl.createRDFTermRoot(rootTermType.getAncestry());

		unboundRDFTermType = UnboundRDFTermTypeImpl.createUnboundRDFTermType(rootRDFTermType.getAncestry());
		termTypeCache.put(COL_TYPE.NULL, unboundRDFTermType);


		// TODO: create an intermediate term type (for all IRI/B-nodes)
		iriTermType = new IRITermType(rootRDFTermType.getAncestry());
		termTypeCache.put(COL_TYPE.OBJECT, iriTermType);
		blankNodeTermType = new BlankNodeTermType(rootRDFTermType.getAncestry());
		termTypeCache.put(COL_TYPE.BNODE, blankNodeTermType);

		rdfsLiteralDatatype = createSimpleRDFDatatype(RDFS.LITERAL, rootRDFTermType.getAncestry(), true, COL_TYPE.LITERAL);
		termTypeCache.put(COL_TYPE.LITERAL, rdfsLiteralDatatype);

		numericDatatype = NumericRDFDatatype.createNumericTermType(ONTOP_NUMERIC, rdfsLiteralDatatype.getAncestry(), true);

		xsdDoubleDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.DOUBLE, numericDatatype.getAncestry(), COL_TYPE.DOUBLE);
		termTypeCache.put(COL_TYPE.DOUBLE, xsdDoubleDatatype);
		xsdFloatDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.FLOAT, numericDatatype.getAncestry(), COL_TYPE.FLOAT);
		termTypeCache.put(COL_TYPE.FLOAT, xsdFloatDatatype);

		owlRealDatatype = NumericRDFDatatype.createNumericTermType(OWL_REAL, numericDatatype.getAncestry(), true);
		owlRationalDatatype = NumericRDFDatatype.createNumericTermType(OWL_RATIONAL, owlRealDatatype.getAncestry(), false);
		xsdDecimalDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.DECIMAL, owlRationalDatatype.getAncestry(), COL_TYPE.DECIMAL);
		termTypeCache.put(COL_TYPE.DECIMAL, xsdDecimalDatatype);
		xsdIntegerDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.INTEGER, xsdDecimalDatatype.getAncestry(), COL_TYPE.INTEGER);
		termTypeCache.put(COL_TYPE.INTEGER, xsdIntegerDatatype);

		xsdNonPositiveIntegerDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.NON_POSITIVE_INTEGER,
				xsdIntegerDatatype.getAncestry(), COL_TYPE.NON_POSITIVE_INTEGER);
		termTypeCache.put(COL_TYPE.NON_POSITIVE_INTEGER, xsdNonPositiveIntegerDatatype);
		xsdNegativeIntegerDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.NEGATIVE_INTEGER,
				xsdNonPositiveIntegerDatatype.getAncestry(), COL_TYPE.NEGATIVE_INTEGER);
		termTypeCache.put(COL_TYPE.NEGATIVE_INTEGER, xsdNegativeIntegerDatatype);

		xsdLongDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.LONG, xsdIntegerDatatype.getAncestry(), COL_TYPE.LONG);
		termTypeCache.put(COL_TYPE.LONG, xsdLongDatatype);
		xsdIntDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.INT, xsdLongDatatype.getAncestry(), COL_TYPE.INT);
		termTypeCache.put(COL_TYPE.INT, xsdIntDatatype);
		xsdShortDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.SHORT, xsdIntDatatype.getAncestry(), false);
		xsdByteDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.BYTE, xsdShortDatatype.getAncestry(), false);

		xsdNonNegativeIntegerDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.NON_NEGATIVE_INTEGER,
				xsdIntegerDatatype.getAncestry(), COL_TYPE.NON_NEGATIVE_INTEGER);
		termTypeCache.put(COL_TYPE.NON_NEGATIVE_INTEGER, xsdNonNegativeIntegerDatatype);

		xsdUnsignedLongDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.UNSIGNED_LONG,
				xsdIntegerDatatype.getAncestry(), false);
		xsdUnsignedIntDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.UNSIGNED_INT,
				xsdUnsignedLongDatatype.getAncestry(), COL_TYPE.UNSIGNED_INT);
		termTypeCache.put(COL_TYPE.UNSIGNED_INT, xsdUnsignedIntDatatype);
		xsdUnsignedShortDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.UNSIGNED_SHORT,
				xsdUnsignedIntDatatype.getAncestry(), false);
		xsdUnsignedByteDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.UNSIGNED_BYTE,
				xsdUnsignedShortDatatype.getAncestry(), false);

		xsdPositiveIntegerDatatype = NumericRDFDatatype.createNumericTermType(XMLSchema.POSITIVE_INTEGER,
				xsdNonNegativeIntegerDatatype.getAncestry(), COL_TYPE.POSITIVE_INTEGER);
		termTypeCache.put(COL_TYPE.POSITIVE_INTEGER, xsdPositiveIntegerDatatype);

		xsdBooleanDatatype = createSimpleRDFDatatype(XMLSchema.BOOLEAN,
				rdfsLiteralDatatype.getAncestry(), COL_TYPE.BOOLEAN);
		termTypeCache.put(COL_TYPE.BOOLEAN, xsdBooleanDatatype);

		xsdStringDatatype = createSimpleRDFDatatype(XMLSchema.STRING, rdfsLiteralDatatype.getAncestry(),
				COL_TYPE.STRING);
		termTypeCache.put(COL_TYPE.STRING, xsdStringDatatype);

		defaultUnsupportedDatatype = UnsupportedRDFDatatype.createUnsupportedDatatype(rdfsLiteralDatatype.getAncestry());
		termTypeCache.put(COL_TYPE.UNSUPPORTED, defaultUnsupportedDatatype);

		xsdTimeDatatype = createSimpleRDFDatatype(XMLSchema.TIME, rdfsLiteralDatatype.getAncestry(), COL_TYPE.TIME);
		termTypeCache.put(COL_TYPE.TIME, xsdTimeDatatype);
		xsdDateDatatype = createSimpleRDFDatatype(XMLSchema.DATE, rdfsLiteralDatatype.getAncestry(), COL_TYPE.DATE);
		termTypeCache.put(COL_TYPE.DATE, xsdDateDatatype);
		xsdDatetimeDatatype = createSimpleRDFDatatype(XMLSchema.DATETIME, rdfsLiteralDatatype.getAncestry(), COL_TYPE.DATETIME);
		termTypeCache.put(COL_TYPE.DATETIME, xsdDatetimeDatatype);
		xsdDatetimeStampDatatype = createSimpleRDFDatatype(datetimestamp, xsdDatetimeDatatype.getAncestry(),
				COL_TYPE.DATETIME_STAMP);
		termTypeCache.put(COL_TYPE.DATETIME_STAMP, xsdDatetimeStampDatatype);
		xsdGYearDatatype = createSimpleRDFDatatype(XMLSchema.GYEAR, rdfsLiteralDatatype.getAncestry(), COL_TYPE.YEAR);
		termTypeCache.put(COL_TYPE.YEAR, xsdGYearDatatype);
	}


	public static TypeFactory getInstance() {
		return INSTANCE;
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
	public Optional<TermType> getInternalType(DatatypePredicate predicate) {
		// TODO: refactor (don't use col_type anymore)
		return Optional.ofNullable(mapURItoCOLTYPE.get(predicate.getName()))
				.filter(c -> c != COL_TYPE.LANG_STRING)
				.map(this::getTermType);
	}
	
	
	@Override
	public DatatypePredicate getTypePredicate(COL_TYPE type) {
		return mapCOLTYPEtoPredicate.get(type);
		
		//case OBJECT:   // different uses
		//	return getUriTemplatePredicate(1);
		//case BNODE:    // different uses			
		//	return getBNodeTemplatePredicate(1);
	}

	/**
	 * TODO: refactor (temporary)
	 */
	@Override
	public RDFTermType getTermType(COL_TYPE type) {
		RDFTermType cachedType = termTypeCache.get(type);
		if (cachedType == null) {
			throw new RuntimeException("TODO: support " + type);
		}
		return cachedType;
	}

	@Override
	public RDFDatatype getTermType(String languageTagString) {
		return langTypeCache
				.computeIfAbsent(languageTagString,
						k -> LangDatatype.createLangDatatype(
								new LanguageTagImpl(languageTagString), xsdStringDatatype.getAncestry()));
	}

	@Override
	public RDFTermType getIRITermType() {
		return getTermType(COL_TYPE.OBJECT);
	}

}
