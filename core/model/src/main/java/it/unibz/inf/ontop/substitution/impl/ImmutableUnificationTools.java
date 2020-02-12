package it.unibz.inf.ontop.substitution.impl;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.PredicateImpl;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import java.util.*;

/**
 * Tools for new-gen immutable unifying substitutions.
 */
@Singleton
public class ImmutableUnificationTools {

    private final SubstitutionFactory substitutionFactory;
    private final ImmutableSubstitutionTools substitutionTools;
    private final UnifierUtilities unifierUtilities;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    private ImmutableUnificationTools(SubstitutionFactory substitutionFactory,
                                      ImmutableSubstitutionTools substitutionTools, UnifierUtilities unifierUtilities,
                                      ImmutabilityTools immutabilityTools) {
        this.substitutionFactory = substitutionFactory;
        this.substitutionTools = substitutionTools;
        this.unifierUtilities = unifierUtilities;
        this.immutabilityTools = immutabilityTools;
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
    private static class TermPair {
        private boolean canBeRemoved;
        private Variable leftVariable;
        private ImmutableTerm rightTerm;
        private final SubstitutionFactory substitutionFactory;
        private final ImmutableUnificationTools unificationTools;

        private TermPair(Variable variable, ImmutableTerm rightTerm, SubstitutionFactory substitutionFactory,
                         ImmutableUnificationTools unificationTools) {
            this.leftVariable = variable;
            this.rightTerm = rightTerm;
            this.substitutionFactory = substitutionFactory;
            this.unificationTools = unificationTools;
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

            ImmutableTerm transformedLeftTerm = substitution.apply(leftVariable);
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
                        unificationTools.computeDirectedMGU(transformedLeftTerm, transformedRightTerm);
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
                    return unificationTools.convertIntoPairs(optionalUnifier.get());
                }
            }
        }

        public ImmutableSubstitution<ImmutableTerm> getSubstitution() {
            if (canBeRemoved) {
                return substitutionFactory.getSubstitution();
            }
            return substitutionFactory.getSubstitution(ImmutableMap.of(leftVariable, rightTerm));
        }

        boolean canBeRemoved() {
            return canBeRemoved;
        }

        Variable getLeftVariable() {
            return leftVariable;
        }

        ImmutableTerm getRightTerm() {
            return rightTerm;
        }

    }

    /**
     * TODO: explain
     *
     */

    public <T extends ImmutableTerm> Optional<ImmutableSubstitution<T>> computeMGU(ImmutableList<T> args1,
                                                                                   ImmutableList<T> args2) {
        if (args1.size() != args2.size())
            throw new IllegalArgumentException("The two argument lists must have the same size");

        ImmutableMap<Variable, ImmutableTerm> mutableSubstitution = unifierUtilities.getMGU(args1, args2);

        if (mutableSubstitution == null) {
            return Optional.empty();
        }
        return Optional.of(substitutionFactory.getSubstitution((ImmutableMap)mutableSubstitution));
    }

    public Optional<ImmutableSubstitution<VariableOrGroundTerm>> computeAtomMGU(DataAtom<?> atom1, DataAtom<?> atom2) {
        if (!atom1.getPredicate().equals(atom2.getPredicate()))
            return Optional.empty();

        ImmutableMap<Variable, ImmutableTerm> mutableSubstitution = unifierUtilities.getMGU(atom1.getArguments(), atom2.getArguments());

        if (mutableSubstitution == null) {
            return Optional.empty();
        }

        return Optional.of(substitutionFactory.getSubstitution((ImmutableMap)mutableSubstitution));
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
                .map(substitutionTools::convertIntoVariableOrGroundTermSubstitution);
    }


    /**
     * Computes a MGU that reuses as much as possible the variables from the target part.
     *
     */
    public Optional<ImmutableSubstitution<ImmutableTerm>> computeDirectedMGU(ImmutableTerm sourceTerm,
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
                    : substitutionFactory.getSubstitution(ImmutableMap.of(sourceVariable, targetTerm));

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
                    ImmutableSubstitution<ImmutableTerm> substitution = substitutionFactory.getSubstitution(
                            ImmutableMap.of(targetVariable, sourceTerm));
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
                ImmutableSubstitution<ImmutableTerm> substitution = substitutionFactory.getSubstitution(
                        ImmutableMap.of(targetVariable, sourceTerm));
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
                if (currentPair.canBeRemoved()) {
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
                        if (pairToUpdate.canBeRemoved()) {
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
            listBuilder.add(new TermPair(entry.getKey(), entry.getValue(), substitutionFactory, this));
        }
        return listBuilder.build();
    }

    private ImmutableSubstitution<ImmutableTerm> convertPairs2Substitution(List<TermPair> pairs)
            throws UnificationException {
        Map<Variable, ImmutableTerm> substitutionMap = new HashMap<>();
        for(TermPair pair : pairs) {
            Variable leftVariable = pair.getLeftVariable();
            ImmutableTerm rightTerm = pair.getRightTerm();
            if (!substitutionMap.containsKey(leftVariable)) {
                substitutionMap.put(leftVariable, rightTerm);
            }
            else if (!substitutionMap.get(leftVariable).equals(rightTerm)) {
                throw new UnificationException();
            }
        }
        return substitutionFactory.getSubstitution(ImmutableMap.copyOf(substitutionMap));
    }
}
