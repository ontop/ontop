package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;


public class GeoDBBooleanFunctionSymbol extends DefaultSQLSimpleDBBooleanFunctionSymbol {

    public GeoDBBooleanFunctionSymbol(String nameInDialect, int arity, DBTermType targetType, DBTermType rootDBTermType) {
        super(nameInDialect, arity, targetType, rootDBTermType);
    }

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableList<ImmutableTerm> simplifiedTerms = terms.stream().map(this::unwrapSTAsText).collect(ImmutableCollectors.toList());
        if (simplifiedTerms.stream()
                .anyMatch(t -> t instanceof ImmutableFunctionalTerm
                        && ((ImmutableFunctionalTerm) t).getFunctionSymbol().getName().startsWith("ST_ASTEXT"))) {
        }
        return super.simplify(simplifiedTerms, termFactory, variableNullability);
    }

    // if term is ST_ASTEXT(arg), returns arg, otherwise the term itself
    private ImmutableTerm unwrapSTAsText(ImmutableTerm term) {
        return Optional.of(term)
                // term is a function
                .filter(t -> t instanceof ImmutableFunctionalTerm).map(ImmutableFunctionalTerm.class::cast)
                // the function symbol is ST_ASTEXT
                .filter(t -> t.getFunctionSymbol().getName().startsWith("ST_ASTEXT"))
                // extract the 0-th argument
                .map(t -> t.getTerm(0))
                // otherwise the term itself
                .orElse(term);
    }
}
