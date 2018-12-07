package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.Optional;

public class RDFTermTypeFunctionSymbolImpl extends FunctionSymbolImpl implements RDFTermTypeFunctionSymbol {


    private final MetaRDFTermType metaType;
    private final ImmutableMap<DBConstant, RDFTermTypeConstant> conversionMap;

    protected RDFTermTypeFunctionSymbolImpl(TypeFactory typeFactory,
                                            ImmutableMap<DBConstant, RDFTermTypeConstant> conversionMap) {
        super("rdfTypeConversion", ImmutableList.of(typeFactory.getDBTypeFactory().getDBBooleanType()));
        metaType = typeFactory.getMetaRDFTermType();
        this.conversionMap = conversionMap;
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return true;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(metaType));
    }

    @Override
    public Optional<TermTypeInference> inferAndValidateType(ImmutableList<? extends ImmutableTerm> terms) throws FatalTypingException {
        validateSubTermTypes(terms);
        return inferType(terms);
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
        ImmutableTerm term = newTerms.get(0);
        if (term instanceof DBConstant) {
            return conversionMap.get(term);
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, term);
    }

    @Override
    public boolean canBePostProcessed() {
        return true;
    }

    @Override
    public ImmutableMap<DBConstant, RDFTermTypeConstant> getConversionMap() {
        return conversionMap;
    }
}
