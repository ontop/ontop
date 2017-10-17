package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.vocabulary.OWL;
import it.unibz.inf.ontop.model.vocabulary.OntopInternal;
import it.unibz.inf.ontop.model.term.functionsymbol.DatatypePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.term.impl.DatatypePredicateImpl;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static it.unibz.inf.ontop.model.type.impl.AbstractNumericRDFDatatype.createAbstractNumericTermType;
import static it.unibz.inf.ontop.model.type.impl.ConcreteNumericRDFDatatypeImpl.createConcreteNumericTermType;
import static it.unibz.inf.ontop.model.type.impl.ConcreteNumericRDFDatatypeImpl.createTopConcreteNumericTermType;
import static it.unibz.inf.ontop.model.type.impl.SimpleRDFDatatype.createSimpleRDFDatatype;
import static it.unibz.inf.ontop.model.vocabulary.RDF.LANGSTRING;

@Singleton
public class TypeFactoryImpl implements TypeFactory {

	// special case of literals with the specified language
	private final DatatypePredicate RDF_LANG_STRING;

	private static final TypeFactory INSTANCE = new TypeFactoryImpl();

	private final DatatypePredicate XSD_STRING;
	private final DatatypePredicate XSD_INTEGER, XSD_NEGATIVE_INTEGER, XSD_NON_NEGATIVE_INTEGER;
	private final DatatypePredicate XSD_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER;
	private final DatatypePredicate XSD_INT, XSD_UNSIGNED_INT, XSD_LONG;
	private final DatatypePredicate XSD_DECIMAL;
	private final DatatypePredicate XSD_DOUBLE, XSD_FLOAT;
	private final DatatypePredicate XSD_DATETIME, XSD_DATETIME_STAMP;
	private final DatatypePredicate XSD_BOOLEAN, XSD_BASE64;
	private final DatatypePredicate XSD_DATE, XSD_TIME, XSD_YEAR;

	private final Map<TermType, DatatypePredicate> mapTypetoPredicate = new HashMap<>();
	private final List<Predicate> predicateList = new LinkedList<>();

	// Only builds these TermTypes once.
	private final Map<COL_TYPE, RDFTermType> termTypeColTypeCache = new ConcurrentHashMap<>();
	private final Map<IRI, RDFDatatype> datatypeCache = new ConcurrentHashMap<>();
	private final Map<String, RDFDatatype> langTypeCache = new ConcurrentHashMap<>();

	private final TermType rootTermType;
	private final RDFTermType rootRDFTermType;
	private final UnboundRDFTermType unboundRDFTermType;
	private final ObjectRDFType objectRDFType, iriTermType, blankNodeTermType;
	private final RDFDatatype rdfsLiteralDatatype;
	private final NumericRDFDatatype numericDatatype, owlRealDatatype;
	private final ConcreteNumericRDFDatatype owlRationalDatatype, xsdDecimalDatatype;
	private final ConcreteNumericRDFDatatype xsdDoubleDatatype, xsdFloatDatatype;
	private final ConcreteNumericRDFDatatype xsdIntegerDatatype, xsdLongDatatype, xsdIntDatatype, xsdShortDatatype, xsdByteDatatype;
	private final ConcreteNumericRDFDatatype xsdNonPositiveIntegerDatatype, xsdNegativeIntegerDatatype;
	private final ConcreteNumericRDFDatatype xsdNonNegativeIntegerDatatype, xsdPositiveIntegerDatatype;
	private final ConcreteNumericRDFDatatype xsdUnsignedLongDatatype, xsdUnsignedIntDatatype, xsdUnsignedShortDatatype, xsdUnsignedByteDatatype;
	private final RDFDatatype defaultUnsupportedDatatype, xsdStringDatatype, xsdBooleanDatatype, xsdBase64Datatype;
	private final RDFDatatype xsdTimeDatatype, xsdDateDatatype, xsdDatetimeDatatype, xsdDatetimeStampDatatype, xsdGYearDatatype;
	private final RDF rdfFactory;

	private TypeFactoryImpl() {
		rdfFactory = new SimpleRDF();

		rootTermType = TermTypeImpl.createOriginTermType();
		rootRDFTermType = RDFTermTypeImpl.createRDFTermRoot(rootTermType.getAncestry());

		unboundRDFTermType = UnboundRDFTermTypeImpl.createUnboundRDFTermType(rootRDFTermType.getAncestry());
		termTypeColTypeCache.put(COL_TYPE.NULL, unboundRDFTermType);

		objectRDFType = AbstractObjectRDFType.createAbstractObjectRDFType(rootRDFTermType.getAncestry());
		iriTermType = new IRITermType(objectRDFType.getAncestry());
		termTypeColTypeCache.put(COL_TYPE.OBJECT, iriTermType);
		blankNodeTermType = new BlankNodeTermType(objectRDFType.getAncestry());
		termTypeColTypeCache.put(COL_TYPE.BNODE, blankNodeTermType);

		rdfsLiteralDatatype = createSimpleRDFDatatype(RDFS.LITERAL, rootRDFTermType.getAncestry(), true, COL_TYPE.LITERAL);
		termTypeColTypeCache.put(COL_TYPE.LITERAL, rdfsLiteralDatatype);
		registerDatatype(rdfsLiteralDatatype);

		numericDatatype = createAbstractNumericTermType(OntopInternal.NUMERIC, rdfsLiteralDatatype.getAncestry());
		registerDatatype(numericDatatype);

		xsdDoubleDatatype = createTopConcreteNumericTermType(XSD.DOUBLE, numericDatatype, COL_TYPE.DOUBLE);
		termTypeColTypeCache.put(COL_TYPE.DOUBLE, xsdDoubleDatatype);
		registerDatatype(xsdDoubleDatatype);

		// Type promotion: an xsd:float can be promoted into a xsd:double
		xsdFloatDatatype = createConcreteNumericTermType(XSD.FLOAT, numericDatatype.getAncestry(),
				xsdDoubleDatatype.getPromotionSubstitutionHierarchy(), COL_TYPE.FLOAT, true);
		termTypeColTypeCache.put(COL_TYPE.FLOAT, xsdFloatDatatype);
		registerDatatype(xsdFloatDatatype);

		owlRealDatatype = createAbstractNumericTermType(OWL.REAL, numericDatatype.getAncestry());
		registerDatatype(owlRealDatatype);
		// Type promotion: an owl:rational can be promoted into a xsd:float
		owlRationalDatatype = createConcreteNumericTermType(OWL.RATIONAL, owlRealDatatype.getAncestry(),
				xsdFloatDatatype.getPromotionSubstitutionHierarchy(), true);
		registerDatatype(owlRationalDatatype);
		xsdDecimalDatatype = createConcreteNumericTermType(XSD.DECIMAL, owlRationalDatatype, COL_TYPE.DECIMAL, true);
		termTypeColTypeCache.put(COL_TYPE.DECIMAL, xsdDecimalDatatype);
		registerDatatype(xsdDecimalDatatype);
		xsdIntegerDatatype = createConcreteNumericTermType(XSD.INTEGER, xsdDecimalDatatype, COL_TYPE.INTEGER, true);
		termTypeColTypeCache.put(COL_TYPE.INTEGER, xsdIntegerDatatype);
		registerDatatype(xsdIntegerDatatype);

		xsdNonPositiveIntegerDatatype = createConcreteNumericTermType(XSD.NON_POSITIVE_INTEGER,
				xsdIntegerDatatype, COL_TYPE.NON_POSITIVE_INTEGER, false);
		termTypeColTypeCache.put(COL_TYPE.NON_POSITIVE_INTEGER, xsdNonPositiveIntegerDatatype);
		registerDatatype(xsdNonPositiveIntegerDatatype);
		xsdNegativeIntegerDatatype = createConcreteNumericTermType(XSD.NEGATIVE_INTEGER,
				xsdNonPositiveIntegerDatatype, COL_TYPE.NEGATIVE_INTEGER, false);
		termTypeColTypeCache.put(COL_TYPE.NEGATIVE_INTEGER, xsdNegativeIntegerDatatype);
		registerDatatype(xsdNegativeIntegerDatatype);

		xsdLongDatatype = createConcreteNumericTermType(XSD.LONG, xsdIntegerDatatype, COL_TYPE.LONG, false);
		termTypeColTypeCache.put(COL_TYPE.LONG, xsdLongDatatype);
		registerDatatype(xsdLongDatatype);
		xsdIntDatatype = createConcreteNumericTermType(XSD.INT, xsdLongDatatype, COL_TYPE.INT, false);
		termTypeColTypeCache.put(COL_TYPE.INT, xsdIntDatatype);
		registerDatatype(xsdIntDatatype);
		xsdShortDatatype = createConcreteNumericTermType(XSD.SHORT, xsdIntDatatype, false);
		registerDatatype(xsdShortDatatype);
		xsdByteDatatype = createConcreteNumericTermType(XSD.BYTE, xsdShortDatatype, false);
		registerDatatype(xsdByteDatatype);

		xsdNonNegativeIntegerDatatype = createConcreteNumericTermType(XSD.NON_NEGATIVE_INTEGER,
				xsdIntegerDatatype, COL_TYPE.NON_NEGATIVE_INTEGER, false);
		termTypeColTypeCache.put(COL_TYPE.NON_NEGATIVE_INTEGER, xsdNonNegativeIntegerDatatype);
		registerDatatype(xsdNonNegativeIntegerDatatype);

		xsdUnsignedLongDatatype = createConcreteNumericTermType(XSD.UNSIGNED_LONG, xsdIntegerDatatype, false);
		registerDatatype(xsdUnsignedLongDatatype);
		xsdUnsignedIntDatatype = createConcreteNumericTermType(XSD.UNSIGNED_INT, xsdUnsignedLongDatatype, COL_TYPE.UNSIGNED_INT, false);
		termTypeColTypeCache.put(COL_TYPE.UNSIGNED_INT, xsdUnsignedIntDatatype);
		registerDatatype(xsdUnsignedIntDatatype);

		xsdUnsignedShortDatatype = createConcreteNumericTermType(XSD.UNSIGNED_SHORT, xsdUnsignedIntDatatype, false);
		registerDatatype(xsdUnsignedShortDatatype);
		xsdUnsignedByteDatatype = createConcreteNumericTermType(XSD.UNSIGNED_BYTE, xsdUnsignedShortDatatype, false);
		registerDatatype(xsdUnsignedByteDatatype);

		xsdPositiveIntegerDatatype = createConcreteNumericTermType(XSD.POSITIVE_INTEGER,
				xsdNonNegativeIntegerDatatype, COL_TYPE.POSITIVE_INTEGER, false);
		termTypeColTypeCache.put(COL_TYPE.POSITIVE_INTEGER, xsdPositiveIntegerDatatype);
		registerDatatype(xsdPositiveIntegerDatatype);

		xsdBooleanDatatype = createSimpleRDFDatatype(XSD.BOOLEAN,
				rdfsLiteralDatatype.getAncestry(), COL_TYPE.BOOLEAN);
		termTypeColTypeCache.put(COL_TYPE.BOOLEAN, xsdBooleanDatatype);
		registerDatatype(xsdBooleanDatatype);

		xsdStringDatatype = createSimpleRDFDatatype(XSD.STRING, rdfsLiteralDatatype.getAncestry(),
				COL_TYPE.STRING);
		termTypeColTypeCache.put(COL_TYPE.STRING, xsdStringDatatype);
		registerDatatype(xsdStringDatatype);

		defaultUnsupportedDatatype = UnsupportedRDFDatatype.createUnsupportedDatatype(rdfsLiteralDatatype.getAncestry());
		termTypeColTypeCache.put(COL_TYPE.UNSUPPORTED, defaultUnsupportedDatatype);;

		xsdTimeDatatype = createSimpleRDFDatatype(XSD.TIME, rdfsLiteralDatatype.getAncestry(), COL_TYPE.TIME);
		termTypeColTypeCache.put(COL_TYPE.TIME, xsdTimeDatatype);
		registerDatatype(xsdTimeDatatype);
		xsdDateDatatype = createSimpleRDFDatatype(XSD.DATE, rdfsLiteralDatatype.getAncestry(), COL_TYPE.DATE);
		termTypeColTypeCache.put(COL_TYPE.DATE, xsdDateDatatype);
		registerDatatype(xsdDateDatatype);
		xsdDatetimeDatatype = createSimpleRDFDatatype(XSD.DATETIME, rdfsLiteralDatatype.getAncestry(), COL_TYPE.DATETIME);
		termTypeColTypeCache.put(COL_TYPE.DATETIME, xsdDatetimeDatatype);
		registerDatatype(xsdDatetimeDatatype);
		xsdDatetimeStampDatatype = createSimpleRDFDatatype(XSD.DATETIMESTAMP, xsdDatetimeDatatype.getAncestry(),
				COL_TYPE.DATETIME_STAMP);;
		termTypeColTypeCache.put(COL_TYPE.DATETIME_STAMP, xsdDatetimeStampDatatype);
		registerDatatype(xsdDatetimeStampDatatype);
		xsdGYearDatatype = createSimpleRDFDatatype(XSD.GYEAR, rdfsLiteralDatatype.getAncestry(), COL_TYPE.YEAR);
		termTypeColTypeCache.put(COL_TYPE.YEAR, xsdGYearDatatype);
		registerDatatype(xsdGYearDatatype);

		xsdBase64Datatype = createSimpleRDFDatatype(XSD.BASE64BINARY, rdfsLiteralDatatype.getAncestry(), false);
		registerDatatype(xsdBase64Datatype);

		XSD_INTEGER = registerType(XSD.INTEGER, xsdIntegerDatatype);  //  4 "http://www.w3.org/2001/XMLSchema#integer";
		XSD_DECIMAL = registerType(XSD.DECIMAL, xsdDecimalDatatype);  // 5 "http://www.w3.org/2001/XMLSchema#decimal"
		XSD_DOUBLE = registerType(XSD.DOUBLE, xsdDoubleDatatype);  // 6 "http://www.w3.org/2001/XMLSchema#double"
		XSD_STRING = registerType(XSD.STRING, xsdStringDatatype);  // 7 "http://www.w3.org/2001/XMLSchema#string"
		XSD_DATETIME = registerType(XSD.DATETIME, xsdDatetimeDatatype); // 8 "http://www.w3.org/2001/XMLSchema#dateTime"
		XSD_DATETIME_STAMP = registerType(XSD.DATETIMESTAMP, xsdDatetimeStampDatatype);
		XSD_BOOLEAN = registerType(XSD.BOOLEAN, xsdBooleanDatatype);  // 9 "http://www.w3.org/2001/XMLSchema#boolean"
		XSD_DATE = registerType(XSD.DATE, xsdDateDatatype);  // 10 "http://www.w3.org/2001/XMLSchema#date";
		XSD_TIME = registerType(XSD.TIME, xsdTimeDatatype);  // 11 "http://www.w3.org/2001/XMLSchema#time";
		XSD_YEAR = registerType(XSD.GYEAR, xsdGYearDatatype);  // 12 "http://www.w3.org/2001/XMLSchema#gYear";
		XSD_LONG = registerType(XSD.LONG, xsdLongDatatype);  // 13 "http://www.w3.org/2001/XMLSchema#long"
		XSD_FLOAT = registerType(XSD.FLOAT,xsdFloatDatatype); // 14 "http://www.w3.org/2001/XMLSchema#float"
		XSD_NEGATIVE_INTEGER = registerType(XSD.NEGATIVE_INTEGER, xsdNegativeIntegerDatatype); // 15 "http://www.w3.org/2001/XMLSchema#negativeInteger";
		XSD_NON_NEGATIVE_INTEGER = registerType(XSD.NON_NEGATIVE_INTEGER, xsdNonNegativeIntegerDatatype); // 16 "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"
		XSD_POSITIVE_INTEGER = registerType(XSD.POSITIVE_INTEGER, xsdPositiveIntegerDatatype); // 17 "http://www.w3.org/2001/XMLSchema#positiveInteger"
		XSD_NON_POSITIVE_INTEGER = registerType(XSD.NON_POSITIVE_INTEGER, xsdNonPositiveIntegerDatatype); // 18 "http://www.w3.org/2001/XMLSchema#nonPositiveInteger"
		XSD_INT = registerType(XSD.INT, xsdIntDatatype);  // 19 "http://www.w3.org/2001/XMLSchema#int"
		XSD_UNSIGNED_INT = registerType(XSD.UNSIGNED_INT, xsdUnsignedIntDatatype);   // 20 "http://www.w3.org/2001/XMLSchema#unsignedInt"

		XSD_BASE64 = registerType(XSD.BASE64BINARY, xsdBase64Datatype);

		// Limited registration
		RDF_LANG_STRING = new DatatypePredicateImpl(LANGSTRING, ImmutableList.of(xsdStringDatatype, xsdStringDatatype));
		predicateList.add(RDF_LANG_STRING);
	}

	private void registerDatatype(RDFDatatype datatype) {
		datatypeCache.put(datatype.getIRI(), datatype);
	}


	public static TypeFactory getInstance() {
		return INSTANCE;
	}

	private DatatypePredicate registerType(IRI uri, TermType type) {
		DatatypePredicate predicate = new DatatypePredicateImpl(uri, type);
		mapTypetoPredicate.put(type, predicate);
		predicateList.add(predicate);
		return predicate;
	}

	@Override
	public Optional<RDFDatatype> getOptionalDatatype(String uri) {
		return getOptionalDatatype(rdfFactory.createIRI(uri));
	}

	@Override
	public Optional<RDFDatatype> getOptionalDatatype(IRI iri) {
		return Optional.ofNullable(datatypeCache.get(iri));
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
	public ImmutableList<Predicate> getDatatypePredicates() {
		return ImmutableList.copyOf(predicateList);
	}

	@Override
	public Optional<TermType> getInternalType(DatatypePredicate predicate) {
		return Optional.ofNullable(datatypeCache.get(rdfFactory.createIRI(predicate.getName())));
	}
	
	
	@Override
	public DatatypePredicate getTypePredicate(COL_TYPE type) {
		if (type == COL_TYPE.LANG_STRING)
			return RDF_LANG_STRING;
		return mapTypetoPredicate.get(getTermType(type));
		
		//case OBJECT:   // different uses
		//	return getUriTemplatePredicate(1);
		//case BNODE:    // different uses			
		//	return getBNodeTemplatePredicate(1);
	}

	@Override
	public DatatypePredicate getTypePredicate(TermType type) {
		return mapTypetoPredicate.get(type);

		//case OBJECT:   // different uses
		//	return getUriTemplatePredicate(1);
		//case BNODE:    // different uses
		//	return getBNodeTemplatePredicate(1);
	}

	@Override
	public Optional<DatatypePredicate> getOptionalTypePredicate(TermType type) {
		return Optional.ofNullable(mapTypetoPredicate.get(type));
	}

	/**
	 * TODO: refactor (temporary)
	 */
	@Override
	public RDFTermType getTermType(@Nonnull COL_TYPE type) {
		RDFTermType cachedType = termTypeColTypeCache.get(type);
		if (cachedType == null) {
			throw new RuntimeException("TODO: support " + type);
		}
		return cachedType;
	}

	@Override
	public RDFDatatype getLangTermType(String languageTagString) {
		return langTypeCache
				.computeIfAbsent(languageTagString,
						k -> LangDatatype.createLangDatatype(
								new LanguageTagImpl(languageTagString), xsdStringDatatype.getAncestry()));
	}

	@Override
	public RDFDatatype getDatatype(IRI iri) {
		RDFDatatype datatype = datatypeCache.get	(iri);
		if (datatype == null)
			throw new RuntimeException("TODO: support arbitrary datatypes such as " + iri);
		return datatype;
	}

	@Override
	public ObjectRDFType getIRITermType() {
		return iriTermType;
	}

	@Override
	public ObjectRDFType getBlankNodeType() {
		return blankNodeTermType;
	}

	@Override
	public UnboundRDFTermType getUnboundTermType() {
		return unboundRDFTermType;
	}

	@Override
	public RDFDatatype getUnsupportedDatatype() {
		return defaultUnsupportedDatatype;
	}

	@Override
	public RDFDatatype getAbstractOntopNumericDatatype() {
		return numericDatatype;
	}

	@Override
	public RDFDatatype getAbstractRDFSLiteral() {
		return rdfsLiteralDatatype;
	}

	@Override
	public TermType getAbstractAtomicTermType() {
		return rootTermType;
	}

	@Override
	public RDFTermType getAbstractRDFTermType() {
		return rootRDFTermType;
	}

    @Override
    public ObjectRDFType getAbstractObjectRDFType() {
		return objectRDFType;
    }

}
