package it.unibz.inf.ontop.model.type;

/**
 * TODO: explain
 *
 * Immutable
 */
public interface TermType {

    /**
     * Returns true if the TermType INSTANCE cannot be attached to a constant
     */
    boolean isAbstract();

    boolean isA(TermType otherTermType);

    TermType getCommonDenominator(TermType otherTermType);

    TermTypeAncestry getAncestry();
}
