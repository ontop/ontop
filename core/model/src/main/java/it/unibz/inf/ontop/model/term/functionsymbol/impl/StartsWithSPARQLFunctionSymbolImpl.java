package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;

public class StartsWithSPARQLFunctionSymbolImpl extends StringBooleanBinarySPARQLFunctionSymbolImpl {

    protected StartsWithSPARQLFunctionSymbolImpl(RDFDatatype xsdStringType, RDFDatatype xsdBooleanType) {
        super("SP_STARTS_WITH", XPathFunction.STARTS_WITH, xsdStringType, xsdBooleanType);
    }

    @Override
    protected ImmutableTerm computeDBBooleanTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                                 ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return termFactory.getDBStartsWith(subLexicalTerms);
    }

    /**
     * Could be allowed in the future
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
