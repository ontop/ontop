package it.unibz.inf.ontop.model.type;

/**
 * Hierarchy used for type propagation and type substitution between CONCRETE numeric RDF datatypes
 *
 * Does not contain any abstract RDF datatype
 *
 */
public interface TypePropagationSubstitutionHierarchy extends TermTypeHierarchy<ConcreteNumericRDFDatatype> {

    /**
     * All the concrete numeric types are expected to be convertible into xsd:double.
     */
    ConcreteNumericRDFDatatype getClosestCommonType(TypePropagationSubstitutionHierarchy otherHierarchy);

    /**
     * Builds a new hierarchy
     */
    TypePropagationSubstitutionHierarchy newHierarchy(ConcreteNumericRDFDatatype childType);
}
