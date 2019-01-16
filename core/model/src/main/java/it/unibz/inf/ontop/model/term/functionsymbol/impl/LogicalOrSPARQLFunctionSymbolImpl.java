package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

public class LogicalOrSPARQLFunctionSymbolImpl extends AbstractBinaryBooleanOperatorSPARQLFunctionSymbol {

    protected LogicalOrSPARQLFunctionSymbolImpl(RDFDatatype xsdBooleanType) {
        super("SP_OR", SPARQL.LOGICAL_OR, xsdBooleanType);
    }

    @Override
    protected ImmutableExpression computeExpression(ImmutableList<ImmutableExpression> subExpressions,
                                                    TermFactory termFactory) {
        return termFactory.getDisjunction(subExpressions);
    }
}
