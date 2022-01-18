package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RDFTermFunctionSymbolImpl extends FunctionSymbolImpl implements RDFTermFunctionSymbol {

    protected RDFTermFunctionSymbolImpl(TermType lexicalType, MetaRDFTermType typeTermType) {
        super("RDF", ImmutableList.of(lexicalType, typeTermType));
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
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

        // Only when both are non-nulls (one is not enough, quite likely to be invalid)
        if (newTerms.stream().allMatch(ImmutableTerm::isNull))
            return termFactory.getNullConstant();

        if (newTerms.stream()
                .allMatch(t -> t instanceof NonNullConstant)) {

            DBConstant lexicalConstant = Optional.of(newTerms.get(0))
                    .filter(c -> c instanceof DBConstant)
                    .map(c -> (DBConstant) c)
                    .orElseThrow(() -> new MinorOntopInternalBugException(
                            "The constant for the lexical part was expected to be a DBConstant"));

            RDFTermType rdfTermType = Optional.of(newTerms.get(1))
                    .filter(c -> c instanceof RDFTermTypeConstant)
                    .map(c -> (RDFTermTypeConstant) c)
                    .map(RDFTermTypeConstant::getRDFTermType)
                    .orElseThrow(() -> new MinorOntopInternalBugException(
                            "The second constant argument was expected to be a RDFTermTypeConstant"));

            return rdfTermType.isAbstract()
                    // May still be temporarily abstract during some stages of the mapping processing
                    ? termFactory.getImmutableFunctionalTerm(this, newTerms)
                    : termFactory.getRDFConstant(lexicalConstant.getValue(), rdfTermType);
        }
        else
            // May block invalid cases such as RDF(NULL, IRI)
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
        /*
         * Strict equality between two RDF functional terms -> strict equality between their components
         */
        else if ((otherTerm instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) otherTerm).getFunctionSymbol().equals(this))) {
            ImmutableList<? extends ImmutableTerm> otherSubTerms = ((ImmutableFunctionalTerm) otherTerm).getTerms();
            return termFactory.getConjunction(
                    termFactory.getStrictEquality(terms.get(0), otherSubTerms.get(0)),
                    termFactory.getStrictEquality(terms.get(1), otherSubTerms.get(1)))
                    .evaluate(variableNullability, true);
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
        Optional<ImmutableExpression> optionalExpression = termFactory.getDBIsNotNull(terms.stream()
                .filter(t -> t.isNullable(nullableVariables)));

        return optionalExpression
                .map(e -> e.evaluate(variableNullability, true))
                .orElseGet(IncrementalEvaluation::declareIsTrue);
    }

    /**
     * Looks at the lexical term for provenance variables
     */
    @Override
    public Stream<Variable> proposeProvenanceVariables(ImmutableList<? extends ImmutableTerm> terms) {
        ImmutableTerm lexicalTerm = terms.get(0);
        if (lexicalTerm instanceof Variable)
            return Stream.of((Variable) lexicalTerm);

        return Optional.of(lexicalTerm)
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .map(ImmutableFunctionalTerm::proposeProvenanceVariables)
                .orElseGet(Stream::empty);
    }

    /**
     * TODO: stop overriding (only use the top implementation)
     */
    @Override
    protected boolean canBeSafelyDecomposedIntoConjunction(ImmutableList<? extends ImmutableTerm> terms,
                                                           VariableNullability variableNullability,
                                                           ImmutableList<? extends ImmutableTerm> otherTerms) {
        return true;
    }

    /**
     * Overridden because RDF(...) officially "tolerates" NULLs
     * (so as to complain when only one argument is NULL, which should never happen)
     */
    @Override
    public FunctionalTermSimplification simplifyAsGuaranteedToBeNonNull(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory) {
        ImmutableMap<Integer, FunctionalTermSimplification> subTermSimplifications = IntStream.range(0, terms.size())
                .filter(i -> terms.get(i) instanceof ImmutableFunctionalTerm)
                .boxed()
                .collect(ImmutableCollectors.toMap(
                        i -> i,
                        // Recursive
                        i -> ((ImmutableFunctionalTerm) terms.get(i)).simplifyAsGuaranteedToBeNonNull()));

        ImmutableList<ImmutableTerm> newSubTerms = IntStream.range(0, terms.size())
                .mapToObj(i -> Optional.ofNullable(subTermSimplifications.get(i))
                        .map(FunctionalTermSimplification::getSimplifiedTerm)
                        .orElseGet(() -> terms.get(i)))
                .collect(ImmutableCollectors.toList());

        ImmutableFunctionalTerm simplifiedTerm = termFactory.getImmutableFunctionalTerm(this, newSubTerms);

        ImmutableSet<Variable> simplifiableVariables = Stream.concat(
                subTermSimplifications.values().stream()
                        .flatMap(s -> s.getSimplifiableVariables().stream()),
                terms.stream()
                        .filter(t -> t instanceof Variable)
                        .map(v -> (Variable) v))
                .collect(ImmutableCollectors.toSet());

        return FunctionalTermSimplification.create(simplifiedTerm, simplifiableVariables);
    }
}
