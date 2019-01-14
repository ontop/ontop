package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;

public class EncodeForUriAbstractUnaryStringSPARQLFunctionSymbolImpl extends AbstractUnaryStringSPARQLFunctionSymbol {
    private final RDFDatatype xsdStringDatatype;

    protected EncodeForUriAbstractUnaryStringSPARQLFunctionSymbolImpl(RDFDatatype xsdStringDatatype) {
        super("SP_ENCODE_FOR_URI", XPathFunction.ENCODE_FOR_URI, xsdStringDatatype);
        this.xsdStringDatatype = xsdStringDatatype;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms,
                                               TermFactory termFactory) {
        return termFactory.getR2RMLIRISafeEncodeFunctionalTerm(subLexicalTerms.get(0));
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(xsdStringDatatype);
    }

    @Override
    protected boolean isAlwaysInjective() {
        return true;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

}
