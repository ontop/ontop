package it.unibz.inf.ontop.model.type;

public interface ConcreteNumericRDFDatatype extends NumericRDFDatatype {

    /**
     * Hierarchy dealing with type promotion (see https://www.w3.org/TR/xpath20/#promotion)
     * and type substitution.
     *
     */
    TypePropagationSubstitutionHierarchy getPromotionSubstitutionHierarchy();

    ConcreteNumericRDFDatatype getCommonPropagatedOrSubstitutedType(ConcreteNumericRDFDatatype otherType);
}
