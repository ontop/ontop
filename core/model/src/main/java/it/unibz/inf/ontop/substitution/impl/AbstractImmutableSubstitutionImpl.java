package it.unibz.inf.ontop.substitution.impl;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

/**
 * Common abstract class for ImmutableSubstitutionImpl and Var2VarSubstitutionImpl
 */
public abstract class AbstractImmutableSubstitutionImpl<T  extends ImmutableTerm> extends LocallyImmutableSubstitutionImpl
        implements ImmutableSubstitution<T> {

    private final AtomFactory atomFactory;
    private final TermFactory termFactory;

    protected AbstractImmutableSubstitutionImpl(AtomFactory atomFactory, TermFactory termFactory) {
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
    }

    @Override
    public ImmutableTerm apply(ImmutableTerm term) {
        if (term instanceof Constant) {
            return term;
        }
        else if (term instanceof Variable) {
            return applyToVariable((Variable) term);
        }
        else if (term instanceof ImmutableFunctionalTerm) {
            return applyToFunctionalTerm((ImmutableFunctionalTerm) term);
        }
        else {
            throw new IllegalArgumentException("Unexpected kind of term: " + term.getClass());
        }
    }

    @Override
    public ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm) {
        if (isEmpty())
            return functionalTerm;

        ImmutableList.Builder<ImmutableTerm> subTermsBuilder = ImmutableList.builder();

        for (ImmutableTerm subTerm : functionalTerm.getArguments()) {
            subTermsBuilder.add(apply(subTerm));
        }
        Predicate functionSymbol = functionalTerm.getFunctionSymbol();

        /**
         * Distinguishes the BooleanExpression from the other functional terms.
         */
        if (functionSymbol instanceof OperationPredicate) {
            return termFactory.getImmutableExpression((OperationPredicate) functionSymbol,
                    subTermsBuilder.build());
        }
        else {
            return termFactory.getImmutableFunctionalTerm(functionSymbol, subTermsBuilder.build());
        }
    }

    @Override
    public Function applyToMutableFunctionalTerm(Function mutableFunctionalTerm) {
        if (isEmpty())
            return mutableFunctionalTerm;

        List<Term> transformedSubTerms = new ArrayList<>();

        for (Term subTerm : mutableFunctionalTerm.getTerms()) {
            transformedSubTerms.add(applyToMutableTerm(subTerm));
        }
        Predicate functionSymbol = mutableFunctionalTerm.getFunctionSymbol();

        return termFactory.getFunction(functionSymbol, transformedSubTerms);
    }

    @Override
    public ImmutableExpression applyToBooleanExpression(ImmutableExpression booleanExpression) {
        return (ImmutableExpression) apply(booleanExpression);
    }

    @Override
    public DataAtom applyToDataAtom(DataAtom atom) throws ConversionException {
        ImmutableFunctionalTerm newFunctionalTerm = applyToFunctionalTerm(atom);

        if (newFunctionalTerm instanceof DataAtom)
            return (DataAtom) newFunctionalTerm;

        AtomPredicate predicate = (AtomPredicate) newFunctionalTerm.getFunctionSymbol();

        /**
         * Casts all the sub-terms into VariableOrGroundTerm
         *
         * Throws a ConversionException if this cast is impossible.
         */
        ImmutableList.Builder<VariableOrGroundTerm> argBuilder = ImmutableList.builder();
        for (ImmutableTerm subTerm : newFunctionalTerm.getArguments()) {
            if (!(subTerm instanceof VariableOrGroundTerm))
                throw new ConversionException("The sub-term: " + subTerm + " is not a VariableOrGroundTerm");
            argBuilder.add((VariableOrGroundTerm)subTerm);

        }
        return atomFactory.getDataAtom(predicate, argBuilder.build());
    }

    @Override
    public DistinctVariableDataAtom applyToDistinctVariableDataAtom(DistinctVariableDataAtom dataAtom)
            throws ConversionException {
        DataAtom newDataAtom = applyToDataAtom(dataAtom);

        if (newDataAtom instanceof DistinctVariableDataAtom) {
            return (DistinctVariableDataAtom) newDataAtom;
        }

        /**
         * Checks if new data atom can be converted into a DistinctVariableDataAtom
         */
        if (newDataAtom.getArguments().size() == newDataAtom.getVariables().size()) {
            return atomFactory.getDistinctVariableDataAtom(newDataAtom.getPredicate(),
                    (ImmutableList<Variable>)newDataAtom.getArguments());
        }
        else {
            throw new ConversionException("The substitution has transformed a DistinctVariableDataAtom into" +
                    "a non-DistinctVariableDataAtom: " + newDataAtom);
        }
    }

    @Override
    public DistinctVariableOnlyDataAtom applyToDistinctVariableOnlyDataAtom(DistinctVariableOnlyDataAtom dataAtom)
            throws ConversionException {
        DistinctVariableDataAtom newDataAtom = applyToDistinctVariableDataAtom(dataAtom);

        if (newDataAtom instanceof DistinctVariableOnlyDataAtom) {
            return (DistinctVariableOnlyDataAtom) newDataAtom;
        }
        else {
            throw new ConversionException("The substitution has transformed a DistinctVariableOnlyDataAtom into" +
                    "a DistinctVariableDataAtom containing GroundTerm-s: " + newDataAtom);
        }
    }


    /**
     *" "this o g"
     *
     * Equivalent to the function x -> this.apply(g.apply(x))
     *
     * Follows the formal definition of a the composition of two substitutions.
     *
     */
    @Override
    public ImmutableSubstitution<ImmutableTerm> composeWith(ImmutableSubstitution<? extends ImmutableTerm> g) {
        if (isEmpty()) {
            return (ImmutableSubstitution<ImmutableTerm>)g;
        }
        if (g.isEmpty()) {
            return (ImmutableSubstitution<ImmutableTerm>)this;
        }

        Map<Variable, ImmutableTerm> substitutionMap = new HashMap<>();

        /**
         * For all variables in the domain of g
         */

        for (Map.Entry<Variable, ? extends ImmutableTerm> gEntry :  g.getImmutableMap().entrySet()) {
            substitutionMap.put(gEntry.getKey(), apply(gEntry.getValue()));
        }

        /**
         * For the other variables (in the local domain but not in g)
         */
        for (Map.Entry<Variable, ? extends ImmutableTerm> localEntry :  getImmutableMap().entrySet()) {
            Variable localVariable = localEntry.getKey();

            if (substitutionMap.containsKey(localVariable))
                continue;

            substitutionMap.put(localVariable, localEntry.getValue());
        }

        return new ImmutableSubstitutionImpl<>(
                substitutionMap.entrySet().stream()
                        // Clean out entries like t/t
                        .filter(entry -> !entry.getKey().equals(entry.getValue()))
                        .collect(ImmutableCollectors.toMap()), atomFactory, termFactory);
    }

    /**
     * In case some sub-classes wants to add a new unionXX method returning an optional in their own type.
     *
     * TODO: explain
     */
    protected Optional<ImmutableMap<Variable, T>> computeUnionMap(ImmutableSubstitution<T> otherSubstitution) {
        ImmutableMap.Builder<Variable, T> mapBuilder = ImmutableMap.builder();
        mapBuilder.putAll(getImmutableMap());

        ImmutableMap<Variable, T> otherMap = otherSubstitution.getImmutableMap();
        for(Variable otherVariable : otherMap.keySet()) {

            T otherTerm = otherMap.get(otherVariable);

            /**
             * TODO: explain
             */
            if (isDefining(otherVariable) && (!get(otherVariable).equals(otherTerm))) {
                return Optional.empty();
            }

            mapBuilder.put(otherVariable, otherTerm);
        }

        return Optional.of(mapBuilder.build());
    }

    @Override
    public Optional<ImmutableSubstitution<T>> union(ImmutableSubstitution<T> otherSubstitution) {
        if (otherSubstitution.isEmpty())
            return Optional.of((ImmutableSubstitution<T>)this);
        else if(isEmpty())
            return Optional.of(otherSubstitution);

        Optional<ImmutableMap<Variable, T>> optionalMap = computeUnionMap(otherSubstitution);
        if (optionalMap.isPresent()) {
            ImmutableSubstitution<T> unionSubstitution = new ImmutableSubstitutionImpl<>(optionalMap.get(),
                    atomFactory, termFactory);
            return Optional.of(unionSubstitution);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ImmutableSubstitution<? extends ImmutableTerm>> unionHeterogeneous(
            ImmutableSubstitution<? extends ImmutableTerm> otherSubstitution) {
        if (otherSubstitution.isEmpty())
            return Optional.of((ImmutableSubstitution<? extends ImmutableTerm>)this);
        else if(isEmpty())
            return Optional.of(otherSubstitution);

        ImmutableMap<Variable, T> localMap = getImmutableMap();
        ImmutableSet<? extends Map.Entry<Variable, ? extends ImmutableTerm>> otherEntrySet = otherSubstitution.getImmutableMap().entrySet();

        /**
         * Checks for multiple entries of the same variable
         */
        if (otherEntrySet.stream()
                .filter(e -> localMap.containsKey(e.getKey()))
                .anyMatch(e -> !localMap.get(e.getKey()).equals(e.getValue()))) {
            return Optional.empty();
        }
        else {
            ImmutableMap<Variable, ? extends ImmutableTerm> newMap = Stream.concat(localMap.entrySet().stream(),
                    otherEntrySet.stream())
                    .distinct()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue));
            return Optional.of(new ImmutableSubstitutionImpl<>(newMap, atomFactory, termFactory));
        }
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> applyToTarget(ImmutableSubstitution<? extends ImmutableTerm>
                                                                          otherSubstitution) {
        ImmutableMap.Builder<Variable, ImmutableTerm> mapBuilder = ImmutableMap.builder();

        ImmutableMap<Variable, ? extends ImmutableTerm> otherSubstitutionMap = otherSubstitution.getImmutableMap();
        for (Map.Entry<Variable, ? extends ImmutableTerm> otherEntry : otherSubstitutionMap.entrySet()) {
            mapBuilder.put(otherEntry.getKey(), apply(otherEntry.getValue()));
        }
        return new ImmutableSubstitutionImpl<>(mapBuilder.build(), atomFactory, termFactory);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ImmutableSubstitution) {
            return getImmutableMap().equals(((ImmutableSubstitution) other).getImmutableMap());
        }
        return false;
    }

    protected abstract ImmutableSubstitution<T> constructNewSubstitution(ImmutableMap<Variable, T> map);

    @Override
    public ImmutableSubstitution<T> orientate(ImmutableList<Variable> priorityVariables) {
        if (priorityVariables.isEmpty() || isEmpty()) {
            return this;
        }

        ImmutableMap<Variable, T> localMap = getImmutableMap();
        ImmutableSet<Variable> domain = getDomain();

        if (localMap.values().stream()
                .flatMap(ImmutableTerm::getVariableStream)
                .anyMatch(domain::contains)) {
            throw new UnsupportedOperationException("The orientate() method requires the domain and the range to be disjoint");
        }

        ImmutableMap<Variable, Variable> renamingMap = localMap.entrySet().stream()
                // Will produce some results only if T is compatible with Variable
                .filter(e -> e.getValue() instanceof Variable)
                .filter(e -> {
                    int replacedVariableIndex = priorityVariables.indexOf(e.getKey());
                    int targetVariableIndex = priorityVariables.indexOf(e.getValue());
                    return replacedVariableIndex >= 0 && ((targetVariableIndex < 0)
                            || (replacedVariableIndex < targetVariableIndex));
                })
                .collect(ImmutableCollectors.toMap(
                        e -> (Variable) e.getValue(),
                        Map.Entry::getKey,
                        (v1, v2) -> priorityVariables.indexOf(v1) <= priorityVariables.indexOf(v2) ? v1 : v2
                ));



        /**
         * Applies the renaming
         */
        if (renamingMap.isEmpty()) {
            return this;
        }
        else {
            Var2VarSubstitution renamingSubstitution = new Var2VarSubstitutionImpl(renamingMap, getAtomFactory(),
                    getTermFactory());

            ImmutableMap<Variable, T> orientedMap = Stream.concat(
                    localMap.entrySet().stream()
                            /**
                             * Removes entries that will be reversed
                             */
                            .filter(e -> !Optional.ofNullable(renamingMap.get(e.getValue()))
                                    .filter(newValue -> newValue.equals(e.getKey()))
                                    .isPresent()),
                    renamingMap.entrySet().stream()
                            .map(e -> (Map.Entry<Variable, T>) e))
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> renamingSubstitution.applyToTerm(e.getValue())
                    ));

            return constructNewSubstitution(orientedMap);
        }
    }

    @Override
    public  Optional<ImmutableExpression> convertIntoBooleanExpression() {
        return convertIntoBooleanExpression(this);
    }

    @Override
    public ImmutableSubstitution<T> reduceDomainToIntersectionWith(ImmutableSet<Variable> restrictingDomain) {
        return new ImmutableSubstitutionImpl<>(
                this.getImmutableMap().entrySet().stream()
                        .filter(e -> restrictingDomain.contains(e.getKey()))
                        .collect(ImmutableCollectors.toMap()),
                atomFactory, termFactory
        );
    }

    protected Optional<ImmutableExpression> convertIntoBooleanExpression(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {

        List<ImmutableExpression> equalities = new ArrayList<>();

        for (Map.Entry<Variable, ? extends ImmutableTerm> entry : substitution.getImmutableMap().entrySet()) {
            equalities.add(termFactory.getImmutableExpression(ExpressionOperation.EQ, entry.getKey(), entry.getValue()));
        }

        switch(equalities.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(equalities.get(0));
            default:
                Iterator<ImmutableExpression> equalityIterator = equalities.iterator();
                // Non-final
                ImmutableExpression aggregateExpression = equalityIterator.next();
                while (equalityIterator.hasNext()) {
                    aggregateExpression = termFactory.getImmutableExpression(ExpressionOperation.AND, aggregateExpression,
                            equalityIterator.next());
                }
                return Optional.of(aggregateExpression);
        }
    }


    /**
     * For backward compatibility with mutable terms (used in CQIE).
     * TO BE REMOVED with the support for CQIE
     */
    private Term applyToMutableTerm(Term term) {
        if (term instanceof Constant) {
            return term;
        }
        else if (term instanceof Variable) {
            return applyToVariable((Variable) term);
        }
        else if (term instanceof Function) {
            return applyToMutableFunctionalTerm((Function)term);
        }
        else {
            throw new IllegalArgumentException("Unexpected kind of term: " + term.getClass());
        }
    }

    protected AtomFactory getAtomFactory() {
        return atomFactory;
    }

    protected TermFactory getTermFactory() {
        return termFactory;
    }

}
