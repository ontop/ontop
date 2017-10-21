package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.*;
import org.apache.commons.rdf.api.IRI;


public class ConcreteNumericRDFDatatypeImpl extends SimpleRDFDatatype implements ConcreteNumericRDFDatatype {

    private final TypePropagationSubstitutionHierarchy promotedHierarchy;

    private ConcreteNumericRDFDatatypeImpl(IRI datatypeIRI, TermTypeAncestry parentAncestry,
                                           TypePropagationSubstitutionHierarchy promotedParentHierarchy,
                                           boolean appendToPromotedHierarchy) {
        super(datatypeIRI, parentAncestry, false);
        promotedHierarchy = appendToPromotedHierarchy ?
                promotedParentHierarchy.newHierarchy(this)
                : promotedParentHierarchy;
    }

    private ConcreteNumericRDFDatatypeImpl(IRI datatypeIRI, TermTypeAncestry parentAncestry) {
        super(datatypeIRI, parentAncestry, false);
        promotedHierarchy = new TypePropagationSubstitutionHierarchyImpl(this);
    }

    @Override
    public TypePropagationSubstitutionHierarchy getPromotionSubstitutionHierarchy() {
        return promotedHierarchy;
    }

    @Override
    public ConcreteNumericRDFDatatype getCommonPropagatedOrSubstitutedType(ConcreteNumericRDFDatatype otherType){
        if (equals(otherType))
            return this;
        if (promotedHierarchy.contains(otherType))
            return otherType;

        TypePropagationSubstitutionHierarchy otherHierarchy = otherType.getPromotionSubstitutionHierarchy();
        if (otherHierarchy.contains(this))
            return this;
        return promotedHierarchy.getClosestCommonType(otherHierarchy);
    }

    static ConcreteNumericRDFDatatype createTopConcreteNumericTermType(IRI datatypeIRI,
                                                                       NumericRDFDatatype abstractParentDatatype) {
        if (!abstractParentDatatype.isAbstract())
            throw new IllegalArgumentException("The parent datatype must be abstract");

        return new ConcreteNumericRDFDatatypeImpl(datatypeIRI, abstractParentDatatype.getAncestry());
    }

    static ConcreteNumericRDFDatatype createConcreteNumericTermType(IRI datatypeIRI, ConcreteNumericRDFDatatype parentDatatype,
                                                                    boolean appendToPromotedHierarchy) {
        return new ConcreteNumericRDFDatatypeImpl(datatypeIRI, parentDatatype.getAncestry(),
                parentDatatype.getPromotionSubstitutionHierarchy(), appendToPromotedHierarchy);
    }

    static ConcreteNumericRDFDatatype createConcreteNumericTermType(IRI datatypeIRI, TermTypeAncestry parentAncestry,
                                                                    TypePropagationSubstitutionHierarchy promotedParentHierarchy,
                                                                    boolean appendToPromotedHierarchy) {

        return new ConcreteNumericRDFDatatypeImpl(datatypeIRI, parentAncestry, promotedParentHierarchy,
                appendToPromotedHierarchy);
    }
}
