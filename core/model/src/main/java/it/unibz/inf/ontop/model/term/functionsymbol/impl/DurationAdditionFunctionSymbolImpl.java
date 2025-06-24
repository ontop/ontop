package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.*;

import java.util.Optional;
import java.util.stream.IntStream;

public class DurationAdditionFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {
    private final RDFTermType targetType;
    private final RDFTermType durationType;
    private final TypeFactory typeFactory;

    public DurationAdditionFunctionSymbolImpl(RDFTermType dateOrDatetimeType, RDFTermType durationType, TypeFactory typeFactory) {
        super("SP_DURATION_SUM", "DURATION_SUM", ImmutableList.of(dateOrDatetimeType, durationType));
        this.targetType = dateOrDatetimeType;
        this.durationType = durationType;
        this.typeFactory = typeFactory;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {
        int constantDurationIndex = IntStream.range(0, typeTerms.size())
                .filter(i -> typeTerms.get(i) instanceof RDFTermTypeConstant &&
                        ((RDFTermTypeConstant) typeTerms.get(i)).getRDFTermType().isA(durationType) &&
                        subLexicalTerms.get(i) instanceof Constant)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("At least one argument must be a constant xsd:duration"));

        int otherIndex = (constantDurationIndex == 0) ? 1 : 0;

        if (returnedTypeTerm instanceof RDFTermTypeConstant) {
            TermType termType = (((RDFTermTypeConstant) returnedTypeTerm).getRDFTermType());
            if (termType.isA(typeFactory.getXsdDate())) {
                return termFactory.getDBDurationAddFromDate(
                        subLexicalTerms.get(otherIndex),
                        subLexicalTerms.get(constantDurationIndex));
            }
            else if (termType.isA(typeFactory.getXsdDatetimeDatatype())) {
                return termFactory.getDBDurationAddFromDateTime(
                        subLexicalTerms.get(otherIndex),
                        subLexicalTerms.get(constantDurationIndex));
            } else {
                throw new RuntimeException("The first argument must be either xsd:date or xsd:datetime, but got: " + termType);
            }
        }

        throw new RuntimeException("Logic still not implemented");

    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(targetType);
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(targetType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
