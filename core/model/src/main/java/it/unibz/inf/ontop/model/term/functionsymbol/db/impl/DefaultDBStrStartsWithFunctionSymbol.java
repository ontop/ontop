package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.template.Template;
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
                ImmutableFunctionalTerm firstFunctionalTerm = (ImmutableFunctionalTerm) newTerms.get(0);
                if (firstFunctionalTerm.getFunctionSymbol() instanceof IRIStringTemplateFunctionSymbol) {
                    Template.Component firstComponent = ((IRIStringTemplateFunctionSymbol) firstFunctionalTerm.getFunctionSymbol())
                            .getTemplateComponents().stream().findFirst()
                            .orElseThrow(() -> new MinorOntopInternalBugException("A template should have at least one component"));
                    if (!firstComponent.isColumn()) {
                        String firstComponentString = firstComponent.getComponent();
                        if (firstComponentString.startsWith(prefixString))
                            // True (or null)
                            return termFactory.getTrueOrNullFunctionalTerm(
                                    ImmutableList.of(termFactory.getDBIsNotNull(firstFunctionalTerm)))
                                    .simplify(variableNullability);
                        else if ((prefixString.length() <= firstComponentString.length())
                                || (! firstComponentString.startsWith(prefixString.substring(0, firstComponentString.length())))) {
                            // False (or null)
                            return termFactory.getFalseOrNullFunctionalTerm(ImmutableList.of(termFactory.getDBIsNull(firstFunctionalTerm)))
                                    .simplify(variableNullability);
                        }
                    }
                    // TODO: shall we further simplify the other cases?
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
