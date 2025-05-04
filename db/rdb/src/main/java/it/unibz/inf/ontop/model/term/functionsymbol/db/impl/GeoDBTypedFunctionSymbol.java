package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.GeoTermUtils;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;


public class GeoDBTypedFunctionSymbol extends DefaultSQLSimpleTypedDBFunctionSymbol{
    public GeoDBTypedFunctionSymbol(String nameInDialect, int arity, DBTermType targetType, boolean isInjective,
                                    DBTermType rootDBTermType) {
        super(nameInDialect, arity, targetType, isInjective, rootDBTermType);
    }

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableList<ImmutableTerm> simplifiedTerms = terms.stream().map(GeoTermUtils::unwrapSTAsText).collect(ImmutableCollectors.toList());
        return super.simplify(simplifiedTerms, termFactory, variableNullability);
    }
}

