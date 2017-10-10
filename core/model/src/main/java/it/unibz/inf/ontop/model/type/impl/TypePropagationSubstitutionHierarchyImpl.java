package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.type.ConcreteNumericRDFDatatype;
import it.unibz.inf.ontop.model.type.TypePropagationSubstitutionHierarchy;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;


public class TypePropagationSubstitutionHierarchyImpl extends TermTypeHierarchyImpl<ConcreteNumericRDFDatatype>
        implements TypePropagationSubstitutionHierarchy {

    protected TypePropagationSubstitutionHierarchyImpl(ConcreteNumericRDFDatatype topDatatype) {
        this(ImmutableList.of(topDatatype));
    }

    private TypePropagationSubstitutionHierarchyImpl(ImmutableList<ConcreteNumericRDFDatatype> types) {
        super(types);
    }

    @Override
    public ConcreteNumericRDFDatatype getClosestCommonType(TypePropagationSubstitutionHierarchy otherHierarchy) {
        return getClosestCommonTermType(otherHierarchy)
                .orElseThrow(DifferentTopPropagatedNumericTypeException::new);
    }

    @Override
    public TypePropagationSubstitutionHierarchy newHierarchy(ConcreteNumericRDFDatatype childType) {
        return new TypePropagationSubstitutionHierarchyImpl(
                Stream.concat(getTermTypes(), Stream.of(childType))
                    .collect(ImmutableCollectors.toList()));
    }

    /**
     * Internal bug: all the concrete numeric types must be convertible into xsd:double
     */
    private static class DifferentTopPropagatedNumericTypeException extends OntopInternalBugException {

        private DifferentTopPropagatedNumericTypeException() {
            super("Internal bug: all the concrete numeric types must be convertible into xsd:double");
        }
    }
}
