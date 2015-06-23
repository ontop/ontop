package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fj.P;
import fj.P2;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.model.Var2VarSubstitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionUtilities;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitutionImpl;
import org.semanticweb.ontop.pivotalrepr.DataAtom;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO: explain
 */
public class SubQueryUnificationTools {

    /**
     * TODO: explain
     */
    public static class SubQueryUnificationException extends Exception {
        protected SubQueryUnificationException(String message) {
            super(message);
        }
    }

    /**
     * TODO: explain
     *
     * Returns a new IntermediateQuery (the original one is untouched).
     */
    public static IntermediateQuery unifySubQuery(final IntermediateQuery originalSubQuery,
                                                  final DataAtom targetDataAtom,
                                                  final ImmutableSet<VariableImpl> reservedVariables)
            throws SubQueryUnificationException {

        ConstructionNode originalRootNode = originalSubQuery.getRootProjectionNode();
        ImmutableSet<VariableImpl> originalVariables = VariableCollector.collectVariables(originalSubQuery);
        ImmutableSet<Variable> allKnownVariables = ImmutableSet.<Variable>builder()
                .addAll(reservedVariables)
                .addAll(originalVariables)
                .build();

        /**
         * Should have already been checked.
         */
        if (!originalRootNode.getProjectionAtom().hasSamePredicateAndArity(targetDataAtom)) {
            throw new IllegalArgumentException("The target data atom is not compatible with the query");
        }

        VariableGenerator variableGenerator = new VariableGenerator(allKnownVariables);

//        ConstructionNodeUnification rootRenaming = new ConstructionNodeUnification(originalRootNode, originalSubQuery,
//                targetDataAtom, reservedVariables, variableGenerator);
//
//        ConstructionNode newRootNode = rootRenaming.generateNewProjectionNode();
//        Var2VarSubstitution conflictSubstitution =  rootRenaming.generateConflictSubstitutionForSubTree();
//        // TODO: create a sub-type of immutable substitution
//        ImmutableSubstitution normalSubstitution =  rootRenaming.generateNormalSubstitutionForSubTree();


        // TODO: continue


        throw new RuntimeException("Not fully implemented yet");
    }


    /**
     * TODO: explain
     */
    protected static P2<ConstructionNode, ImmutableSubstitution<VariableOrGroundTerm>> unifyConstructionNode(ConstructionNode constructionNode,
                                                                                                             DataAtom targetAtom)
            throws SubQueryUnificationException{

        if (!haveDisjunctVariableSets(constructionNode, targetAtom)) {
            throw new IllegalArgumentException("The variable sets of the construction node and the target atom must " +
                    "be disjunct!");
        }

        ImmutableSubstitution<VariableOrGroundTerm> atomSubstitution = extractAtomSubstitution(
                constructionNode.getProjectionAtom(), targetAtom);

        P2<ImmutableList<InjectiveVar2VarSubstitution>, ImmutableSubstitution<VariableOrGroundTerm>> decomposition
                = splitAtomSubstitution(atomSubstitution);

        ImmutableList<InjectiveVar2VarSubstitution> renamingSubstitutions = decomposition._1();
        ImmutableSubstitution<VariableOrGroundTerm> additionalConstraintSubstitution = decomposition._2();

        Optional<ImmutableSubstitution<ImmutableTerm>> optionalConstraintUnifier = ImmutableSubstitutionUtilities.computeMGUU(
                additionalConstraintSubstitution, constructionNode.getSubstitution());

        if (!optionalConstraintUnifier.isPresent()) {
            // TODO: Is it an internal error?
            throw new SubQueryUnificationException("Constraints could not be unified");
        }
        ImmutableSubstitution<ImmutableTerm> constraintUnifier = optionalConstraintUnifier.get();

        ImmutableSubstitution<ImmutableTerm> filteredConstraintSubstitution = extractConstraintsNotEncodedInAtom(
                constraintUnifier, additionalConstraintSubstitution);


        ImmutableSubstitution<ImmutableTerm> newConstructionNodeSubstitution = ImmutableSubstitutionUtilities.renameSubstitution(
                filteredConstraintSubstitution, renamingSubstitutions);

        ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate = extractSubstitutionToPropagate(atomSubstitution,
                constraintUnifier, filteredConstraintSubstitution);


        ConstructionNode newConstructionNode = new ConstructionNodeImpl(targetAtom, newConstructionNodeSubstitution);
        return P.p(newConstructionNode, substitutionToPropagate);
    }

    /**
     * TODO: implement it
     */
    private static ImmutableSubstitution<VariableOrGroundTerm> extractSubstitutionToPropagate(
            ImmutableSubstitution<VariableOrGroundTerm> atomSubstitution,
            ImmutableSubstitution<ImmutableTerm> constraintUnifier,
            ImmutableSubstitution<ImmutableTerm> filteredConstraintSubstitution) {
        throw new RuntimeException("Not fully implemented yet");
    }

    /**
     * TODO: explain it
     */
    private static ImmutableSubstitution<ImmutableTerm> extractConstraintsNotEncodedInAtom(
            ImmutableSubstitution<ImmutableTerm> constraintUnifier,
            ImmutableSubstitution<VariableOrGroundTerm> constraintsFromAtoms) {
        if (constraintsFromAtoms.isEmpty())
            return constraintUnifier;

        ImmutableSet<VariableImpl> variablesToFilterOut = constraintsFromAtoms.getImmutableMap().keySet();
        ImmutableMap<VariableImpl, ImmutableTerm> constraintMap = constraintUnifier.getImmutableMap();

        ImmutableMap.Builder<VariableImpl, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();
        for (VariableImpl variable : constraintMap.keySet()) {
            if (!variablesToFilterOut.contains(variable)) {
                substitutionMapBuilder.put(variable, constraintMap.get(variable));
            }
        }

        return new ImmutableSubstitutionImpl<>(substitutionMapBuilder.build());
    }

    /**
     * TODO: explain
     *
     */
    private static P2<ImmutableList<InjectiveVar2VarSubstitution>, ImmutableSubstitution<VariableOrGroundTerm>>
                splitAtomSubstitution(ImmutableSubstitution<VariableOrGroundTerm> atomSubstitution) {

        ImmutableMap<VariableImpl, VariableOrGroundTerm> originalMap = atomSubstitution.getImmutableMap();
        ImmutableMap.Builder<VariableImpl, VariableOrGroundTerm> constraintMapBuilder = ImmutableMap.builder();
        Set<VariableImpl> originalVariablesToRename = new HashSet<>();

        /**
         * Extracts var-to-ground-term constraints and collects original variables that will be renamed
         */
        for (Map.Entry<VariableImpl, VariableOrGroundTerm> entry : originalMap.entrySet()) {
            VariableOrGroundTerm targetTerm = entry.getValue();
            VariableImpl originalVariable = entry.getKey();

            if (targetTerm instanceof GroundTerm) {
                constraintMapBuilder.put(originalVariable, targetTerm);
            }
            else {
                originalVariablesToRename.add(originalVariable);
            }
        }

        /**
         * Extracts the injective renaming substitutions and some additional constraints.
         */
        P2<ImmutableList<InjectiveVar2VarSubstitution>, ImmutableMap<VariableImpl, VariableOrGroundTerm>> extractedPair
                = extractRenamingSubstitutions(originalMap, originalVariablesToRename);
        ImmutableList<InjectiveVar2VarSubstitution> renamingSubstitutions = extractedPair._1();
        constraintMapBuilder.putAll(extractedPair._2());

        ImmutableSubstitution<VariableOrGroundTerm> constraintSubstitution = new ImmutableSubstitutionImpl<>(
                constraintMapBuilder.build());
        return P.p(renamingSubstitutions, constraintSubstitution);
    }

    /**
     * Creates the injective renaming substitutions and extracts additional constraints.
     *
     * TODO: Further explain
     *
     */
    private static P2<ImmutableList<InjectiveVar2VarSubstitution>, ImmutableMap<VariableImpl, VariableOrGroundTerm>>
                extractRenamingSubstitutions(ImmutableMap<VariableImpl, VariableOrGroundTerm> originalMap,
                                             Set<VariableImpl> originalVariablesToRenameLater) {

        ImmutableMap.Builder<VariableImpl, VariableOrGroundTerm> additionalConstraintMapBuilder = ImmutableMap.builder();

        ImmutableList.Builder<InjectiveVar2VarSubstitution> renamingListBuilder = ImmutableList.builder();

        while (!originalVariablesToRenameLater.isEmpty()) {
            Map<VariableImpl, VariableImpl> renamingMap = new HashMap<>();

            Set<VariableImpl> originalVariablesToRenameNow = originalVariablesToRenameLater;
            originalVariablesToRenameLater = new HashSet<>();

            for (VariableImpl originalVariable : originalVariablesToRenameNow) {
                VariableImpl targetVariable = (VariableImpl) originalMap.get(originalVariable);

                if (renamingMap.values().contains(targetVariable)) {
                    originalVariablesToRenameLater.add(originalVariable);
                    additionalConstraintMapBuilder.put(targetVariable, originalVariable);
                }
                else {
                    renamingMap.put(originalVariable, targetVariable);
                }
            }
            // Creates a new renaming substitution
            renamingListBuilder.add(new InjectiveVar2VarSubstitutionImpl(renamingMap));
        }

        return P.p(renamingListBuilder.build(), additionalConstraintMapBuilder.build());
    }



    /**
     * TODO: explain
     *
     * This could have been implemented using a MGU
     * but we want to distinguish different cases where unification is not impossible.
     *
     * In one case, unification could be possible but only in an indirect manner.
     */
    private static ImmutableSubstitution<VariableOrGroundTerm> extractAtomSubstitution(DataAtom originalAtom,
                                                                                       DataAtom newAtom)
            throws SubQueryUnificationException {

        if(!originalAtom.hasSamePredicateAndArity(newAtom)) {
            throw new SubQueryUnificationException(originalAtom + " and " + newAtom
                    + " have different predicates and/or arities");
        }

        // ImmutableMap.Builder<VariableImpl, VariableOrGroundTerm> mapBuilder = ImmutableMap.builder();
        Map<VariableImpl, VariableOrGroundTerm> substitutionMap = new HashMap<>();

        ImmutableList<VariableOrGroundTerm> originalArgs = originalAtom.getVariablesOrGroundTerms();
        ImmutableList<VariableOrGroundTerm> newArgs = newAtom.getVariablesOrGroundTerms();

        for (int i = 0; i < originalArgs.size(); i++) {
            VariableOrGroundTerm originalArg = originalArgs.get(i);
            VariableOrGroundTerm newArg = newArgs.get(i);

            if (originalArg instanceof VariableImpl) {
                VariableImpl originalVar = (VariableImpl) originalArg;

                /**
                 * Normal case: new variable to variable-or-ground-term.
                 * --> added to the map.
                 */
                if (!substitutionMap.containsKey(originalVar)) {
                    substitutionMap.put(originalVar, newArg);
                }
                /**
                 * Otherwise, we except this entry to be already present.
                 */
                else if (!substitutionMap.get(originalVar).equals(newArg)) {
                    /**
                     * TODO: throw a different exception when we will support
                     * indirect unification.
                     *
                     */
                    throw new SubQueryUnificationException(originalAtom + " and " + newAtom
                            + " are not DIRECTLY unifiable");
                }
            }
            /**
             * The original arg is a ground term --> should be equal
             */
            else if (!originalArg.equals(newArg)) {
                throw new SubQueryUnificationException(originalAtom + " and " + newAtom
                        + " are not unifiable");
            }
        }
        return new ImmutableSubstitutionImpl<>(ImmutableMap.copyOf(substitutionMap));
    }

    private static boolean haveDisjunctVariableSets(ConstructionNode constructionNode, DataAtom targetAtom) {

        Set<VariableImpl> variableSet = new HashSet<>();

        /**
         * First put the target variables
         */
        for (VariableOrGroundTerm term : targetAtom.getVariablesOrGroundTerms()) {
            if (term instanceof VariableImpl)
                variableSet.add((VariableImpl)term);
        }


        /**
         * Removes all the variables that are not used in the construction node.
         * Said differently, computes the intersection.
         */
        variableSet.retainAll(VariableCollector.collectVariables(constructionNode));

        return variableSet.isEmpty();

    }


}
