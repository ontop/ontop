package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fj.P;
import fj.P2;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.model.Var2VarSubstitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
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
    protected static P2<ConstructionNode, Var2VarSubstitution> unifyConstructionNode(ConstructionNode constructionNode,
                                                                                     DataAtom targetAtom)
            throws SubQueryUnificationException{

        if (!haveDisjunctVariableSets(constructionNode, targetAtom)) {
            throw new IllegalArgumentException("The variable sets of the construction node and the target atom must " +
                    "be disjunct!");
        }

        ImmutableSubstitution<VariableOrGroundTerm> atomSubstitution = extractAtomSubstitution(
                constructionNode.getProjectionAtom(), targetAtom);

        // TODO: continue

        /**
         * TODO: fill data
         */
        ConstructionNode newConstructionNode = null;
        Var2VarSubstitution propagatedSubstitution = null;
        return P.p(newConstructionNode, propagatedSubstitution);
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
