package it.unibz.inf.ontop.model.type;

import java.util.Optional;

/**
 * Hierarchy used for type propagation and type substitution between CONCRETE numeric RDF datatypes
 *
 * Does not contain any abstract RDF datatype
 *
 */
public interface TypePropagationSubstitutionHierarchy extends TermTypeHierarchy<ConcreteNumericRDFDatatype> {

    Optional<ConcreteNumericRDFDatatype> getClosestCommonType(TypePropagationSubstitutionHierarchy otherHierarchy);

    /**
     * Builds a new hierarchy
     */
    TypePropagationSubstitutionHierarchy newHierarchy(ConcreteNumericRDFDatatype childType);
}
