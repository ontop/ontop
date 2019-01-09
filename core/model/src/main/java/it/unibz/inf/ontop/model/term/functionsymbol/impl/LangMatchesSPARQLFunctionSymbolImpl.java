package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

public class LangMatchesSPARQLFunctionSymbolImpl extends StringBooleanBinarySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdStringType;

    protected LangMatchesSPARQLFunctionSymbolImpl(RDFDatatype xsdStringType, RDFDatatype xsdBooleanType) {
        super("SP_LANG_MATCHES", SPARQL.LANG_MATCHES, xsdStringType, xsdBooleanType);
        this.xsdStringType = xsdStringType;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getDBCastFunctionalTerm(
                dbTypeFactory.getDBBooleanType(),
                dbTypeFactory.getDBStringType(),
                termFactory.getLexicalLangMatches(subLexicalTerms.get(0), subLexicalTerms.get(1)));
    }

    /**
     * Both must be "simple" XSD.STRING (not langString).
     */
    @Override
    protected ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> typeTerms,
                                                                    TermFactory termFactory, VariableNullability variableNullability) {
        RDFTermTypeConstant xsdStringTypeConstant = termFactory.getRDFTermTypeConstant(xsdStringType);

        return termFactory.getConjunction(
                    termFactory.getStrictEquality(typeTerms.get(0), xsdStringTypeConstant),
                    termFactory.getStrictEquality(typeTerms.get(1), xsdStringTypeConstant))
                .evaluate(termFactory, variableNullability);
    }

    /**
     * Could be allowed in the future
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
