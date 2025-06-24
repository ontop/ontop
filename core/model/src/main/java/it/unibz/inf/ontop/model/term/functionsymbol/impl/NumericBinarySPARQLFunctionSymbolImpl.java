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


public class NumericBinarySPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    protected NumericBinarySPARQLFunctionSymbolImpl(String functionSymbolName, String officialName,
                                                    RDFDatatype abstractNumericType) {
        super(functionSymbolName, officialName, ImmutableList.of(abstractNumericType, abstractNumericType));
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        TypeFactory typeFactory = termFactory.getTypeFactory();
        if (typeTerms.stream().allMatch(t -> t instanceof RDFTermTypeConstant)){
            ImmutableList<RDFTermType> typeConstants = typeTerms.stream()
                    .map(t -> ((RDFTermTypeConstant) t).getRDFTermType())
                    .collect(ImmutableList.toImmutableList());
            boolean areAllTemporal = typeConstants.stream()
                    .allMatch(c -> c.isA(typeFactory.getAbstractOntopTemporalDatatype()));

            if (areAllTemporal) {
                if (typeConstants.stream().noneMatch(t -> t.isA(typeFactory.getXsdDurationDatatype()))) {
                    throw new UnsupportedOperationException("At least one of the types must be xsd:duration");
                } else {
                    RDFTermType temporalType = termFactory.getRDFTermTypeConstant(typeConstants.stream()
                            .filter(t -> !t.isA(typeFactory.getXsdDurationDatatype()))
                            .findAny().get()).getRDFTermType();

                    if (!temporalType.isA(typeFactory.getXsdDate()) &&
                        !temporalType.isA(typeFactory.getXsdDatetimeDatatype())) {
                        throw new UnsupportedOperationException("For now either xsd:date or xsd:datetime is expected as the other type");
                    }
                    return termFactory.getRDFTermTypeConstant(temporalType);
                }
            }
        }


        return termFactory.getCommonPropagatedOrSubstitutedNumericType(typeTerms.get(0), typeTerms.get(1));
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                               ImmutableTerm returnedRDFTypeTerm) {
        TypeFactory typeFactory = termFactory.getTypeFactory();

        if ((returnedRDFTypeTerm instanceof RDFTermTypeConstant) &&
                ((RDFTermTypeConstant) returnedRDFTypeTerm).getRDFTermType().isA(termFactory.getTypeFactory().getAbstractOntopTemporalDatatype())) {
            RDFTermType rdfTermType = ((RDFTermTypeConstant) returnedRDFTypeTerm).getRDFTermType();

            int constantDurationIndex = IntStream.range(0, typeTerms.size())
                    .filter(i -> typeTerms.get(i) instanceof RDFTermTypeConstant &&
                            ((RDFTermTypeConstant) typeTerms.get(i)).getRDFTermType().isA(typeFactory.getXsdDurationDatatype()) &&
                            subLexicalTerms.get(i) instanceof Constant)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("At least one argument must be a constant xsd:duration"));

            int otherIndex = (constantDurationIndex == 0) ? 1 : 0;
            if (rdfTermType.isA(typeFactory.getXsdDate())) {
                return termFactory.getConversionFromRDFLexical2DB(
                        termFactory.getDBDurationAddFromDate(
                            subLexicalTerms.get(otherIndex),
                            subLexicalTerms.get(constantDurationIndex)),
                        rdfTermType);
            } else if (rdfTermType.isA(typeFactory.getXsdDatetimeDatatype())) {
                return termFactory.getConversionFromRDFLexical2DB(
                        termFactory.getDBDurationAddFromDateTime(
                            subLexicalTerms.get(otherIndex),
                            subLexicalTerms.get(constantDurationIndex)),
                        rdfTermType);
            } else {
                throw new RuntimeException("The first argument must be either xsd:date or xsd:datetime, but got: " + rdfTermType);
            }
        }

        return termFactory.getBinaryNumericLexicalFunctionalTerm(getOfficialName(),
                subLexicalTerms.get(0),
                subLexicalTerms.get(1),
                returnedRDFTypeTerm);

    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * Too complex logic so not infer at this level (but after simplification into DB functional terms)
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
