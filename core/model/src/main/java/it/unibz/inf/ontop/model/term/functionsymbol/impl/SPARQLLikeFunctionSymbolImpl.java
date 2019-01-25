package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.type.TermType;

import javax.annotation.Nonnull;

/**
 * May not implement the SPARQLFunctionSymbol interface.
 *
 * Abstraction useful for EBV, which is not strictly a SPARQL function/operator
 */
public abstract class SPARQLLikeFunctionSymbolImpl extends FunctionSymbolImpl {

    protected SPARQLLikeFunctionSymbolImpl(@Nonnull String name, @Nonnull ImmutableList<TermType> expectedBaseTypes) {
        super(name, expectedBaseTypes);
    }

    /**
     * Default value for SPARQL functions as they may produce NULL due
     * to SPARQL errors
     */
    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }

    protected boolean isRDFFunctionalTerm(ImmutableTerm term) {
        return (term instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol);
    }

    protected ImmutableTerm extractRDFTermTypeTerm(ImmutableTerm rdfTerm, TermFactory termFactory) {
        if (isRDFFunctionalTerm(rdfTerm))
            return ((ImmutableFunctionalTerm)rdfTerm).getTerm(1);
        else if (rdfTerm instanceof RDFConstant)
            return termFactory.getRDFTermTypeConstant(((RDFConstant) rdfTerm).getType());
        else if ((rdfTerm instanceof Constant) && rdfTerm.isNull())
            return termFactory.getNullConstant();
        throw new IllegalArgumentException("Was expecting a isRDFFunctionalTerm or an RDFConstant or NULL");
    }

    protected ImmutableTerm extractLexicalTerm(ImmutableTerm rdfTerm, TermFactory termFactory) {
        if (isRDFFunctionalTerm(rdfTerm))
            return ((ImmutableFunctionalTerm)rdfTerm).getTerm(0);
        else if (rdfTerm instanceof RDFConstant)
            return termFactory.getDBStringConstant(((RDFConstant) rdfTerm).getValue());
        else if ((rdfTerm instanceof Constant) && rdfTerm.isNull())
            return termFactory.getNullConstant();
        throw new IllegalArgumentException("Was expecting a isRDFFunctionalTerm or an RDFConstant or NULL");
    }
}
