package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInferenceRule;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;
import static it.unibz.inf.ontop.model.type.COL_TYPE.*;

/**
 * TODO: explain
 */
public class TermTypeInferenceRules {

    public static final TermTypeInferenceRule PREDEFINED_OBJECT_RULE = new PredefinedTermTypeInferenceRule(OBJECT);
    public static final TermTypeInferenceRule PREDEFINED_LITERAL_RULE = new PredefinedTermTypeInferenceRule(STRING);
    public static final TermTypeInferenceRule PREDEFINED_BOOLEAN_RULE = new PredefinedTermTypeInferenceRule(BOOLEAN);
    public static final TermTypeInferenceRule PREDEFINED_INTEGER_RULE = new PredefinedTermTypeInferenceRule(INTEGER);
    public static final TermTypeInferenceRule PREDEFINED_DECIMAL_RULE = new PredefinedTermTypeInferenceRule(DECIMAL);
    public static final TermTypeInferenceRule PREDEFINED_DOUBLE_RULE = new PredefinedTermTypeInferenceRule(DOUBLE);
    public static final TermTypeInferenceRule PREDEFINED_DATETIME_RULE = new PredefinedTermTypeInferenceRule(DATETIME);
    /**
     * TODO: explain
     */
    public static final TermTypeInferenceRule STRING_LANG_RULE = new StringLangTypeInferenceRule();

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
    public static final TermTypeInferenceRule FIRST_STRING_LANG_ARG_RULE = new FirstStringLangArgTermTypeInferenceRule();

    /**
     * TODO: explain
     */
    public static final TermTypeInferenceRule SECOND_ARG_RULE = new SecondArgumentTermTypeInferenceRule();


    public static final RDFDatatype ONTOP_NUMERIC_DT = TYPE_FACTORY.getAbstractOntopNumericDatatype();
    public static final RDFDatatype XSD_INTEGER_DT = TYPE_FACTORY.getXsdIntegerDatatype();
    public static final RDFDatatype XSD_BOOLEAN_DT = TYPE_FACTORY.getXsdBooleanDatatype();
    public static final RDFDatatype XSD_DATETIME_DT = TYPE_FACTORY.getXsdDatetimeDatatype();
    public static final RDFDatatype RDFS_LITERAL_DT = TYPE_FACTORY.getAbstractRDFSLiteral();
    public static final RDFTermType RDF_TERM_TYPE = TYPE_FACTORY.getAbstractRDFTermType();
    public static final TermType ROOT_TERM_TYPE = TYPE_FACTORY.getAbstractAtomicTermType();
}
