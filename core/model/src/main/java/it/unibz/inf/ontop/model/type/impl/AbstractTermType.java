package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

import static it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools.getCommonDenominatorType;

/**
 * TODO: integrate into a factory
 */
public class AbstractTermType implements TermType {

    private final Predicate.COL_TYPE colType;

    protected AbstractTermType(Predicate.COL_TYPE colType) {
        this.colType = colType;
    }

    @Override
    public Predicate.COL_TYPE getColType() {
        return colType;
    }

    /**
     * TODO: refactor
     */
    @Override
    public boolean isCompatibleWith(TermType moreGeneralType) {
        TermType commonDenominator = getCommonDenominator(moreGeneralType);
        return moreGeneralType.equals(commonDenominator);
    }

    /**
     * Can be overloaded
     *
     * TODO: refactor it
     */
    @Override
    public TermType getCommonDenominator(TermType otherTermType) {
        return getCommonDenominatorType(getColType(), otherTermType.getColType());
    }

    /**
     * TODO: refactor
     */
    @Override
    public boolean equals(Object other) {
        return Optional.ofNullable(other)
                .filter(o -> (o instanceof TermType))
                .map(o -> (TermType) o)
                .filter(o -> colType == o.getColType())
                .isPresent();
    }

    @Override
    public String toString() {
        return colType.toString();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
