package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

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
	RDFDatatype getAbstractRDFSLiteral();

	TermType getAbstractAtomicTermType();

	RDFTermType getAbstractRDFTermType();

	ObjectRDFType getAbstractObjectRDFType();

	default RDFDatatype getXsdIntegerDatatype() {
		return getDatatype(XSD.INTEGER);
	}

	default RDFDatatype getXsdDecimalDatatype() {
		return getDatatype(XSD.DECIMAL);
	}

	default RDFDatatype getXsdStringDatatype() {
		return getDatatype(XSD.STRING);
	}

	default RDFDatatype getXsdBooleanDatatype() {
		return getDatatype(XSD.BOOLEAN);
	}

	default RDFDatatype getXsdDoubleDatatype() {
		return getDatatype(XSD.DOUBLE);
	}

	default RDFDatatype getXsdFloatDatatype() {
		return getDatatype(XSD.FLOAT);
	}

	default RDFDatatype getXsdDatetimeDatatype() {
		return getDatatype(XSD.DATETIME);
	}

	default RDFDatatype getXsdDatetimeStampDatatype() {
		return getDatatype(XSD.DATETIMESTAMP);
	}

	MetaRDFTermType getMetaRDFTermType();

	DBTypeFactory getDBTypeFactory();
}
