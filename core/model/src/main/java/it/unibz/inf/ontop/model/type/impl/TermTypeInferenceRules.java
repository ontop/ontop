package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.model.type.*;

/**
 * TODO: explain
 */
public class TermTypeInferenceRules {

    public static final RDFDatatype ONTOP_NUMERIC_DT;
    public static final RDFDatatype XSD_INTEGER_DT;
    public static final RDFDatatype XSD_BOOLEAN_DT;
    public static final RDFDatatype XSD_DECIMAL_DT;
    public static final RDFDatatype XSD_DATETIME_DT;
    public static final RDFDatatype XSD_DOUBLE_DT;
    public static final RDFDatatype XSD_STRING_DT;
    public static final RDFDatatype RDFS_LITERAL_DT;
    public static final RDFTermType RDF_TERM_TYPE;
    public static final ObjectRDFType IRI_RDF_TYPE;
    public static final TermType ROOT_TERM_TYPE;

    public static final TermTypeInferenceRule PREDEFINED_IRI_RULE;
    public static final TermTypeInferenceRule PREDEFINED_STRING_RULE;
    public static final TermTypeInferenceRule PREDEFINED_BOOLEAN_RULE;
    public static final TermTypeInferenceRule PREDEFINED_INTEGER_RULE;
    public static final TermTypeInferenceRule PREDEFINED_DECIMAL_RULE;
    public static final TermTypeInferenceRule PREDEFINED_DOUBLE_RULE;
    public static final TermTypeInferenceRule PREDEFINED_DATETIME_RULE;

    public static final ArgumentValidator COMPATIBLE_STRING_VALIDATOR;


    /**
     * TODO: explain
     */
    public static final TermTypeInferenceRule STRING_LANG_RULE;

    /**
     * TODO: explain
     */
    public static final TermTypeInferenceRule STANDARD_NUMERIC_RULE;

    /**
     * Cannot infer COL_TYPE.INTEGER (will put COL_TYPE.DECIMAL instead)
     */
    public static final TermTypeInferenceRule NON_INTEGER_NUMERIC_RULE;

    /**
     * TODO: explain
     */
    public static final TermTypeInferenceRule FIRST_ARG_RULE;

    /**
     * TODO: explain
     */
    public static final TermTypeInferenceRule SECOND_ARG_RULE;
    
    static {
        // UGLY!! Temporary hack
        TypeFactory typeFactory = OntopModelConfiguration.defaultBuilder().build().getTypeFactory();

        ONTOP_NUMERIC_DT = typeFactory.getAbstractOntopNumericDatatype();
        XSD_INTEGER_DT = typeFactory.getXsdIntegerDatatype();
        XSD_BOOLEAN_DT = typeFactory.getXsdBooleanDatatype();
        XSD_DECIMAL_DT = typeFactory.getXsdDecimalDatatype();
        XSD_DATETIME_DT = typeFactory.getXsdDatetimeDatatype();
        XSD_DOUBLE_DT = typeFactory.getXsdDoubleDatatype();
        XSD_STRING_DT = typeFactory.getXsdStringDatatype();
        RDFS_LITERAL_DT = typeFactory.getAbstractRDFSLiteral();
        RDF_TERM_TYPE = typeFactory.getAbstractRDFTermType();
        IRI_RDF_TYPE = typeFactory.getIRITermType();
        ROOT_TERM_TYPE = typeFactory.getAbstractAtomicTermType();

        PREDEFINED_IRI_RULE = new PredefinedTermTypeInferenceRule(IRI_RDF_TYPE);
        PREDEFINED_STRING_RULE = new PredefinedTermTypeInferenceRule(XSD_STRING_DT);
        PREDEFINED_BOOLEAN_RULE = new PredefinedTermTypeInferenceRule(XSD_BOOLEAN_DT);
        PREDEFINED_INTEGER_RULE = new PredefinedTermTypeInferenceRule(XSD_INTEGER_DT);
        PREDEFINED_DECIMAL_RULE = new PredefinedTermTypeInferenceRule(XSD_DECIMAL_DT);
        PREDEFINED_DOUBLE_RULE = new PredefinedTermTypeInferenceRule(XSD_DOUBLE_DT);
        PREDEFINED_DATETIME_RULE = new PredefinedTermTypeInferenceRule(XSD_DATETIME_DT);

        COMPATIBLE_STRING_VALIDATOR = new FirstTypeIsASecondTypeValidator(
                ImmutableList.of(XSD_STRING_DT, XSD_STRING_DT));


        /**
         * TODO: explain
         */
        STRING_LANG_RULE = new UnifierTermTypeInferenceRule();

        /**
         * TODO: explain
         */
        STANDARD_NUMERIC_RULE = new NumericTermTypeInferenceRule();

        /**
         * Cannot infer COL_TYPE.INTEGER (will put COL_TYPE.DECIMAL instead)
         */
        NON_INTEGER_NUMERIC_RULE = new NonIntegerNumericInferenceRule(typeFactory);

        /**
         * TODO: explain
         */
        FIRST_ARG_RULE = new FirstArgumentTermTypeInferenceRule();

        /**
         * TODO: explain
         */
        SECOND_ARG_RULE = new SecondArgumentTermTypeInferenceRule();
        
    }
}
