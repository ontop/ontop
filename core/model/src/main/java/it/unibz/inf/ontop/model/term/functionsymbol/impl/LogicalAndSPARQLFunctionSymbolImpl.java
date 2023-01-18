package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;

import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

public class LogicalAndSPARQLFunctionSymbolImpl extends AbstractBinaryBooleanOperatorSPARQLFunctionSymbol {

    protected LogicalAndSPARQLFunctionSymbolImpl(RDFDatatype xsdBooleanType) {
        super("SP_AND", SPARQL.LOGICAL_AND, xsdBooleanType);
    }

    @Override
    protected ImmutableExpression computeExpression(ImmutableList<ImmutableExpression> subExpressions,
                                                    TermFactory termFactory) {
        return termFactory.getConjunction(subExpressions);
    }
}
