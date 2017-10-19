package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.*;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

/**
 * TODO: explain
 */
public class TermTypeInferenceRules {

    public static final RDFDatatype ONTOP_NUMERIC_DT = TYPE_FACTORY.getAbstractOntopNumericDatatype();
    public static final RDFDatatype XSD_INTEGER_DT = TYPE_FACTORY.getXsdIntegerDatatype();
    public static final RDFDatatype XSD_BOOLEAN_DT = TYPE_FACTORY.getXsdBooleanDatatype();
    public static final RDFDatatype XSD_DECIMAL_DT = TYPE_FACTORY.getXsdDecimalDatatype();
    public static final RDFDatatype XSD_DATETIME_DT = TYPE_FACTORY.getXsdDatetimeDatatype();
    public static final RDFDatatype XSD_DOUBLE_DT = TYPE_FACTORY.getXsdDoubleDatatype();
    public static final RDFDatatype XSD_STRING_DT = TYPE_FACTORY.getXsdStringDatatype();
    public static final RDFDatatype RDFS_LITERAL_DT = TYPE_FACTORY.getAbstractRDFSLiteral();
    public static final RDFTermType RDF_TERM_TYPE = TYPE_FACTORY.getAbstractRDFTermType();
    public static final ObjectRDFType IRI_RDF_TYPE = TYPE_FACTORY.getIRITermType();
    public static final TermType ROOT_TERM_TYPE = TYPE_FACTORY.getAbstractAtomicTermType();

    public static final TermTypeInferenceRule PREDEFINED_IRI_RULE = new PredefinedTermTypeInferenceRule(IRI_RDF_TYPE);
    public static final TermTypeInferenceRule PREDEFINED_STRING_RULE = new PredefinedTermTypeInferenceRule(XSD_STRING_DT);
    public static final TermTypeInferenceRule PREDEFINED_BOOLEAN_RULE = new PredefinedTermTypeInferenceRule(XSD_BOOLEAN_DT);
    public static final TermTypeInferenceRule PREDEFINED_INTEGER_RULE = new PredefinedTermTypeInferenceRule(XSD_INTEGER_DT);
    public static final TermTypeInferenceRule PREDEFINED_DECIMAL_RULE = new PredefinedTermTypeInferenceRule(XSD_DECIMAL_DT);
    public static final TermTypeInferenceRule PREDEFINED_DOUBLE_RULE = new PredefinedTermTypeInferenceRule(XSD_DOUBLE_DT);
    public static final TermTypeInferenceRule PREDEFINED_DATETIME_RULE = new PredefinedTermTypeInferenceRule(XSD_DATETIME_DT);

    public static final ArgumentValidator COMPATIBLE_STRING_VALIDATOR = new FirstTypeIsASecondTypeValidator(
            ImmutableList.of(XSD_STRING_DT, XSD_STRING_DT));


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
