package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfThenFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public class BinaryArithmeticTermTypeFunctionSymbolImpl extends FunctionSymbolImpl{

    private final MetaRDFTermType metaRDFTermType;
    private final TypeFactory typeFactory;
    private final String dbOperationName;

    protected BinaryArithmeticTermTypeFunctionSymbolImpl(String dbOperationName, DBTermType dbTermType,
                                                         MetaRDFTermType metaRDFType, TypeFactory typeFactory) {
        super("TYPE_BINARY_" + dbOperationName, ImmutableList.of(dbTermType, dbTermType, metaRDFType, metaRDFType));

        this.dbOperationName = dbOperationName;
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
            ImmutableList<RDFTermTypeConstant> rdfTypeConstants = typeTerms.stream()
                    .map(t -> (RDFTermTypeConstant) t)
                    .collect(ImmutableCollectors.toList());

            ImmutableList<RDFTermType> rdfTypes = rdfTypeConstants.stream()
                    .map(RDFTermTypeConstant::getRDFTermType)
                    .collect(ImmutableCollectors.toList());

            if (rdfTypeConstants.stream().allMatch(t -> t.getRDFTermType().isA(typeFactory.getAbstractOntopNumericDatatype()))) {
                return getCommonPropagatedOrSubstitutedType(rdfTypeConstants.stream(), termFactory)
                        .map(t -> (Constant) t)
                        .orElseGet(termFactory::getNullConstant);
            } else {
                return new AllowedOperandTypesCombinations(termFactory, typeFactory).getResultType(dbOperationName, rdfTypes)
                        .orElseGet(termFactory::getNullConstant);
            }
        } else if (typeTerms.stream().anyMatch(t -> t instanceof ImmutableFunctionalTerm)) {

            return typeTerms.stream()
                    .filter(t -> t instanceof ImmutableFunctionalTerm)
                    .map(t -> (ImmutableFunctionalTerm) t)
                    .findAny()
                    .flatMap(functionalTerm ->
                            // tries to lift the DB case of the rdf type term if there is any
                            tryPushingDownFunctionalTerm(functionalTerm, newTerms, termFactory, variableNullability))
                    // tries to lift magic numbers
                    .or(() ->  super.tryToLiftMagicNumbers(newTerms, termFactory, variableNullability, false))
                    .orElseGet(() -> super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability));
        } else {
            return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
        }
    }

    private Optional<ImmutableTerm> tryPushingDownFunctionalTerm(ImmutableFunctionalTerm term, ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        return Optional.of(term)
                .filter(t -> t.getFunctionSymbol() instanceof DBIfThenFunctionSymbol)
                .map(t -> ((DBIfThenFunctionSymbol) t.getFunctionSymbol())
                        .pushDownRegularFunctionalTerm(
                                termFactory.getImmutableFunctionalTerm(this, newTerms),
                                newTerms.indexOf(t),
                                termFactory))
                .map(t -> t.simplify(variableNullability));
    }

    private Optional<RDFTermTypeConstant> getCommonPropagatedOrSubstitutedType(Stream<RDFTermTypeConstant> typeConstantStream, TermFactory termFactory) {
        Optional<ConcreteNumericRDFDatatype> optionalNumericType = typeConstantStream
                .map(RDFTermTypeConstant::getRDFTermType)
                .filter(t -> t instanceof ConcreteNumericRDFDatatype)
                .map(t -> (ConcreteNumericRDFDatatype) t)
                .reduce(ConcreteNumericRDFDatatype::getCommonPropagatedOrSubstitutedType)
                .filter(t -> !t.isAbstract());

        return optionalNumericType
                .map(termFactory::getRDFTermTypeConstant);
    }

    protected static class AllowedOperandTypesCombinations {
        private final Table<RDFTermType,RDFTermType, RDFTermType> additionMap;
        private final Table<RDFTermType,RDFTermType, RDFTermType> subtractionMap;
        private final Table<RDFTermType,RDFTermType, RDFTermType> multiplicationMap;
        private final TermFactory termFactory;
        private final TypeFactory typeFactory;

        public AllowedOperandTypesCombinations(TermFactory termFactory, TypeFactory typeFactory) {
            this.termFactory = termFactory;
            this.typeFactory = typeFactory;
            this.additionMap = createAdditionMap(typeFactory);
            this.subtractionMap = createSubtractionMap(typeFactory);
            this.multiplicationMap = createMultiplicationMap(typeFactory);
        }

        public Optional<ImmutableTerm> getResultType(String operand, ImmutableList<RDFTermType> argumentsTypes) {
            RDFTermType firstType = argumentsTypes.get(0);
            RDFTermType secondType = argumentsTypes.get(1);

            // generalize all numeric types to the abstract numeric type
            if (firstType.isA(typeFactory.getAbstractOntopNumericDatatype())) {
                firstType = typeFactory.getAbstractOntopNumericDatatype();
            }
            if (secondType.isA(typeFactory.getAbstractOntopNumericDatatype())) {
                secondType = typeFactory.getAbstractOntopNumericDatatype();
            }
            switch (operand) {
                case "+": {
                    return Optional.ofNullable(additionMap.get(firstType, secondType))
                            .map(termFactory::getRDFTermTypeConstant);
                }
                case "-": {
                    return Optional.ofNullable(subtractionMap.get(firstType, secondType))
                            .map(termFactory::getRDFTermTypeConstant);
                }
                case "*": {
                    return Optional.ofNullable(multiplicationMap.get(firstType, secondType))
                            .map(termFactory::getRDFTermTypeConstant);
                }
                default:
                    throw new IllegalArgumentException("Unsupported operand: " + operand);
            }
        }

        private static ImmutableTable<RDFTermType, RDFTermType, RDFTermType> createAdditionMap(TypeFactory typeFactory) {
            return new ImmutableTable.Builder<RDFTermType, RDFTermType, RDFTermType>()
                    // numeric + numeric -> numeric
                    .put(typeFactory.getAbstractOntopNumericDatatype(), typeFactory.getAbstractOntopNumericDatatype(),
                            typeFactory.getAbstractOntopNumericDatatype())
                    // dateTime + duration -> dateTime
                    .put(typeFactory.getXsdDatetimeDatatype(), typeFactory.getXsdDurationDatatype(),
                            typeFactory.getXsdDatetimeDatatype())
                    // duration + dateTime -> dateTime
                    .put(typeFactory.getXsdDurationDatatype(), typeFactory.getXsdDatetimeDatatype(),
                            typeFactory.getXsdDatetimeDatatype())
                    // date + duration -> date
                    .put(typeFactory.getXsdDate(), typeFactory.getXsdDurationDatatype(),
                            typeFactory.getXsdDate())
                    // duration + date -> date
                    .put(typeFactory.getXsdDurationDatatype(), typeFactory.getXsdDate(),
                            typeFactory.getXsdDate())
                    // time + dayTimeDuration -> time
                    .put(typeFactory.getXsdTime(), typeFactory.getXsdDayTimeDurationDatatype(),
                            typeFactory.getXsdTime())
                    // dayTimeDuration + time -> time
                    .put(typeFactory.getXsdDayTimeDurationDatatype(), typeFactory.getXsdTime(),
                            typeFactory.getXsdTime())
                    // duration + duration -> duration
                    .put(typeFactory.getXsdDurationDatatype(), typeFactory.getXsdDurationDatatype(),
                            typeFactory.getXsdDurationDatatype())
                    // dayTimeDuration + dayTimeDuration -> dayTimeDuration
                    .put(typeFactory.getXsdDayTimeDurationDatatype(), typeFactory.getXsdDayTimeDurationDatatype(),
                            typeFactory.getXsdDayTimeDurationDatatype())
                    // yearMonthDuration + yearMonthDuration -> yearMonthDuration
                    .put(typeFactory.getXsdYearMonthDurationDatatype(), typeFactory.getXsdYearMonthDurationDatatype(),
                            typeFactory.getXsdYearMonthDurationDatatype())
                    .build();
        }

        private static Table<RDFTermType, RDFTermType, RDFTermType> createSubtractionMap(TypeFactory typeFactory) {
            return new ImmutableTable.Builder<RDFTermType, RDFTermType, RDFTermType>()
                    // numeric - numeric -> numeric
                    .put(typeFactory.getAbstractOntopNumericDatatype(), typeFactory.getAbstractOntopNumericDatatype(),
                            typeFactory.getAbstractOntopNumericDatatype())
                    // dateTime - duration -> dateTime
                    .put(typeFactory.getXsdDatetimeDatatype(), typeFactory.getXsdDurationDatatype(),
                            typeFactory.getXsdDatetimeDatatype())
                    // date - duration -> date
                    .put(typeFactory.getXsdDate(), typeFactory.getXsdDurationDatatype(),
                            typeFactory.getXsdDate())
                    // time - dayTimeDuration -> time
                    .put(typeFactory.getXsdTime(), typeFactory.getXsdDayTimeDurationDatatype(),
                            typeFactory.getXsdTime())
                    // dateTime - dateTime -> duration
                    .put(typeFactory.getXsdDatetimeDatatype(), typeFactory.getXsdDatetimeDatatype(),
                            typeFactory.getXsdDurationDatatype())
                    // date - date -> duration
                    .put(typeFactory.getXsdDate(), typeFactory.getXsdDate(),
                            typeFactory.getXsdDurationDatatype())
                    // time - time -> dayTimeDuration
                    .put(typeFactory.getXsdTime(), typeFactory.getXsdTime(),
                            typeFactory.getXsdDayTimeDurationDatatype())
                    .build();
        }

        private static Table<RDFTermType, RDFTermType, RDFTermType> createMultiplicationMap(TypeFactory typeFactory) {
            return new ImmutableTable.Builder<RDFTermType, RDFTermType, RDFTermType>()
                    // numeric * numeric -> numeric
                    .put(typeFactory.getAbstractOntopNumericDatatype(), typeFactory.getAbstractOntopNumericDatatype(),
                            typeFactory.getAbstractOntopNumericDatatype())
                    // duration * numeric -> duration
                    .put(typeFactory.getXsdDurationDatatype(), typeFactory.getAbstractOntopNumericDatatype(),
                            typeFactory.getXsdDurationDatatype())
                    // numeric * duration -> duration
                    .put(typeFactory.getAbstractOntopNumericDatatype(), typeFactory.getXsdDurationDatatype(),
                            typeFactory.getXsdDurationDatatype())
                    .build();
        }
    }
}
