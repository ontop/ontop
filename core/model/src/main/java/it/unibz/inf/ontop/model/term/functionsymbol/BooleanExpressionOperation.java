package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.impl.FunctionalTermNullabilityImpl;
import it.unibz.inf.ontop.model.type.ArgumentValidator;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TermTypeInferenceRule;
import it.unibz.inf.ontop.model.type.impl.SimpleArgumentValidator;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceRules;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;

import java.util.Optional;

import static it.unibz.inf.ontop.model.type.impl.TermTypeInferenceRules.*;
import static it.unibz.inf.ontop.model.type.impl.TermTypeInferenceRules.RDF_TERM_TYPE;

/**
 * TEMPORARY
 */
public enum BooleanExpressionOperation implements BooleanFunctionSymbol {

    AND("AND", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, XSD_BOOLEAN_DT, XSD_BOOLEAN_DT),
    OR("OR", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, XSD_BOOLEAN_DT, XSD_BOOLEAN_DT),
    NOT("NOT", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, XSD_BOOLEAN_DT),

    EQ("EQ", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE, RDF_TERM_TYPE),
    NEQ("NEQ", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE, RDF_TERM_TYPE),
    /*
     * BC: is it defined for IRIs?
     */
    GTE("GTE", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
    GT("GT", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
    LTE("LTE", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
    LT("LT", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),

    IS_NULL("IS_NULL", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),
    IS_NOT_NULL("IS_NOT_NULL", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),
    IS_TRUE("IS_TRUE", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),

    STR_STARTS("STRSTARTS", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, COMPATIBLE_STRING_VALIDATOR),
    STR_ENDS("STRENDS", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, COMPATIBLE_STRING_VALIDATOR),
    CONTAINS("CONTAINS", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, COMPATIBLE_STRING_VALIDATOR),

    /* SPARQL built-in predicates */

    IS_NUMERIC("isNumeric", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),
    IS_LITERAL("isLiteral", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),
    IS_IRI("isIRI", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),
    IS_BLANK("isBlank", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),
    LANGMATCHES("LangMatches", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
    REGEX("regex", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT, RDFS_LITERAL_DT),

    // ROMAN (23 Dec 2015) THIS COMES ONLY FROM MAPPINGS
    SQL_LIKE("like", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT);

    // unary operations
    BooleanExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule,
                        @Nonnull TermType arg1) {
        this.name = name;
        this.termTypeInferenceRule = termTypeInferenceRule;
        this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of(arg1));
    }
    // binary operations
    BooleanExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule,
                        @Nonnull TermType arg1, @Nonnull TermType arg2) {
        this.name = name;
        this.termTypeInferenceRule = termTypeInferenceRule;
        this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of(arg1, arg2));
    }
    // ternary operations
    BooleanExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule,
                        @Nonnull TermType arg1, @Nonnull TermType arg2, @Nonnull TermType arg3) {
        this.name = name;
        this.termTypeInferenceRule = termTypeInferenceRule;
        this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of(arg1, arg2, arg3));
    }

    BooleanExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule,
                        @Nonnull ArgumentValidator argumentValidator) {
        this.name = name;
        this.termTypeInferenceRule = termTypeInferenceRule;
        this.argumentValidator = argumentValidator;
    }

    private final String name;
    private final TermTypeInferenceRule termTypeInferenceRule;
    private final ArgumentValidator argumentValidator;


    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getArity() {
        return argumentValidator.getExpectedBaseArgumentTypes().size();
    }

    @Override
    public TermType getExpectedBaseType(int index) {
        return argumentValidator.getExpectedBaseType(index);
    }

    @Override
    public ImmutableList<TermType> getExpectedBaseArgumentTypes() {
        return argumentValidator.getExpectedBaseArgumentTypes();
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) throws FatalTypingException {

        ImmutableList<Optional<TermTypeInference>> argumentTypes = terms.stream()
                .map(ImmutableTerm::inferType)
                .collect(ImmutableCollectors.toList());

        return inferTypeFromArgumentTypes(argumentTypes);
    }

    @Override
    public Optional<TermTypeInference> inferTypeFromArgumentTypes(ImmutableList<Optional<TermTypeInference>> argumentTypes) {
        argumentValidator.validate(argumentTypes);

        return termTypeInferenceRule.inferTypeFromArgumentTypes(argumentTypes);
    }


    /**
     * TODO: IMPLEMENT IT SERIOUSLY
     */
    @Override
    public FunctionalTermNullability evaluateNullability(ImmutableList<? extends NonFunctionalTerm> arguments,
                                                         VariableNullability childNullability) {
        boolean isNullable = arguments.stream()
                .filter(a -> a instanceof Variable)
                .anyMatch(a -> childNullability.isPossiblyNullable((Variable) a));
        return new FunctionalTermNullabilityImpl(isNullable);
    }

}
