package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;


/**
 * SPARQL STR. Does not tolerate blank nodes as arguments.
 */
public class StrSPARQLFunctionSymbolImpl extends AbstractStrSPARQLFunctionSymbolImpl {

    private final ObjectRDFType bnodeType;

    protected StrSPARQLFunctionSymbolImpl(RDFTermType abstractRDFType, RDFDatatype xsdStringType, ObjectRDFType bnodeType) {
        super("SP_STR", SPARQL.STR, abstractRDFType, xsdStringType);
        this.bnodeType = bnodeType;
    }

    /**
     * Excludes blank nodes
     */
    @Override
    protected ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms,
                                                                    TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm typeTerm = typeTerms.get(0);

        return termFactory.getDBNot(termFactory.getIsAExpression(typeTerm, bnodeType))
                .evaluate(variableNullability);
    }
}
