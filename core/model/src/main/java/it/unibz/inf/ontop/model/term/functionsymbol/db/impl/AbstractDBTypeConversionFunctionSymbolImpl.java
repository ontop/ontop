package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
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
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
        // Null
        if (isOneArgumentNull(newTerms))
            return termFactory.getNullConstant();

        ImmutableTerm subTerm = newTerms.get(0);

        // Non null
        return (subTerm instanceof DBConstant)
                ? convertDBConstant((DBConstant) subTerm, termFactory)
                : termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    protected abstract DBConstant convertDBConstant(DBConstant constant, TermFactory termFactory);
}
