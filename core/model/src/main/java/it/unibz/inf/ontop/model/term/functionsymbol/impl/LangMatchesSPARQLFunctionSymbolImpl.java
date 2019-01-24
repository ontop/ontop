package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

public class LangMatchesSPARQLFunctionSymbolImpl extends StringBooleanBinarySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdStringType;

    protected LangMatchesSPARQLFunctionSymbolImpl(RDFDatatype xsdStringType, RDFDatatype xsdBooleanType) {
        super("SP_LANG_MATCHES", SPARQL.LANG_MATCHES, xsdStringType, xsdBooleanType);
        this.xsdStringType = xsdStringType;
    }

    @Override
    protected ImmutableTerm computeDBBooleanTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                                 ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return termFactory.getLexicalLangMatches(subLexicalTerms.get(0), subLexicalTerms.get(1));
    }

    /**
     * In theory, both should be "simple" XSD.STRING (not langString).
     * However, for the moment, we tolerate
     * TODO: enforce this restriction? This would require to create a novel function symbol IS_EXACTY_A .
     */
    @Override
    protected ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms,
                                                                    TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getConjunction(
                    termFactory.getIsAExpression(typeTerms.get(0), xsdStringType),
                    termFactory.getIsAExpression(typeTerms.get(1), xsdStringType))
                .evaluate(variableNullability);
    }

    /**
     * Could be allowed in the future
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
