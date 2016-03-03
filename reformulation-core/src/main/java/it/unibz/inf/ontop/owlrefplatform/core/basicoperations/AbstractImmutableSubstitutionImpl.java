package it.unibz.inf.ontop.owlrefplatform.core.basicoperations;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;

import java.util.*;

/**
 * Common abstract class for ImmutableSubstitutionImpl and Var2VarSubstitutionImpl
 */
public abstract class AbstractImmutableSubstitutionImpl<T  extends ImmutableTerm> extends LocallyImmutableSubstitutionImpl
        implements ImmutableSubstitution<T> {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

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
            return DATA_FACTORY.getImmutableBooleanExpression((OperationPredicate) functionSymbol,
                    subTermsBuilder.build());
        }
        else {
            return DATA_FACTORY.getImmutableFunctionalTerm(functionSymbol, subTermsBuilder.build());
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

        return DATA_FACTORY.getFunction(functionSymbol, transformedSubTerms);
    }

    @Override
    public ImmutableBooleanExpression applyToBooleanExpression(ImmutableBooleanExpression booleanExpression) {
        return (ImmutableBooleanExpression) apply(booleanExpression);
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
        return DATA_FACTORY.getDataAtom(predicate, argBuilder.build());
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
            return DATA_FACTORY.getDistinctVariableDataAtom(newDataAtom.getPredicate(),
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


        return new ImmutableSubstitutionImpl<>(ImmutableMap.copyOf(substitutionMap));
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
            ImmutableSubstitution<T> unionSubstitution = new ImmutableSubstitutionImpl<>(optionalMap.get());
            return Optional.of(unionSubstitution);
        }
        return Optional.empty();
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> applyToTarget(ImmutableSubstitution<? extends ImmutableTerm>
                                                                          otherSubstitution) {
        ImmutableMap.Builder<Variable, ImmutableTerm> mapBuilder = ImmutableMap.builder();

        ImmutableMap<Variable, ? extends ImmutableTerm> otherSubstitutionMap = otherSubstitution.getImmutableMap();
        for (Map.Entry<Variable, ? extends ImmutableTerm> otherEntry : otherSubstitutionMap.entrySet()) {
            mapBuilder.put(otherEntry.getKey(), apply(otherEntry.getValue()));
        }
        return new ImmutableSubstitutionImpl<>(mapBuilder.build());
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
    public ImmutableSubstitution<T> orientate(ImmutableSet<Variable> variablesToTryToKeep) {
        if (variablesToTryToKeep.isEmpty() || isEmpty()) {
            return this;
        }

        ImmutableMap.Builder<Variable, T> mapBuilder = ImmutableMap.builder();
        for (Variable replacedVariable : getImmutableMap().keySet()) {
            T target = get(replacedVariable);
            if ((target instanceof Variable) && (variablesToTryToKeep.contains(replacedVariable))) {
                Variable targetVariable = (Variable) target;

                /**
                 * TODO: explain
                 */
                if (!variablesToTryToKeep.contains(targetVariable)) {
                    // Inverses the variables
                    // NB: now we know that T extends Variable
                    mapBuilder.put(targetVariable, (T)replacedVariable);
                    continue;
                }
            }
            /**
             * By default, keep the entry
             */
            mapBuilder.put(replacedVariable, target);
        }

        return constructNewSubstitution(mapBuilder.build());
    }

    @Override
    public  Optional<ImmutableBooleanExpression> convertIntoBooleanExpression() {
        return convertIntoBooleanExpression(this);
    }

    protected static Optional<ImmutableBooleanExpression> convertIntoBooleanExpression(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {

        List<ImmutableBooleanExpression> equalities = new ArrayList<>();

        for (Map.Entry<Variable, ? extends ImmutableTerm> entry : substitution.getImmutableMap().entrySet()) {
            equalities.add(DATA_FACTORY.getImmutableBooleanExpression(ExpressionOperation.EQ, entry.getKey(), entry.getValue()));
        }

        switch(equalities.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(equalities.get(0));
            default:
                Iterator<ImmutableBooleanExpression> equalityIterator = equalities.iterator();
                // Non-final
                ImmutableBooleanExpression aggregateExpression = equalityIterator.next();
                while (equalityIterator.hasNext()) {
                    aggregateExpression = DATA_FACTORY.getImmutableBooleanExpression(ExpressionOperation.AND, aggregateExpression,
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

}
