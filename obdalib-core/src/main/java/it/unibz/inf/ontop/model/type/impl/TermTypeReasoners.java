package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermTypeReasoner;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.*;

/**
 * TODO: explain
 */
public class TermTypeReasoners {

    public static final TermTypeReasoner PREDEFINED_OBJECT_REASONER = new PredefinedTermTypeReasoner(OBJECT);
    public static final TermTypeReasoner PREDEFINED_LITERAL_REASONER = new PredefinedTermTypeReasoner(LITERAL);
    public static final TermTypeReasoner PREDEFINED_BOOLEAN_REASONER = new PredefinedTermTypeReasoner(BOOLEAN);
    public static final TermTypeReasoner PREDEFINED_INTEGER_REASONER = new PredefinedTermTypeReasoner(INTEGER);
    public static final TermTypeReasoner PREDEFINED_DECIMAL_REASONER = new PredefinedTermTypeReasoner(DECIMAL);
    public static final TermTypeReasoner PREDEFINED_DOUBLE_REASONER = new PredefinedTermTypeReasoner(DOUBLE);
    public static final TermTypeReasoner PREDEFINED_DATETIME_STAMP_REASONER = new PredefinedTermTypeReasoner(DATETIME_STAMP);
    /**
     * TODO: explain
     */
    public static final TermTypeReasoner STRING_LANG_REASONER = new UnifierTermTypeReasoner();

    /**
     * TODO: explain
     */
    public static final TermTypeReasoner STANDARD_NUMERIC_REASONER = new NumericTermTypeReasoner();

    /**
     * Cannot infer COL_TYPE.INTEGER (will put COL_TYPE.DECIMAL instead)
     */
    public static final TermTypeReasoner NON_INTEGER_NUMERIC_REASONER = new NonIntegerNumericReasoner();

    /**
     * TODO: explain
     */
    public static final TermTypeReasoner FIRST_ARG_REASONER = new FirstArgumentTermTypeReasoner();

    /**
     * TODO: explain
     */
    public static final TermTypeReasoner SECOND_ARG_REASONER = new SecondArgumentTermTypeReasoner();
}
