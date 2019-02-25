package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

public class RDFTermFunctionSymbolImpl extends FunctionSymbolImpl implements RDFTermFunctionSymbol {

    protected RDFTermFunctionSymbolImpl(TermType lexicalType, MetaRDFTermType typeTermType) {
        super("RDF", ImmutableList.of(lexicalType, typeTermType));
    }

    @Override
    protected boolean isAlwaysInjective() {
        return true;
    }

    /**
     * TODO: implement seriously
     */
    @Override
    public FunctionalTermNullability evaluateNullability(ImmutableList<? extends NonFunctionalTerm> arguments,
                                                         VariableNullability childNullability) {
        return super.evaluateNullability(arguments, childNullability);
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        // TODO: complain if one is null and the one is not nullable
        if (terms.stream()
                .filter(t -> t instanceof Constant)
                .anyMatch(c -> ((Constant) c).isNull()))
            return Optional.empty();

        if (terms.size() != 2)
            throw new IllegalArgumentException("Wrong arity");

        return Optional.of(terms.get(1))
                .filter(t -> t instanceof RDFTermTypeConstant)
                .map(t -> (RDFTermTypeConstant) t)
                .map(RDFTermTypeConstant::getRDFTermType)
                .map(TermTypeInference::declareTermType);
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {

        if (newTerms.stream().allMatch(ImmutableTerm::isNull))
            return termFactory.getNullConstant();

        // Only one is null, could be invalid so blocks the simplification
        if (newTerms.stream().anyMatch(ImmutableTerm::isNull))
            return termFactory.getImmutableFunctionalTerm(this, newTerms);

        if (newTerms.stream()
                .allMatch(t -> t instanceof DBConstant)) {

            DBConstant lexicalConstant = (DBConstant) newTerms.get(0);

            RDFTermType rdfTermType = Optional.of(newTerms.get(1))
                    .filter(c -> c instanceof RDFTermTypeConstant)
                    .map(c -> (RDFTermTypeConstant) c)
                    .map(RDFTermTypeConstant::getRDFTermType)
                    .orElseThrow(() -> new MinorOntopInternalBugException(
                            "The second constant argument was expected to be a RDFTermTypeConstant"));

            return termFactory.getRDFConstant(lexicalConstant.getValue(), rdfTermType);
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    /**
     * E.g. RDF(NULL, IRI) is invalid and therefore does not simplify itself to NULL.
     */
    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    public IncrementalEvaluation evaluateStrictEq(ImmutableList<? extends ImmutableTerm> terms, ImmutableTerm otherTerm,
                                                  TermFactory termFactory, VariableNullability variableNullability) {
        if (otherTerm instanceof RDFConstant) {
            RDFConstant otherConstant = (RDFConstant) otherTerm;

            ImmutableExpression conjunction = termFactory.getConjunction(
                    termFactory.getStrictEquality(terms.get(0), termFactory.getDBStringConstant(otherConstant.getValue())),
                    termFactory.getStrictEquality(terms.get(1), termFactory.getRDFTermTypeConstant(otherConstant.getType())));

            return conjunction.evaluate(variableNullability, true);
        }
        return super.evaluateStrictEq(terms, otherTerm, termFactory, variableNullability);
    }

    /**
     * Overridden because, it officially "tolerates" NULLs, due to the requirement that either its arguments
     * are both null or both non-null.
     *
     * Nevertheless, the "spirit" of the decomposition is the same.
     */
    @Override
    public IncrementalEvaluation evaluateIsNotNull(ImmutableList<? extends ImmutableTerm> terms,
                                                   TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableSet<Variable> nullableVariables = variableNullability.getNullableVariables();
        Optional<ImmutableExpression> optionalExpression = termFactory.getConjunction(terms.stream()
                .filter(t -> (t.isNullable(nullableVariables)))
                .map(termFactory::getDBIsNotNull));

        return optionalExpression
                .map(e -> e.evaluate(variableNullability, true))
                .orElseGet(IncrementalEvaluation::declareIsTrue);
    }
}
