package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBCoalesceFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class LexicalLangMatchesFunctionSymbolImpl extends BooleanFunctionSymbolImpl {

    protected LexicalLangMatchesFunctionSymbolImpl(DBTermType dbStringType, DBTermType dbBooleanTermType) {
        super("LEX_LANG_MATCHES", ImmutableList.of(dbStringType, dbStringType), dbBooleanTermType);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        if (newTerms.stream().anyMatch(t -> (t instanceof Constant) && t.isNull()))
            return termFactory.getNullConstant();

        if (newTerms.stream().allMatch(t -> t instanceof NonNullConstant)) {
            ImmutableList<NonNullConstant> constants = (ImmutableList<NonNullConstant>) (ImmutableList<?>) newTerms;
            return termFactory.getDBBooleanConstant(isMatching(constants.get(0).getValue(), constants.get(1).getValue()));
        }

        if (newTerms.get(0) instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm firstTerm = (ImmutableFunctionalTerm) newTerms.get(0);

            if (firstTerm.getFunctionSymbol() instanceof DBCoalesceFunctionSymbol) {
                ImmutableTerm secondTerm = newTerms.get(1);

                ImmutableList<ImmutableTerm> newSubTerms = firstTerm.getTerms().stream()
                        .map(t -> termFactory.getImmutableFunctionalTerm(this, t, secondTerm))
                        .collect(ImmutableCollectors.toList());

                return termFactory.getDBBooleanCoalesce(newSubTerms).simplify(variableNullability);
            }

        }

        // TODO: simplify in the presence of magic numbers

        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    /**
     * TODO: implement more seriously
     */
    private boolean isMatching(String langTag, String langRange) {
        if (langRange.equals("*"))
            return !langTag.isEmpty();

        return langTag.toLowerCase().startsWith(langRange.toLowerCase());
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }
}
