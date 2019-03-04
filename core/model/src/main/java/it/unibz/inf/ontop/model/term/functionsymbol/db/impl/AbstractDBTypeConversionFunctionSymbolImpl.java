package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

public abstract class AbstractDBTypeConversionFunctionSymbolImpl extends AbstractTypedDBFunctionSymbol
        implements DBTypeConversionFunctionSymbol {

    protected AbstractDBTypeConversionFunctionSymbolImpl(String name, DBTermType inputBaseType, DBTermType targetType) {
        super(name, ImmutableList.of(inputBaseType), targetType);
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm subTerm = newTerms.get(0);

        // Non null
        return (subTerm instanceof DBConstant)
                ? convertDBConstant((DBConstant) subTerm, termFactory)
                : (subTerm instanceof ImmutableFunctionalTerm)
                    ? buildTermFromFunctionalTerm((ImmutableFunctionalTerm) subTerm, termFactory, variableNullability)
                    : termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    /**
     * Default implementation
     *
     */
    protected ImmutableTerm buildTermFromFunctionalTerm(ImmutableFunctionalTerm subTerm, TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getImmutableFunctionalTerm(this, ImmutableList.of(subTerm));
    }

    protected abstract DBConstant convertDBConstant(DBConstant constant, TermFactory termFactory) throws DBTypeConversionException;

    protected static class DBTypeConversionException extends OntopInternalBugException {

        protected DBTypeConversionException(String message) {
            super(message);
        }
    }
}
