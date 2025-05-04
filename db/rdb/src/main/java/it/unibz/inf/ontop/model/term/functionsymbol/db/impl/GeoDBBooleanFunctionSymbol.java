package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.GeoTermUtils;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


public class GeoDBBooleanFunctionSymbol extends DefaultSQLSimpleDBBooleanFunctionSymbol {

    public GeoDBBooleanFunctionSymbol(String nameInDialect, int arity, DBTermType targetType, DBTermType rootDBTermType) {
        super(nameInDialect, arity, targetType, rootDBTermType);
    }

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableList<ImmutableTerm> simplifiedTerms = terms.stream()
                .map(GeoTermUtils::unwrapSTAsText)
                .collect(ImmutableCollectors.toList());

        if (simplifiedTerms.stream()
                .anyMatch(t -> t instanceof ImmutableFunctionalTerm
                        && ((ImmutableFunctionalTerm) t).getFunctionSymbol().getName().startsWith("ST_ASTEXT"))) {
        }

        return super.simplify(simplifiedTerms, termFactory, variableNullability);
    }

}
