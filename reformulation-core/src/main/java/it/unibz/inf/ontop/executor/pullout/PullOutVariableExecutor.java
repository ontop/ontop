package it.unibz.inf.ontop.executor.pullout;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.executor.substitution.SubstitutionPropagationTools;
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.FilterNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.PullOutVariableProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.*;


/**
 * TODO: explain
 *
 * TODO: complete (partially implemented)
 *
 */
public class PullOutVariableExecutor implements NodeCentricInternalExecutor<SubTreeDelimiterNode, PullOutVariableProposal> {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

    private static class VariableRenaming {
        public final Variable originalVariable;
        public final Variable newVariable;

        private VariableRenaming(Variable originalVariable, Variable newVariable) {
            this.originalVariable = originalVariable;
            this.newVariable = newVariable;
        }
    }

    private static class FocusNodeUpdate {
        public final SubTreeDelimiterNode newFocusNode;
        public final Optional<InjectiveVar2VarSubstitution> optionalSubstitution;
        public final ImmutableBooleanExpression newEqualities;

        private FocusNodeUpdate(SubTreeDelimiterNode newFocusNode, Optional<InjectiveVar2VarSubstitution> optionalSubstitution,
                                ImmutableBooleanExpression newEqualities) {
            this.newFocusNode = newFocusNode;
            this.optionalSubstitution = optionalSubstitution;
            this.newEqualities = newEqualities;
        }
    }

    @Override
    public NodeCentricOptimizationResults<SubTreeDelimiterNode> apply(PullOutVariableProposal proposal,
                                                                      IntermediateQuery query,
                                                                      QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        try {
            return pullOut(proposal, query, treeComponent);
        } catch (NotNeededNodeException e) {
            throw new RuntimeException("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * TODO: explain
     */
    private NodeCentricOptimizationResults<SubTreeDelimiterNode> pullOut(PullOutVariableProposal proposal,
                                                                         IntermediateQuery query,
                                                                         QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, IllegalTreeUpdateException, QueryNodeTransformationException,
            NotNeededNodeException, QueryNodeSubstitutionException {
        SubTreeDelimiterNode originalFocusNode = proposal.getFocusNode();
        ImmutableMap<Integer, VariableRenaming> renamingMap = generateRenamingMap(originalFocusNode, proposal.getIndexes(),
                query);

        FocusNodeUpdate focusNodeUpdate = generateNewFocusNodeAndSubstitution(originalFocusNode, renamingMap);

        treeComponent.replaceNode(originalFocusNode, focusNodeUpdate.newFocusNode);

        propagateUpNewEqualities(treeComponent, focusNodeUpdate.newFocusNode, focusNodeUpdate.newEqualities);

        if (focusNodeUpdate.optionalSubstitution.isPresent()) {
            SubstitutionPropagationTools.propagateSubstitutionDown(focusNodeUpdate.newFocusNode, focusNodeUpdate.optionalSubstitution.get(), treeComponent);
        }

        return new NodeCentricOptimizationResultsImpl<>(query, focusNodeUpdate.newFocusNode);
    }

    /**
     * TODO: explain
     *
     * TODO: make this code more better by not relying that much on instance checking!
     *
     */
    private void propagateUpNewEqualities(QueryTreeComponent treeComponent,
                                          SubTreeDelimiterNode newFocusNode, ImmutableBooleanExpression newEqualities)
            throws IllegalTreeUpdateException, InvalidQueryOptimizationProposalException {

        QueryNode lastChildNode = newFocusNode;
        Optional<QueryNode> optionalAncestorNode = treeComponent.getParent(newFocusNode);

        while (optionalAncestorNode.isPresent()) {
            QueryNode ancestorNode = optionalAncestorNode.get();

            if (ancestorNode instanceof CommutativeJoinNode) {
                updateNewJoinLikeNode(treeComponent, (CommutativeJoinNode) ancestorNode, newEqualities);
                return;
            }
            else if (ancestorNode instanceof FilterNode) {
                FilterNode originalFilterNode = (FilterNode) ancestorNode;

                ImmutableBooleanExpression newFilteringCondition = ImmutabilityTools.foldBooleanExpressions(
                        originalFilterNode.getFilterCondition(), newEqualities).get();

                FilterNode newFilterNode = originalFilterNode.changeFilterCondition(newFilteringCondition);
                treeComponent.replaceNode(originalFilterNode, newFilterNode);
                return;
            }
            else if (ancestorNode instanceof LeftJoinNode) {
                NonCommutativeOperatorNode.ArgumentPosition position =
                        treeComponent.getOptionalPosition(ancestorNode, lastChildNode).get();
                switch (position) {
                    case LEFT:
                        insertFilterNode(treeComponent, lastChildNode, newEqualities);
                        return;

                    case RIGHT:
                        updateNewJoinLikeNode(treeComponent, (LeftJoinNode) ancestorNode, newEqualities);
                        return;
                }

            }
            else if (ancestorNode instanceof ConstructionNode) {
                insertFilterNode(treeComponent, lastChildNode, newEqualities);
                return;

            }
            /**
             * Continues to the next ancestor
             */
            else if (ancestorNode instanceof GroupNode) {
                optionalAncestorNode = treeComponent.getParent(ancestorNode);
                lastChildNode = ancestorNode;
            }
            else {
                throw new RuntimeException("Unsupported ancestor node : " + ancestorNode);
            }
        }

        throw new InvalidQueryOptimizationProposalException("A PullOutVariableProposal cannot be applied to the root");

    }

    /**
     * TODO: explain
     */
    private static void updateNewJoinLikeNode(QueryTreeComponent treeComponent, JoinLikeNode originalNode,
                                              ImmutableBooleanExpression newEqualities) {

        Optional<ImmutableBooleanExpression> optionalOriginalFilterCondition = originalNode.getOptionalFilterCondition();
        ImmutableBooleanExpression newFilteringCondition;
        if (optionalOriginalFilterCondition.isPresent()) {
            newFilteringCondition = ImmutabilityTools.foldBooleanExpressions(optionalOriginalFilterCondition.get(),
                    newEqualities).get();
        }
        else {
            newFilteringCondition = newEqualities;
        }

        JoinLikeNode newNode = originalNode.changeOptionalFilterCondition(Optional.of(newFilteringCondition));
        treeComponent.replaceNode(originalNode, newNode);
    }

    /**
     * TODO: explain
     */
    private static void insertFilterNode(QueryTreeComponent treeComponent, QueryNode child,
                                         ImmutableBooleanExpression newEqualities) throws IllegalTreeUpdateException {
        FilterNode newFilterNode = new FilterNodeImpl(newEqualities);
        treeComponent.insertParent(child, newFilterNode);
    }



    /**
     * TODO: explain
     */
    private ImmutableMap<Integer, VariableRenaming> generateRenamingMap(SubTreeDelimiterNode focusNode,
                                                                        ImmutableList<Integer> indexes,
                                                                        IntermediateQuery query)
            throws InvalidQueryOptimizationProposalException {
        ImmutableMap.Builder<Integer, VariableRenaming> mapBuilder = ImmutableMap.builder();
        ImmutableList<? extends VariableOrGroundTerm> arguments = focusNode.getProjectionAtom().getArguments();

        for (Integer index : indexes) {
            VariableOrGroundTerm argument = arguments.get(index);
            if (argument instanceof Variable) {
                Variable formerVariable = (Variable) argument;
                Variable newVariable = query.generateNewVariable(formerVariable);
                mapBuilder.put(index, new VariableRenaming(formerVariable, newVariable));
            }
            else {
                throw new InvalidQueryOptimizationProposalException("The argument at the index "+ index
                        + " is not a variable!");
            }
        }

        return mapBuilder.build();
    }

    /**
     * TODO: explain.
     *
     * Can be overloaded.
     */
    protected FocusNodeUpdate generateNewFocusNodeAndSubstitution(SubTreeDelimiterNode originalFocusNode,
                                                                  ImmutableMap<Integer, VariableRenaming> renamingMap)
            throws QueryNodeTransformationException, NotNeededNodeException, QueryNodeSubstitutionException {

        if (originalFocusNode instanceof ConstructionNode) {
            return generateUpdate4ConstructionNode((ConstructionNode) originalFocusNode, renamingMap);
        }
        else if (originalFocusNode instanceof DataNode) {
            return generateUpdate4DataNode((DataNode) originalFocusNode, renamingMap);
        }
        else if (originalFocusNode instanceof DelimiterCommutativeJoinNode) {
            return generateUpdate4DelimiterCommutativeJoinNode((DelimiterCommutativeJoinNode) originalFocusNode, renamingMap);
        }
        else {
            throw new RuntimeException("Unsupported type of SubtreeDelimiterNode: " + originalFocusNode.getClass());
        }

    }

    /**
     * TODO: implement it
     */
    private FocusNodeUpdate generateUpdate4ConstructionNode(ConstructionNode originalConstructionNode,
                                                            ImmutableMap<Integer, VariableRenaming> renamingMap) {
        throw new RuntimeException("TODO: support pulling variables out of construction nodes");
    }

    /**
     * TODO: explain
     */
    private FocusNodeUpdate generateUpdate4DataNode(DataNode originalDataNode, ImmutableMap<Integer, VariableRenaming> renamingMap) {

        DataAtom newAtom = generateNewStandardDataAtom(originalDataNode, renamingMap);
        DataNode newDataNode = originalDataNode.newAtom(newAtom);

        return new FocusNodeUpdate(newDataNode, Optional.<InjectiveVar2VarSubstitution>empty(),
                convertIntoEqualities(renamingMap));
    }

    private ImmutableBooleanExpression convertIntoEqualities(ImmutableMap<Integer, VariableRenaming> renamingMap) {
        if (renamingMap.isEmpty()) {
            throw new IllegalArgumentException("The renaming map must not be empty");
        }

        ImmutableList.Builder<ImmutableBooleanExpression> equalityBuilder = ImmutableList.builder();
        for (VariableRenaming renaming : renamingMap.values()) {
            equalityBuilder.add(DATA_FACTORY.getImmutableBooleanExpression(ExpressionOperation.EQ,
                    renaming.originalVariable, renaming.newVariable));
        }
        return ImmutabilityTools.foldBooleanExpressions(equalityBuilder.build()).get();
    }

    /**
     * TODO: explain
     */
    private DataAtom generateNewStandardDataAtom(SubTreeDelimiterNode originalFocusNode,
                                                 ImmutableMap<Integer, VariableRenaming> renamingMap) {
        DataAtom formerAtom = originalFocusNode.getProjectionAtom();
        ImmutableList<? extends VariableOrGroundTerm> formerArguments = formerAtom.getArguments();

        ImmutableList.Builder<VariableOrGroundTerm> newArgumentBuilder = ImmutableList.builder();

        for (int i = 0; i < formerArguments.size(); i++) {
            if (renamingMap.containsKey(i)) {
                VariableRenaming variableRenaming = renamingMap.get(i);
                newArgumentBuilder.add(variableRenaming.newVariable);
            }
            else {
                newArgumentBuilder.add(formerArguments.get(i));
            }
        }
        return DATA_FACTORY.getDataAtom(formerAtom.getPredicate(), newArgumentBuilder.build());
    }

    /**
     * TODO: explain
     */
    private FocusNodeUpdate generateUpdate4DelimiterCommutativeJoinNode(DelimiterCommutativeJoinNode originalFocusNode,
                                                                        ImmutableMap<Integer, VariableRenaming> renamingMap)
            throws QueryNodeSubstitutionException {

        /**
         * Generates an injective substitution to be propagated
         */
        ImmutableMap.Builder<Variable, Variable> variableBuilder = ImmutableMap.builder();
        for (VariableRenaming renaming : renamingMap.values()) {
            variableBuilder.put(renaming.originalVariable, renaming.newVariable);
        }
        InjectiveVar2VarSubstitution substitution = new InjectiveVar2VarSubstitutionImpl(variableBuilder.build());

        SubstitutionResults<? extends DelimiterCommutativeJoinNode> substitutionResults =
                originalFocusNode.applyDescendentSubstitution(substitution);

        Optional<? extends DelimiterCommutativeJoinNode> optionalNewFocusNode = substitutionResults.getOptionalNewNode();
        Optional<? extends ImmutableSubstitution<? extends VariableOrGroundTerm>> optionalNewSubstitution =
                substitutionResults.getSubstitutionToPropagate();

        if (!optionalNewFocusNode.isPresent()) {
            throw new IllegalStateException("A DelimiterCommutativeJoinNode should remain needed " +
                    "after applying a substitution");
        }
        else if ((!optionalNewSubstitution.isPresent()) || (!substitution.equals(optionalNewSubstitution.get()))) {
            throw new IllegalStateException("This var-2-var substitution is not expected to be changed" +
                    "after being applied to a DelimiterCommutativeJoinNode.");
        }

        return new FocusNodeUpdate(optionalNewFocusNode.get(), Optional.of(substitution),
                convertIntoEqualities(renamingMap));
    }


}
