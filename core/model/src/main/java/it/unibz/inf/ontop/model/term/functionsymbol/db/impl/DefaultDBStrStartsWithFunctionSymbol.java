package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;


public class DefaultDBStrStartsWithFunctionSymbol extends DBBooleanFunctionSymbolImpl {

    /**
     * TODO: type the input
     */
    protected DefaultDBStrStartsWithFunctionSymbol(DBTermType metaDBTermType, DBTermType dbBooleanTermType) {
        super("STR_STARTS_WITH", ImmutableList.of(metaDBTermType, metaDBTermType), dbBooleanTermType);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException("DefaultDBStrStartsWithFunctionSymbol blocks negation");
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableTerm secondTerm = terms.get(1);
        // TODO: use a non-strict equality
        return termConverter.apply(
                termFactory.getStrictEquality(
                        termFactory.getDBSubString3(
                                terms.get(0),
                                termFactory.getDBIntegerConstant(1),
                                termFactory.getDBCharLength(secondTerm)),
                        secondTerm));
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory, VariableNullability variableNullability) {
        if (newTerms.get(1) instanceof DBConstant) {
            String prefixString = ((DBConstant) newTerms.get(1)).getValue();

            if (newTerms.get(0) instanceof DBConstant) {
                DBConstant firstConstant = (DBConstant) newTerms.get(0);
                return termFactory.getDBBooleanConstant(firstConstant.getValue().startsWith(prefixString));
            }

            if (newTerms.get(0) instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm firstNonFunctionalTerm = (ImmutableFunctionalTerm) newTerms.get(0);
                if (firstNonFunctionalTerm.getFunctionSymbol() instanceof IRIStringTemplateFunctionSymbol) {
                    IRIStringTemplateFunctionSymbol iriTemplate = (IRIStringTemplateFunctionSymbol) firstNonFunctionalTerm.getFunctionSymbol();
                    ImmutableTerm simplifiedTerm = iriTemplate.getTemplate().startsWith(prefixString)
                            ? termFactory.getTrueOrNullFunctionalTerm(ImmutableList.of(termFactory.getDBIsNotNull(firstNonFunctionalTerm)))
                            : termFactory.getFalseOrNullFunctionalTerm(ImmutableList.of(termFactory.getDBIsNull(firstNonFunctionalTerm)));
                    return simplifiedTerm.simplify(variableNullability);
                }

            }
        }

        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * TODO: allows it
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
