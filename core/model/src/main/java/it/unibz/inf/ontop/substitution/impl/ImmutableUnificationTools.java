package it.unibz.inf.ontop.substitution.impl;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import java.util.*;

/**
 * Tools for new-gen immutable unifying substitutions.
 */
@Singleton
public class ImmutableUnificationTools {

    private final TermFactory termFactory;
    private final AtomFactory atomFactory;
    private final SubstitutionFactory substitutionFactory;
    private final ImmutableSubstitutionTools substitutionTools;
    private final UnifierUtilities unifierUtilities;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    private ImmutableUnificationTools(TermFactory termFactory, AtomFactory atomFactory, SubstitutionFactory substitutionFactory,
                                      ImmutableSubstitutionTools substitutionTools, UnifierUtilities unifierUtilities,
                                      ImmutabilityTools immutabilityTools) {
        this.termFactory = termFactory;
        this.atomFactory = atomFactory;
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

    private static String PREDICATE_STR = "pred";

    /**
     * TODO: explain
     *
     */
    public Optional<ImmutableSubstitution<ImmutableTerm>> computeMGU(ImmutableFunctionalTerm term1, ImmutableFunctionalTerm term2) {
        Substitution mutableSubstitution = unifierUtilities.getMGU(immutabilityTools.convertToMutableFunction(term1),
                immutabilityTools.convertToMutableFunction(term2));

        if (mutableSubstitution == null) {
            return Optional.empty();
        }
        return Optional.of(substitutionTools.convertMutableSubstitution(mutableSubstitution));
    }

    public Optional<ImmutableSubstitution<VariableOrGroundTerm>> computeAtomMGU(DataAtom atom1, DataAtom atom2) {
        Substitution mutableSubstitution = unifierUtilities.getMGU(
                immutabilityTools.convertToMutableFunction(atom1),
                immutabilityTools.convertToMutableFunction(atom2));

        if (mutableSubstitution == null) {
            return Optional.empty();
        }

        ImmutableMap.Builder<Variable, VariableOrGroundTerm> substitutionMapBuilder = ImmutableMap.builder();
        for (Map.Entry<Variable, Term> entry : mutableSubstitution.getMap().entrySet()) {
            VariableOrGroundTerm value = ImmutabilityTools.convertIntoVariableOrGroundTerm(entry.getValue());

            substitutionMapBuilder.put(entry.getKey(), value);
        }

        ImmutableSubstitution<VariableOrGroundTerm> immutableSubstitution = substitutionFactory.getSubstitution(
                substitutionMapBuilder.build());
        return Optional.of(immutableSubstitution);

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

        Predicate predicate = atomFactory.getAtomPredicate(PREDICATE_STR, firstArgList.size());

        ImmutableFunctionalTerm functionalTerm1 = termFactory.getImmutableFunctionalTerm(predicate, firstArgList);
        ImmutableFunctionalTerm functionalTerm2 = termFactory.getImmutableFunctionalTerm(predicate, secondArgList);

        return computeMGU(functionalTerm1, functionalTerm2);
    }

    public Optional<ImmutableSubstitution<VariableOrGroundTerm>> computeAtomMGUS(
            ImmutableSubstitution<VariableOrGroundTerm> substitution1,
            ImmutableSubstitution<VariableOrGroundTerm> substitution2) {
        Optional<ImmutableSubstitution<ImmutableTerm>> optionalMGUS = computeMGUS(substitution1, substitution2);
        if (optionalMGUS.isPresent()) {
            return Optional.of(substitutionTools.convertIntoVariableOrGroundTermSubstitution(
                    optionalMGUS.get()));
        }
        else {
            return Optional.empty();
        }
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

        ImmutableList<? extends ImmutableTerm> sourceChildren = sourceTerm.getArguments();
        ImmutableList<? extends ImmutableTerm> targetChildren = targetTerm.getArguments();

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
