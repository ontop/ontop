package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.vocabulary.OWL;
import it.unibz.inf.ontop.model.vocabulary.OntopInternal;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static it.unibz.inf.ontop.model.type.impl.AbstractNumericRDFDatatype.createAbstractNumericTermType;
import static it.unibz.inf.ontop.model.type.impl.ConcreteNumericRDFDatatypeImpl.createConcreteNumericTermType;
import static it.unibz.inf.ontop.model.type.impl.ConcreteNumericRDFDatatypeImpl.createTopConcreteNumericTermType;
import static it.unibz.inf.ontop.model.type.impl.SimpleRDFDatatype.createSimpleRDFDatatype;

@Singleton
public class TypeFactoryImpl implements TypeFactory {

	// Only builds these TermTypes once.
	private final Map<IRI, RDFDatatype> datatypeCache = new ConcurrentHashMap<>();
	private final Map<String, RDFDatatype> langTypeCache = new ConcurrentHashMap<>();

	private final TermType rootTermType;
	private final RDFTermType rootRDFTermType;
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

	@Inject
	private TypeFactoryImpl(RDF rdfFactory) {
		this.rdfFactory = rdfFactory;

		rootTermType = TermTypeImpl.createOriginTermType();
		rootRDFTermType = RDFTermTypeImpl.createRDFTermRoot(rootTermType.getAncestry());

		objectRDFType = AbstractObjectRDFType.createAbstractObjectRDFType(rootRDFTermType.getAncestry());
		iriTermType = new IRITermType(objectRDFType.getAncestry());
		blankNodeTermType = new BlankNodeTermType(objectRDFType.getAncestry());

		rdfsLiteralDatatype = createSimpleRDFDatatype(RDFS.LITERAL, rootRDFTermType.getAncestry(), true);
		registerDatatype(rdfsLiteralDatatype);

		numericDatatype = createAbstractNumericTermType(OntopInternal.NUMERIC, rdfsLiteralDatatype.getAncestry());
		registerDatatype(numericDatatype);

		xsdDoubleDatatype = createTopConcreteNumericTermType(XSD.DOUBLE, numericDatatype);
		registerDatatype(xsdDoubleDatatype);

		// Type promotion: an xsd:float can be promoted into a xsd:double
		xsdFloatDatatype = createConcreteNumericTermType(XSD.FLOAT, numericDatatype.getAncestry(),
				xsdDoubleDatatype.getPromotionSubstitutionHierarchy(),true);
		registerDatatype(xsdFloatDatatype);

		owlRealDatatype = createAbstractNumericTermType(OWL.REAL, numericDatatype.getAncestry());
		registerDatatype(owlRealDatatype);
		// Type promotion: an owl:rational can be promoted into a xsd:float
		owlRationalDatatype = createConcreteNumericTermType(OWL.RATIONAL, owlRealDatatype.getAncestry(),
				xsdFloatDatatype.getPromotionSubstitutionHierarchy(), true);
		registerDatatype(owlRationalDatatype);
		xsdDecimalDatatype = createConcreteNumericTermType(XSD.DECIMAL, owlRationalDatatype, true);
		registerDatatype(xsdDecimalDatatype);
		xsdIntegerDatatype = createConcreteNumericTermType(XSD.INTEGER, xsdDecimalDatatype, true);
		registerDatatype(xsdIntegerDatatype);

		xsdNonPositiveIntegerDatatype = createConcreteNumericTermType(XSD.NON_POSITIVE_INTEGER,
				xsdIntegerDatatype, false);
		registerDatatype(xsdNonPositiveIntegerDatatype);
		xsdNegativeIntegerDatatype = createConcreteNumericTermType(XSD.NEGATIVE_INTEGER,
				xsdNonPositiveIntegerDatatype, false);
		registerDatatype(xsdNegativeIntegerDatatype);

		xsdLongDatatype = createConcreteNumericTermType(XSD.LONG, xsdIntegerDatatype,false);
		registerDatatype(xsdLongDatatype);
		xsdIntDatatype = createConcreteNumericTermType(XSD.INT, xsdLongDatatype,false);
		registerDatatype(xsdIntDatatype);
		xsdShortDatatype = createConcreteNumericTermType(XSD.SHORT, xsdIntDatatype, false);
		registerDatatype(xsdShortDatatype);
		xsdByteDatatype = createConcreteNumericTermType(XSD.BYTE, xsdShortDatatype, false);
		registerDatatype(xsdByteDatatype);

		xsdNonNegativeIntegerDatatype = createConcreteNumericTermType(XSD.NON_NEGATIVE_INTEGER,
				xsdIntegerDatatype,false);
		registerDatatype(xsdNonNegativeIntegerDatatype);

		xsdUnsignedLongDatatype = createConcreteNumericTermType(XSD.UNSIGNED_LONG, xsdIntegerDatatype, false);
		registerDatatype(xsdUnsignedLongDatatype);
		xsdUnsignedIntDatatype = createConcreteNumericTermType(XSD.UNSIGNED_INT, xsdUnsignedLongDatatype,false);
		registerDatatype(xsdUnsignedIntDatatype);

		xsdUnsignedShortDatatype = createConcreteNumericTermType(XSD.UNSIGNED_SHORT, xsdUnsignedIntDatatype, false);
		registerDatatype(xsdUnsignedShortDatatype);
		xsdUnsignedByteDatatype = createConcreteNumericTermType(XSD.UNSIGNED_BYTE, xsdUnsignedShortDatatype, false);
		registerDatatype(xsdUnsignedByteDatatype);

		xsdPositiveIntegerDatatype = createConcreteNumericTermType(XSD.POSITIVE_INTEGER,
				xsdNonNegativeIntegerDatatype,false);
		registerDatatype(xsdPositiveIntegerDatatype);

		xsdBooleanDatatype = createSimpleRDFDatatype(XSD.BOOLEAN, rdfsLiteralDatatype.getAncestry());
		registerDatatype(xsdBooleanDatatype);

		xsdStringDatatype = createSimpleRDFDatatype(XSD.STRING, rdfsLiteralDatatype.getAncestry());
		registerDatatype(xsdStringDatatype);

		defaultUnsupportedDatatype = UnsupportedRDFDatatype.createUnsupportedDatatype(rdfsLiteralDatatype.getAncestry());

		xsdTimeDatatype = createSimpleRDFDatatype(XSD.TIME, rdfsLiteralDatatype.getAncestry());
		registerDatatype(xsdTimeDatatype);
		xsdDateDatatype = createSimpleRDFDatatype(XSD.DATE, rdfsLiteralDatatype.getAncestry());
		registerDatatype(xsdDateDatatype);
		xsdDatetimeDatatype = createSimpleRDFDatatype(XSD.DATETIME, rdfsLiteralDatatype.getAncestry());
		registerDatatype(xsdDatetimeDatatype);
		xsdDatetimeStampDatatype = createSimpleRDFDatatype(XSD.DATETIMESTAMP, xsdDatetimeDatatype.getAncestry());
		registerDatatype(xsdDatetimeStampDatatype);
		xsdGYearDatatype = createSimpleRDFDatatype(XSD.GYEAR, rdfsLiteralDatatype.getAncestry());
		registerDatatype(xsdGYearDatatype);

		xsdBase64Datatype = createSimpleRDFDatatype(XSD.BASE64BINARY, rdfsLiteralDatatype.getAncestry(), false);
		registerDatatype(xsdBase64Datatype);
	}

	private void registerDatatype(RDFDatatype datatype) {
		datatypeCache.put(datatype.getIRI(), datatype);
	}

	@Override
	public RDFDatatype getLangTermType(String languageTagString) {
		return langTypeCache
				.computeIfAbsent(languageTagString.toLowerCase(), this::createLangStringDatatype);
	}

	@Override
	public RDFDatatype getDatatype(IRI iri) {
		return datatypeCache.computeIfAbsent(
				iri,
				// Non-predefined datatypes cannot be declared as the child of a concrete datatype
				i -> createSimpleRDFDatatype(i, rdfsLiteralDatatype.getAncestry()));
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

    private RDFDatatype createLangStringDatatype(String languageTagString) {
		return LangDatatype.createLangDatatype(
				new LanguageTagImpl(languageTagString), xsdStringDatatype.getAncestry(), this);
	}
}
