package it.unibz.inf.ontop.model.type;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.DatatypePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import java.util.Optional;

public interface TypeFactory {

	@Deprecated
	Optional<RDFDatatype> getOptionalDatatype(String uri);

	Optional<RDFDatatype> getOptionalDatatype(IRI iri);

	Optional<TermType> getInternalType(DatatypePredicate predicate);

	@Deprecated
	DatatypePredicate getTypePredicate(COL_TYPE type);

	DatatypePredicate getTypePredicate(TermType type);
		
	boolean isBoolean(Predicate p);
	
	boolean isInteger(Predicate p);
	
	boolean isFloat(Predicate p);
	
//	boolean isLiteral(Predicate p);
	
	boolean isString(Predicate p);
	
	ImmutableList<Predicate> getDatatypePredicates();

	/**
	 * TODO: refactor it
	 */
	TermType getTermType(COL_TYPE type);
	RDFDatatype getLangTermType(String languageTag);

	/**
	 * Don't call it with langString!
	 */
	RDFDatatype getDatatype(IRI iri);

	ObjectRDFType getIRITermType();

	ObjectRDFType getBlankNodeType();

	UnboundRDFTermType getUnboundTermType();

	RDFDatatype getUnsupportedDatatype();

	RDFDatatype getAbstractOntopNumericDatatype();
	RDFDatatype getAbstractRDFSLiteral();

	TermType getAbstractAtomicTermType();

	RDFTermType getAbstractRDFTermType();

	default RDFDatatype getXsdIntegerDatatype() {
		return getDatatype(XMLSchema.INTEGER);
	}

	default RDFDatatype getXsdDecimalDatatype() {
		return getDatatype(XMLSchema.DECIMAL);
	}

	default RDFDatatype getXsdStringDatatype() {
		return getDatatype(XMLSchema.STRING);
	}

	default RDFDatatype getXsdBooleanDatatype() {
		return getDatatype(XMLSchema.BOOLEAN);
	}

	default RDFDatatype getXsdDoubleDatatype() {
		return getDatatype(XMLSchema.DOUBLE);
	}

	default RDFDatatype getXsdFloatDatatype() {
		return getDatatype(XMLSchema.FLOAT);
	}

	default RDFDatatype getXsdDatetimeDatatype() {
		return getDatatype(XMLSchema.DATETIME);
	}
}
