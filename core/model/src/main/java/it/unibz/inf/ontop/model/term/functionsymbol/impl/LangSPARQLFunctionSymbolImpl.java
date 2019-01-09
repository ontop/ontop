package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

public class LangSPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdStringDatatype;

    protected LangSPARQLFunctionSymbolImpl(RDFDatatype abstractRDFDatatype, RDFDatatype xsdStringDatatype) {
        super("SP_LANG", "lang", ImmutableList.of(abstractRDFDatatype));
        this.xsdStringDatatype = xsdStringDatatype;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return termFactory.getLangTypeFunctionalTerm(typeTerms.get(0))
                .simplify(false);
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(xsdStringDatatype);
    }

    @Override
    protected boolean isAlwaysInjective() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(xsdStringDatatype));
    }

    /**
     * Could be supported in the future
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
