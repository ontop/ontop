package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.IRIDictionary;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Should be ALWAYS post-processed
 */
public class Int2IRIStringFunctionSymbolImpl extends FunctionSymbolImpl {

    private final DBTermType dbStringType;
    private final IRIDictionary iriDictionary;
    private final DBTermType integerType;

    public Int2IRIStringFunctionSymbolImpl(@Nonnull DBTermType integerType, @Nonnull DBTermType dbStringType,
                                              @Nonnull IRIDictionary iriDictionary) {
        super("int2IRIString-" + iriDictionary.toString(), ImmutableList.of(integerType));
        this.dbStringType = dbStringType;
        this.integerType = integerType;
        this.iriDictionary = iriDictionary;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(dbStringType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof DBConstant) {
            try {
                int id = Integer.parseInt(((DBConstant) newTerm).getValue());
                return Optional.ofNullable(iriDictionary.getURI(id))
                        .map(termFactory::getDBStringConstant)
                        .orElseThrow(() -> new MinorOntopInternalBugException("Unknown encoded ID used: " + id));

            } catch (NumberFormatException e) {
                throw new MinorOntopInternalBugException(getName() + " was expecting an integer, not " + newTerm);
            }
        }
        return termFactory.getImmutableFunctionalTerm(this, newTerm);
    }

    @Override
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {
        ImmutableTerm subTerm = terms.get(0);
        return Optional.ofNullable(iriDictionary.getId(otherTerm.getValue()))
                .filter(id -> id >= 0)
                .map(id -> termFactory.getDBConstant(String.format("%d", id), integerType))
                .map(i -> termFactory.getStrictEquality(subTerm, i))
                .map(IncrementalEvaluation::declareSimplifiedExpression)
                .orElseGet(IncrementalEvaluation::declareIsFalse);
    }
}
