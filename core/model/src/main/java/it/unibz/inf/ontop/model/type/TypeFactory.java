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

	/**
	 * Temporary solution to model nested views
	 * Returns an integer which is not in {@link java.sql.Types}
	 */
	default int getUnderspecifiedDBType() {
		return -1000;
	}

	/**
	 * Default solution for attribute type in relations of:
	 * . parser views
	 * . FlattenNode data atoms
	 */
	default RDFDatatype getDefaultRDFDatatype() {
		return getDatatype(XSD.STRING);
	}
}
