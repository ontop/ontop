package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
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

    protected BinaryArithmeticTermTypeFunctionSymbolImpl(String dbOperationName, DBTermType dbTermType,
                                                         MetaRDFTermType metaRDFType, TypeFactory typeFactory) {
        super("TYPE_BINARY_" + dbOperationName, ImmutableList.of(dbTermType, dbTermType, metaRDFType, metaRDFType));

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

            if (rdfTypeConstants.stream().allMatch(t -> t.getRDFTermType().isA(typeFactory.getAbstractOntopNumericDatatype()))) {
                return getCommonPropagatedOrSubstitutedType(rdfTypeConstants.stream(), termFactory)
                        .map(t -> (Constant) t)
                        .orElseGet(termFactory::getNullConstant);
            } else {
                return termFactory.getNullConstant();
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
}
