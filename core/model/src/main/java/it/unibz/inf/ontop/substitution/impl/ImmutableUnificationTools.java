package it.unibz.inf.ontop.substitution.impl;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.GroundTermTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;

/**
 * Tools for new-gen immutable unifying substitutions.
 */
@Singleton
public class ImmutableUnificationTools {

    private final SubstitutionFactory substitutionFactory;
    private final UnifierUtilities unifierUtilities;

    @Inject
    private ImmutableUnificationTools(SubstitutionFactory substitutionFactory,
                                      UnifierUtilities unifierUtilities) {
        this.substitutionFactory = substitutionFactory;
        this.unifierUtilities = unifierUtilities;
    }

    /**
     * TODO: explain
     */
    private static class UnificationException extends Exception {
    }

    /**
     * TODO: explain
     *
     * MUTABLE
     */
    private class TermPair {
        private boolean canBeRemoved;
        private Variable leftVariable;
        private ImmutableTerm rightTerm;

        private TermPair(Variable variable, ImmutableTerm rightTerm) {
            this.leftVariable = variable;
            this.rightTerm = rightTerm;
            this.canBeRemoved = false;
        }

        /**
         *
         * TODO: explain it
         *
         * May update itself.
         * Returns a list of new pairs
         *
         */
        public ImmutableList<TermPair> applySubstitution(ImmutableSubstitution<? extends ImmutableTerm> substitution)
                throws UnificationException {
            if (canBeRemoved) {
                return ImmutableList.of();
            }

            ImmutableTerm transformedLeftTerm = substitution.applyToVariable(leftVariable);
            ImmutableTerm transformedRightTerm = substitution.apply(rightTerm);

            if (transformedLeftTerm instanceof Variable) {
                leftVariable = (Variable) transformedLeftTerm;
                rightTerm = transformedRightTerm;
                return ImmutableList.of();
            }
            else if (transformedRightTerm instanceof Variable) {
                leftVariable = (Variable) transformedRightTerm;
                rightTerm = transformedLeftTerm;
                return ImmutableList.of();
            }
            else {
                Optional<ImmutableSubstitution<ImmutableTerm>> optionalUnifier =
                        computeDirectedMGU(transformedLeftTerm, transformedRightTerm);
                if (!optionalUnifier.isPresent()) {
                    throw new UnificationException();
                }
                /*
                 * TODO: explain
                 */
                else {
                    canBeRemoved = true;
                    leftVariable = null;
                    rightTerm = null;
                    return convertIntoPairs(optionalUnifier.get());
                }
            }
        }

        public ImmutableSubstitution<ImmutableTerm> getSubstitution() {
            if (canBeRemoved) {
                return substitutionFactory.getSubstitution();
            }
            return substitutionFactory.getSubstitution(leftVariable, rightTerm);
        }
    }

    /**
     * TODO: explain
     *
     */

    public <T extends ImmutableTerm> Optional<ImmutableSubstitution<T>> computeMGU(ImmutableList<T> args1,
                                                                                   ImmutableList<T> args2) {
        // TODO (ROMAN 12/02/20): why is it here?
        if (args1.size() != args2.size())
            throw new IllegalArgumentException("The two argument lists must have the same size");

        return unifierUtilities.getMGU(args1, args2);
    }

    public Optional<ArgumentMapUnification> computeArgumentMapMGU(
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap1,
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap2) {
        ImmutableSet<Integer> firstIndexes = argumentMap1.keySet();
        ImmutableSet<Integer> secondIndexes = argumentMap2.keySet();

        Sets.SetView<Integer> commonIndexes = Sets.intersection(firstIndexes, secondIndexes);

        Optional<ImmutableSubstitution<VariableOrGroundTerm>> unifier = unifierUtilities.getMGU(
                commonIndexes.stream()
                        .map(argumentMap1::get)
                        .collect(ImmutableCollectors.toList()),
                commonIndexes.stream()
                        .map(argumentMap2::get)
                        .collect(ImmutableCollectors.toList()));

        return unifier
                .map(u -> new ArgumentMapUnification(
                        // Merges the argument maps and applies the unifier
                        u.applyToArgumentMap(
                                Sets.union(firstIndexes, secondIndexes).stream()
                                        .collect(ImmutableCollectors.toMap(
                                                i -> i,
                                                i -> Optional.ofNullable((VariableOrGroundTerm) argumentMap1.get(i))
                                                .orElseGet(() -> argumentMap2.get(i))))),
                        u));

    }

    /**
     * TODO: make it replace computeMGUS()
     */
    public Optional<ImmutableSubstitution<NonFunctionalTerm>> computeMGUS2(ImmutableSubstitution<NonFunctionalTerm> s1,
                                                                           ImmutableSubstitution<NonFunctionalTerm> s2) {
        return computeMGUS(s1,s2)
                .map(u -> (ImmutableSubstitution<NonFunctionalTerm>)(ImmutableSubstitution<?>)u);
    }

    /**
     * Computes one Most General Unifier (MGU) of (two) substitutions.
     */
    public Optional<ImmutableSubstitution<ImmutableTerm>> computeMGUS(ImmutableSubstitution<? extends ImmutableTerm> substitution1,
                                                                      ImmutableSubstitution<? extends ImmutableTerm> substitution2) {
        if (substitution1.isEmpty())
            return Optional.of((ImmutableSubstitution<ImmutableTerm>)substitution2);
        else if (substitution2.isEmpty())
            Optional.of((ImmutableSubstitution<ImmutableTerm>)substitution1);

        ImmutableList.Builder<ImmutableTerm> firstArgListBuilder = ImmutableList.builder();
        ImmutableList.Builder<ImmutableTerm> secondArgListBuilder = ImmutableList.builder();

        for (Map.Entry<Variable, ? extends ImmutableTerm> entry : substitution1.getImmutableMap().entrySet()) {
            firstArgListBuilder.add(entry.getKey());
            secondArgListBuilder.add(entry.getValue());
        }

        for (Map.Entry<Variable, ? extends ImmutableTerm> entry : substitution2.getImmutableMap().entrySet()) {
            firstArgListBuilder.add(entry.getKey());
            secondArgListBuilder.add(entry.getValue());
        }

        ImmutableList<ImmutableTerm> firstArgList = firstArgListBuilder.build();
        ImmutableList<ImmutableTerm> secondArgList = secondArgListBuilder.build();

        return computeMGU(firstArgList, secondArgList);
    }

    public Optional<ImmutableSubstitution<VariableOrGroundTerm>> computeAtomMGUS(
            ImmutableSubstitution<VariableOrGroundTerm> substitution1,
            ImmutableSubstitution<VariableOrGroundTerm> substitution2) {
        Optional<ImmutableSubstitution<ImmutableTerm>> optionalMGUS = computeMGUS(substitution1, substitution2);
        return optionalMGUS
                .map(substitution -> substitution.transform(GroundTermTools::convertIntoVariableOrGroundTerm));
    }


    /**
     * Computes a MGU that reuses as much as possible the variables from the target part.
     *
     */
    private Optional<ImmutableSubstitution<ImmutableTerm>> computeDirectedMGU(ImmutableTerm sourceTerm,
                                                                                    ImmutableTerm targetTerm) {
        /*
         * Variable
         */
        if (sourceTerm instanceof Variable) {
            Variable sourceVariable = (Variable) sourceTerm;

            // Constraint
            if ((targetTerm instanceof ImmutableFunctionalTerm)
                    && ((ImmutableFunctionalTerm) targetTerm).getVariables().contains(sourceVariable)) {
                return Optional.empty();
            }

            ImmutableSubstitution<ImmutableTerm> substitution = sourceVariable.equals(targetTerm)
                    ? substitutionFactory.getSubstitution()
                    : substitutionFactory.getSubstitution(sourceVariable, targetTerm);

            return Optional.of(substitution);
        }

        /*
         * Functional term
         */
        else if (sourceTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm sourceFunctionalTerm = (ImmutableFunctionalTerm) sourceTerm;

            if (targetTerm instanceof Variable) {
                Variable targetVariable = (Variable) targetTerm;

                // Constraint
                if (sourceFunctionalTerm.getVariables().contains(targetVariable)) {
                    return Optional.empty();
                }
                else {
                    ImmutableSubstitution<ImmutableTerm> substitution = substitutionFactory.getSubstitution(targetVariable, sourceTerm);
                    return Optional.of(substitution);
                }
            }
            else if (targetTerm instanceof ImmutableFunctionalTerm) {
                return computeDirectedMGUOfTwoFunctionalTerms((ImmutableFunctionalTerm) sourceTerm,
                        (ImmutableFunctionalTerm) targetTerm);
            }
            else {
                return Optional.empty();
            }
        }
        /*
         * Constant
         */
        else if(sourceTerm instanceof Constant) {
            if (targetTerm instanceof Variable) {
                Variable targetVariable = (Variable) targetTerm;
                ImmutableSubstitution<ImmutableTerm> substitution = substitutionFactory.getSubstitution(targetVariable, sourceTerm);
                return Optional.of(substitution);
            }
            else if (sourceTerm.equals(targetTerm)) {
                return Optional.of(substitutionFactory.getSubstitution());
            }
            else {
                return Optional.empty();
            }
        }
        else {
            throw new RuntimeException("Unexpected term: " + sourceTerm + " (" + sourceTerm.getClass() + ")");
        }
    }

    /**
     * TODO: explain
     */
    private Optional<ImmutableSubstitution<ImmutableTerm>> computeDirectedMGUOfTwoFunctionalTerms(
            ImmutableFunctionalTerm sourceTerm, ImmutableFunctionalTerm targetTerm) {
        /*
         * Function symbol equality
         */
        if (!sourceTerm.getFunctionSymbol().equals(
                targetTerm.getFunctionSymbol())) {
            return Optional.empty();
        }

        ImmutableList<? extends ImmutableTerm> sourceChildren = sourceTerm.getTerms();
        ImmutableList<? extends ImmutableTerm> targetChildren = targetTerm.getTerms();

        int childNb = sourceChildren.size();
        if (targetChildren.size() != childNb) {
            return Optional.empty();
        }

        final ImmutableList.Builder<TermPair> pairBuilder = ImmutableList.builder();
        for (int i=0; i < childNb; i++) {
            /*
             * Recursive
             */
            Optional<ImmutableSubstitution<ImmutableTerm>> optionalChildTermUnifier =
                    computeDirectedMGU(sourceChildren.get(i), targetChildren.get(i));

            /*
             * If the unification of one pair of sub-terms is not possible,
             * no global unification is possible.
             */
            if (!optionalChildTermUnifier.isPresent()) {
                return Optional.empty();
            }

            /*
             * Adds all its pairs
             */
            pairBuilder.addAll(convertIntoPairs(optionalChildTermUnifier.get()));
        }

        return unifyPairs(pairBuilder.build());
    }

    /**
     * TODO: explain
     *
     */
    private Optional<ImmutableSubstitution<ImmutableTerm>> unifyPairs(ImmutableList<TermPair> originalPairs) {

        /*
         * Some pairs will be removed, some others will be added.
         */
        List<TermPair> allPairs = new LinkedList<>(originalPairs);
        Queue<TermPair> pairsToVisit = new LinkedList<>(originalPairs);

        try {
            /*
             * TODO: explain
             */
            while (!pairsToVisit.isEmpty()) {
                TermPair currentPair = pairsToVisit.poll();
                if (currentPair.canBeRemoved) {
                    continue;
                }
                ImmutableSubstitution<ImmutableTerm> substitution = currentPair.getSubstitution();

                List<TermPair> additionalPairs = new ArrayList<>();
                /*
                 * TODO: explain
                 */
                Iterator<TermPair> it = allPairs.iterator();
                while (it.hasNext()) {
                    TermPair pairToUpdate = it.next();
                    if (pairToUpdate == currentPair) {
                        continue;
                    } else {
                        additionalPairs.addAll(pairToUpdate.applySubstitution(substitution));
                        if (pairToUpdate.canBeRemoved) {
                            it.remove();
                        }
                    }
                }
                allPairs.addAll(additionalPairs);
                pairsToVisit.addAll(additionalPairs);
            }
            return Optional.of(convertPairs2Substitution(allPairs));
        }
        catch (UnificationException e) {
            return Optional.empty();
        }
    }

    private ImmutableList<TermPair> convertIntoPairs(ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        ImmutableList.Builder<TermPair> listBuilder = ImmutableList.builder();
        for (Map.Entry<Variable, ? extends ImmutableTerm> entry : substitution.getImmutableMap().entrySet()) {
            listBuilder.add(new TermPair(entry.getKey(), entry.getValue()));
        }
        return listBuilder.build();
    }

    private ImmutableSubstitution<ImmutableTerm> convertPairs2Substitution(List<TermPair> pairs)
            throws UnificationException {
        Map<Variable, ImmutableTerm> substitutionMap = new HashMap<>();
        for(TermPair pair : pairs) {
            if (!substitutionMap.containsKey(pair.leftVariable)) {
                substitutionMap.put(pair.leftVariable, pair.rightTerm);
            }
            else if (!substitutionMap.get(pair.leftVariable).equals(pair.rightTerm)) {
                throw new UnificationException();
            }
        }
        return substitutionFactory.getSubstitution(ImmutableMap.copyOf(substitutionMap));
    }

    public static class ArgumentMapUnification {
        public final ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap;
        public final ImmutableSubstitution<VariableOrGroundTerm> substitution;

        public ArgumentMapUnification(ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                      ImmutableSubstitution<VariableOrGroundTerm> substitution) {
            this.argumentMap = argumentMap;
            this.substitution = substitution;
        }
    }
}
