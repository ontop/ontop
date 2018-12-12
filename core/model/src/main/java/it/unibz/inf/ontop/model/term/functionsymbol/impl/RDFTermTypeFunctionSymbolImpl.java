package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;

public class RDFTermTypeFunctionSymbolImpl extends FunctionSymbolImpl implements RDFTermTypeFunctionSymbol {


    private final MetaRDFTermType metaType;
    private final TypeConstantDictionary dictionary;
    private final ImmutableMap<DBConstant, RDFTermTypeConstant> conversionMap;

    protected RDFTermTypeFunctionSymbolImpl(TypeFactory typeFactory,
                                            TypeConstantDictionary dictionary,
                                            ImmutableMap<DBConstant, RDFTermTypeConstant> conversionMap) {
        super("RDF_TYPE" + extractConversionMapString(conversionMap),
                ImmutableList.of(typeFactory.getDBTypeFactory().getDBBooleanType()));
        metaType = typeFactory.getMetaRDFTermType();
        this.dictionary = dictionary;
        this.conversionMap = conversionMap;
    }

    private static String extractConversionMapString(ImmutableMap<DBConstant, RDFTermTypeConstant> conversionMap) {
        return conversionMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> e.getKey().getValue(),
                        Map.Entry::getValue))
                .toString()
                .replace(" ", "");
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

    @Override
    public TypeConstantDictionary getDictionary() {
        return dictionary;
    }
}
