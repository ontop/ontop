package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
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

    /*
     * BC: is it defined for IRIs?
     */
    GTE("GTE", TermTypeInferenceRules.PREDEFINED_XSD_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
    GT("GT", TermTypeInferenceRules.PREDEFINED_XSD_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
    LTE("LTE", TermTypeInferenceRules.PREDEFINED_XSD_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
    LT("LT", TermTypeInferenceRules.PREDEFINED_XSD_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),

    IS_TRUE("IS_TRUE", TermTypeInferenceRules.PREDEFINED_DB_BOOLEAN_RULE, RDF_TERM_TYPE);

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

    /**
     * TODO: implement it?
     */
    @Override
    public EvaluationResult evaluateStrictEq(ImmutableList<? extends ImmutableTerm> terms, ImmutableTerm otherTerm, TermFactory termFactory, VariableNullability variableNullability) {
        return EvaluationResult.declareSameExpression();
    }

    /**
     * TODO: let some of them be post-processed
     * @param arguments
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        switch (this) {
            case IS_TRUE:
                return true;
                // TODO: allow additional ones
            default:
                return false;
        }
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {

        ImmutableList<Optional<TermTypeInference>> argumentTypes = terms.stream()
                .map(ImmutableTerm::inferType)
                .collect(ImmutableCollectors.toList());

        return inferTypeFromArgumentTypes(argumentTypes);
    }

    /**
     * TODO: implement it seriously after getting rid of this enum
     */
    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms,
                                  boolean isInConstructionNodeInOptimizationPhase,
                                  TermFactory termFactory, VariableNullability variableNullability) {
        if (this == IS_TRUE) {
            ImmutableTerm newTerm = terms.get(0).simplify(isInConstructionNodeInOptimizationPhase, variableNullability);
            if (newTerm instanceof Constant) {
                /*
                 * TODO: Is ok to say that IS TRUE can return NULL?
                 */
                return newTerm.isNull()
                        ? newTerm
                        : termFactory.getDBBooleanConstant(newTerm.equals(termFactory.getDBBooleanConstant(true)));
            }
            else
                return termFactory.getImmutableExpression(IS_TRUE, newTerm);
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, terms);
    }

    private Optional<TermTypeInference> inferTypeFromArgumentTypes(ImmutableList<Optional<TermTypeInference>> argumentTypes) {
        try {
            return termTypeInferenceRule.inferTypeFromArgumentTypes(argumentTypes);
        } catch (FatalTypingException e) {
            return Optional.empty();
        }
    }

    /**
     * TODO: implement seriously
     *
     */
    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, VariableNullability variableNullability) {
        return false;
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

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }
}
