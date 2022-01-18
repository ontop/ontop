package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.model.vocabulary.GEO;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.rdf.api.IRI;

/**
 * Accessible through Guice (recommended) or through CoreSingletons.
 */
public interface TypeFactory {

	RDFDatatype getLangTermType(String languageTag);

	/**
	 * Don't call it with langString!
	 */
	RDFDatatype getDatatype(IRI iri);

	ObjectRDFType getIRITermType();

	ObjectRDFType getBlankNodeType();

	RDFDatatype getUnsupportedDatatype();

	RDFDatatype getAbstractOntopNumericDatatype();
	RDFDatatype getAbstractOntopDateOrDatetimeDatatype();

	RDFDatatype getAbstractRDFSLiteral();

	TermType getAbstractAtomicTermType();

	RDFTermType getAbstractRDFTermType();

	ObjectRDFType getAbstractObjectRDFType();

	default ConcreteNumericRDFDatatype getXsdIntegerDatatype() {
		return (ConcreteNumericRDFDatatype) getDatatype(XSD.INTEGER);
	}

	default ConcreteNumericRDFDatatype getXsdLongDatatype() {
		return (ConcreteNumericRDFDatatype) getDatatype(XSD.LONG);
	}

	default ConcreteNumericRDFDatatype getXsdDecimalDatatype() {
		return (ConcreteNumericRDFDatatype) getDatatype(XSD.DECIMAL);
	}

	default RDFDatatype getXsdStringDatatype() {
		return getDatatype(XSD.STRING);
	}

	default RDFDatatype getXsdBooleanDatatype() {
		return getDatatype(XSD.BOOLEAN);
	}

	default ConcreteNumericRDFDatatype getXsdDoubleDatatype() {
		return (ConcreteNumericRDFDatatype)  getDatatype(XSD.DOUBLE);
	}

	default ConcreteNumericRDFDatatype getXsdFloatDatatype() {
		return (ConcreteNumericRDFDatatype)  getDatatype(XSD.FLOAT);
	}

	default RDFDatatype getXsdDatetimeDatatype() {
		return getDatatype(XSD.DATETIME);
	}

	default RDFDatatype getXsdDatetimeStampDatatype() {
		return getDatatype(XSD.DATETIMESTAMP);
	}

	MetaRDFTermType getMetaRDFTermType();

	DBTypeFactory getDBTypeFactory();

    default RDFDatatype getWktLiteralDatatype() {
        return getDatatype(GEO.GEO_WKT_LITERAL);
    }

	default RDFDatatype getXsdAnyUri() {
		return getDatatype(XSD.ANYURI);
	}
}
