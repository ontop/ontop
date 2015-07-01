package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fj.P;
import fj.P2;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionUtilities;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitutionImpl;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition;

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
     */
    private static class AtomSubstitutionSplit {

        private final ImmutableList<InjectiveVar2VarSubstitution> renamingSubstitutions;
        private final ImmutableSubstitution<VariableOrGroundTerm> constraintSubstitution;


        /**
         * TODO: explain
         */
        protected AtomSubstitutionSplit(ImmutableSubstitution<VariableOrGroundTerm> atomSubstitution) {
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
            renamingSubstitutions = extractedPair._1();
            constraintMapBuilder.putAll(extractedPair._2());

            constraintSubstitution = new ImmutableSubstitutionImpl<>(constraintMapBuilder.build());
        }

        public ImmutableList<InjectiveVar2VarSubstitution> getRenamingSubstitutions() {
            return renamingSubstitutions;
        }

        public ImmutableSubstitution<VariableOrGroundTerm> getConstraintSubstitution() {
            return constraintSubstitution;
        }
    }

    /**
     * TODO: explain
     */
    protected static class ConstructionNodeUnification {
        private final ConstructionNode unifiedNode;
        private final SubstitutionPropagator substitutionPropagator;

        protected ConstructionNodeUnification(ConstructionNode unifiedNode,
                                              SubstitutionPropagator newPropagator) {
            this.unifiedNode = unifiedNode;
            this.substitutionPropagator = newPropagator;
        }

        public final ConstructionNode getUnifiedNode() {
            return unifiedNode;
        }

        public final SubstitutionPropagator getSubstitutionPropagator() {
            return substitutionPropagator;
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

        ConstructionNode originalRootNode = originalSubQuery.getRootConstructionNode();

        /**
         * Should have already been checked.
         */
        if (!originalRootNode.getProjectionAtom().hasSamePredicateAndArity(targetDataAtom)) {
            throw new IllegalArgumentException("The target data atom is not compatible with the query");
        }

        QueryNodeRenamer renamer = new QueryNodeRenamer(
                computeRenamingSubstitution(originalSubQuery, reservedVariables));

        ConstructionNodeUnification rootUnification = unifyConstructionNode(renamer.transform(originalRootNode), targetDataAtom);

        try {
            IntermediateQueryBuilder queryBuilder = new JgraphtIntermediateQueryBuilder();
            queryBuilder.init(rootUnification.unifiedNode);

            /**
             * TODO: explain
             */
            queryBuilder = propagateToChildren(queryBuilder, originalSubQuery, originalRootNode, rootUnification.unifiedNode,
                    rootUnification.substitutionPropagator, renamer);

            return queryBuilder.build();

            /**
             * TODO: should we expect this exception? Not just an internal error?
             */
        } catch(IntermediateQueryBuilderException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    /**
     * TODO: explain
     *
     * Recursive
     */
    private static IntermediateQueryBuilder propagateToChildren(IntermediateQueryBuilder queryBuilder,
                                                                IntermediateQuery originalSubQuery,
                                                                QueryNode originalParentNode,
                                                                QueryNode unifiedParentNode,
                                                                SubstitutionPropagator substitutionPropagator,
                                                                QueryNodeRenamer renamer)
            throws IntermediateQueryBuilderException, SubQueryUnificationException {
        for(QueryNode originalChild : originalSubQuery.getCurrentSubNodesOf(originalParentNode)) {
            Optional<QueryNode> optionalNewChild;
            SubstitutionPropagator propagatorForChild;
            try {
                QueryNode newChild = originalChild
                        .acceptNodeTransformer(renamer)
                        .acceptNodeTransformer(substitutionPropagator);
                optionalNewChild = Optional.of(newChild);

                propagatorForChild = substitutionPropagator;

                /**
                 * New substitution
                 * TODO: further explain
                 */
            } catch (SubstitutionPropagator.NewSubstitutionException e) {
                optionalNewChild = Optional.of(e.getTransformedNode());
                propagatorForChild = new SubstitutionPropagator(e.getSubstitution());
            }
            /**
             * Unification rejected by a sub-construction node.
             */
            catch(SubstitutionPropagator.UnificationException e) {
                throw new SubQueryUnificationException(e.getMessage());
            }
            /**
             * No new child because not needed anymore.
             */ catch(SubstitutionPropagator.NotNeededNodeException e) {
                optionalNewChild = Optional.absent();
                propagatorForChild = substitutionPropagator;
            }
            /**
             * Unexpected
             */
            catch (QueryNodeTransformationException e) {
                throw new RuntimeException("Unexpected: " + e.getLocalizedMessage());
            }
            Optional<ArgumentPosition> optionalPosition = originalSubQuery.getOptionalPosition(originalParentNode,
                    originalChild);

            QueryNode nextOriginalParent;
            QueryNode nextNewParent;
            /**
             * Normal case: the new child becomes the new parent.
             */
            if (optionalNewChild.isPresent()) {
                QueryNode newChild = optionalNewChild.get();
                queryBuilder.addChild(unifiedParentNode, newChild, optionalPosition);
                nextOriginalParent = originalChild;
                nextNewParent =  newChild;
            }
            /**
             * No new child: keep the same parent
             */
            else {
                nextOriginalParent = originalParentNode;
                nextNewParent = unifiedParentNode;
            }

            // Recursive call
            queryBuilder = propagateToChildren(queryBuilder, originalSubQuery, nextOriginalParent, nextNewParent,
                    propagatorForChild, renamer);
        }
        return queryBuilder;
    }

    /**
     * TODO: explain
     *
     */
    private static InjectiveVar2VarSubstitution computeRenamingSubstitution(IntermediateQuery subQuery,
                                                                            ImmutableSet<VariableImpl> reservedVariables) {
        ImmutableSet<VariableImpl> subQueryVariables = VariableCollector.collectVariables(subQuery);
        ImmutableSet<Variable> allKnownVariables = ImmutableSet.<Variable>builder()
                .addAll(reservedVariables)
                .addAll(subQueryVariables)
                .build();
        VariableGenerator variableGenerator = new VariableGenerator(allKnownVariables);

        ImmutableMap.Builder<VariableImpl, VariableImpl> renamingBuilder = ImmutableMap.builder();

        for (VariableImpl subQueryVariable : subQueryVariables) {
            /**
             * If there is a conflict: creates a new variable and
             * adds an entry in the renaming substitution
             */
            if (reservedVariables.contains(subQueryVariable)) {
                VariableImpl newVariable = variableGenerator.generateNewVariableFromVar(subQueryVariable);
                renamingBuilder.put(subQueryVariable, newVariable);
            }
        }

        return new InjectiveVar2VarSubstitutionImpl(renamingBuilder.build());
    }

    /**
     * TODO: explain
     *
     * TODO: support query modifiers
     *
     */
    protected static ConstructionNodeUnification unifyConstructionNode(
            ConstructionNode renamedConstructionNode, DataAtom targetAtom)
            throws SubQueryUnificationException{

        if (!haveDisjunctVariableSets(renamedConstructionNode, targetAtom)) {
            throw new IllegalArgumentException("The variable sets of the construction node and the target atom must " +
                    "be disjunct!");
        }

        ImmutableSubstitution<VariableOrGroundTerm> atomSubstitution = extractAtomSubstitution(
                renamedConstructionNode.getProjectionAtom(), targetAtom);

        AtomSubstitutionSplit atomSubstitutionSplit = new AtomSubstitutionSplit(atomSubstitution);

        Optional<ImmutableSubstitution<ImmutableTerm>> optionalConstraintUnifier = ImmutableSubstitutionUtilities.computeMGUU(
                atomSubstitutionSplit.constraintSubstitution, renamedConstructionNode.getSubstitution());

        if (!optionalConstraintUnifier.isPresent()) {
            // TODO: Is it an internal error?
            throw new SubQueryUnificationException("Constraints could not be unified");
        }
        ImmutableSubstitution<ImmutableTerm> constraintUnifier = optionalConstraintUnifier.get();

        ImmutableSubstitution<ImmutableTerm> filteredConstraintSubstitution = extractConstraintsNotEncodedInAtom(
                constraintUnifier, atomSubstitutionSplit.constraintSubstitution);

        ImmutableSubstitution<ImmutableTerm> newConstructionNodeSubstitution = ImmutableSubstitutionUtilities.renameSubstitution(
                filteredConstraintSubstitution, atomSubstitutionSplit.renamingSubstitutions);

        ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate = extractSubstitutionToPropagate(
                atomSubstitutionSplit.renamingSubstitutions, constraintUnifier, filteredConstraintSubstitution);

        Optional<ImmutableQueryModifiers> newOptionalQueryModifiers = updateOptionalModifiers(
                renamedConstructionNode.getOptionalModifiers(), atomSubstitution, constraintUnifier);

        ConstructionNode newConstructionNode = new ConstructionNodeImpl(targetAtom, newConstructionNodeSubstitution,
                newOptionalQueryModifiers);
        return new ConstructionNodeUnification(newConstructionNode, new SubstitutionPropagator(substitutionToPropagate));
    }

    /**
     * TODO: explain
     */
    private static ImmutableSubstitution<VariableOrGroundTerm> extractSubstitutionToPropagate(
            ImmutableList<InjectiveVar2VarSubstitution> renamingSubstitutions,
            ImmutableSubstitution<ImmutableTerm> constraintUnifier,
            ImmutableSubstitution<ImmutableTerm> filteredConstraintSubstitution) {
        ImmutableMap.Builder<VariableImpl, VariableOrGroundTerm> mapBuilder = ImmutableMap.builder();

        /**
         * Extracts renaming mappings that are not in the filtered constraint substitution
         */
        for(InjectiveVar2VarSubstitution renamingSubstitution : renamingSubstitutions) {
            for (Map.Entry<VariableImpl, VariableImpl> entry : renamingSubstitution.getImmutableMap().entrySet()) {
                VariableImpl varToRename = entry.getKey();

                if (!filteredConstraintSubstitution.isDefining(varToRename)) {
                    mapBuilder.put(varToRename, entry.getValue());
                }
            }
        }

        /**
         * Extracts the mappings of the unifier that are not in the filtered constraint substitution
         */
        for (Map.Entry<VariableImpl, ImmutableTerm> entry : constraintUnifier.getImmutableMap().entrySet()) {
            VariableImpl varToRename = entry.getKey();

            if (!filteredConstraintSubstitution.isDefining(varToRename)) {
                ImmutableTerm newTerm =  entry.getValue();

                if (!(newTerm instanceof VariableOrGroundTerm)) {
                    throw new IllegalArgumentException("Inconsistent filteredConstraintSubstitution " +
                            "regarding the constraintUnifier");
                }
                mapBuilder.put(varToRename,(VariableOrGroundTerm) newTerm);
            }
        }

        return new ImmutableSubstitutionImpl<>(mapBuilder.build());
    }

    /**
     * TODO: explain
     */
    private static Optional<ImmutableQueryModifiers> updateOptionalModifiers(
            Optional<ImmutableQueryModifiers> optionalModifiers,
            ImmutableSubstitution<VariableOrGroundTerm> substitution1,
            ImmutableSubstitution<ImmutableTerm> substitution2) {
        if (!optionalModifiers.isPresent()) {
            return Optional.absent();
        }
        ImmutableQueryModifiers previousModifiers = optionalModifiers.get();
        ImmutableList.Builder<OrderCondition> conditionBuilder = ImmutableList.builder();

        for (OrderCondition condition : previousModifiers.getSortConditions()) {
            ImmutableTerm newTerm = substitution2.apply(substitution1.apply(condition.getVariable()));
            /**
             * If after applying the substitution the term is still a variable,
             * "updates" the OrderCondition.
             *
             * Otherwise, forgets it.
             */
            if (newTerm instanceof Variable) {
                conditionBuilder.add(condition.newVariable((Variable) newTerm));
            }
        }
        return previousModifiers.newSortConditions(conditionBuilder.build());
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
