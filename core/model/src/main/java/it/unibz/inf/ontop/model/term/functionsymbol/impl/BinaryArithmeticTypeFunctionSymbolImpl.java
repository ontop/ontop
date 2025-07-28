package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public class BinaryArithmeticTypeFunctionSymbolImpl extends FunctionSymbolImpl{

    private final MetaRDFTermType metaRDFTermType;
    private final TypeFactory typeFactory;

    protected BinaryArithmeticTypeFunctionSymbolImpl(String dbOperationName, DBTermType dbTermType,
                                                     MetaRDFTermType metaRDFType, TypeFactory typeFactory) {
        super("LATELY_OP_TYPE_" + dbOperationName, ImmutableList.of(dbTermType, dbTermType, metaRDFType, metaRDFType));

        this.metaRDFTermType = metaRDFType;
        this.typeFactory = typeFactory;
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(metaRDFTermType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {

        ImmutableList<ImmutableTerm> typeTerms = ImmutableList.of(newTerms.get(2), newTerms.get(3));

        if (typeTerms.stream().allMatch(t -> t instanceof RDFTermTypeConstant)) {
            ImmutableList<RDFTermType> rdfTypeConstants = typeTerms.stream()
                    .map(t -> ((RDFTermTypeConstant) t).getRDFTermType())
                    .collect(ImmutableCollectors.toList());

            if (rdfTypeConstants.stream().anyMatch(t -> t.isA(typeFactory.getAbstractOntopTemporalDatatype()))) {
                throw new UnsupportedOperationException(
                        "Binary arithmetic operations on temporal types are not supported");
            }
            return termFactory.getCommonPropagatedOrSubstitutedNumericType(typeTerms.get(0), typeTerms.get(1));
        }

        // for now no additional logic
        return termFactory.getCommonPropagatedOrSubstitutedNumericType(typeTerms.get(0), typeTerms.get(1));


    }
}
