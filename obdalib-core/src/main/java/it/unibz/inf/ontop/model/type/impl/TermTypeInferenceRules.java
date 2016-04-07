package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermTypeInferenceRule;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.*;

/**
 * TODO: explain
 */
public class TermTypeInferenceRules {

    public static final TermTypeInferenceRule PREDEFINED_OBJECT_RULE = new PredefinedTermTypeInferenceRule(OBJECT);
    public static final TermTypeInferenceRule PREDEFINED_LITERAL_RULE = new PredefinedTermTypeInferenceRule(LITERAL);
    public static final TermTypeInferenceRule PREDEFINED_BOOLEAN_RULE = new PredefinedTermTypeInferenceRule(BOOLEAN);
    public static final TermTypeInferenceRule PREDEFINED_INTEGER_RULE = new PredefinedTermTypeInferenceRule(INTEGER);
    public static final TermTypeInferenceRule PREDEFINED_DECIMAL_RULE = new PredefinedTermTypeInferenceRule(DECIMAL);
    public static final TermTypeInferenceRule PREDEFINED_DOUBLE_RULE = new PredefinedTermTypeInferenceRule(DOUBLE);
    public static final TermTypeInferenceRule PREDEFINED_DATETIME_RULE = new PredefinedTermTypeInferenceRule(DATETIME);
    /**
     * TODO: explain
     */
    public static final TermTypeInferenceRule STRING_LANG_RULE = new UnifierTermTypeInferenceRule();

    /**
     * TODO: explain
     */
    public static final TermTypeInferenceRule STANDARD_NUMERIC_RULE = new NumericTermTypeInferenceRule();

    /**
     * Cannot infer COL_TYPE.INTEGER (will put COL_TYPE.DECIMAL instead)
     */
    public static final TermTypeInferenceRule NON_INTEGER_NUMERIC_RULE = new NonIntegerNumericInferenceRule();

    /**
     * TODO: explain
     */
    public static final TermTypeInferenceRule FIRST_ARG_RULE = new FirstArgumentTermTypeInferenceRule();

    /**
     * TODO: explain
     */
    public static final TermTypeInferenceRule SECOND_ARG_RULE = new SecondArgumentTermTypeInferenceRule();
}
