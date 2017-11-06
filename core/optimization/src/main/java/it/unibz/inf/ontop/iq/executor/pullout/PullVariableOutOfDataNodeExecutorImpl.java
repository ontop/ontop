package it.unibz.inf.ontop.iq.executor.pullout;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.executor.substitution.DescendingPropagationTools;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeSubstitutionException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.PullVariableOutOfDataNodeProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Optional;


/**
 * TODO: explain
 *
 * TODO: UPDATE!!!!! (remove the logic related to ConstructionNode and other kind of nodes)
 *
 * TODO: complete (partially implemented)
 *
 */
@Singleton
public class PullVariableOutOfDataNodeExecutorImpl implements PullVariableOutOfDataNodeExecutor {

    private static class VariableRenaming {
        public final Variable originalVariable;
        public final Variable newVariable;

        private VariableRenaming(Variable originalVariable, Variable newVariable) {
            this.originalVariable = originalVariable;
            this.newVariable = newVariable;
        }
    }

    private static class FocusNodeUpdate {
        public final DataNode newFocusNode;
        public final Optional<InjectiveVar2VarSubstitution> optionalSubstitution;
        public final ImmutableExpression newEqualities;

        private FocusNodeUpdate(DataNode newFocusNode, Optional<InjectiveVar2VarSubstitution> optionalSubstitution,
                                ImmutableExpression newEqualities) {
            this.newFocusNode = newFocusNode;
            this.optionalSubstitution = optionalSubstitution;
            this.newEqualities = newEqualities;
        }
    }

    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    private PullVariableOutOfDataNodeExecutorImpl(IntermediateQueryFactory iqFactory, AtomFactory atomFactory,
                                                  TermFactory termFactory, ImmutabilityTools immutabilityTools) {
        this.iqFactory = iqFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.immutabilityTools = immutabilityTools;
    }

    @Override
    public NodeCentricOptimizationResults<DataNode> apply(PullVariableOutOfDataNodeProposal proposal,
                                                                      IntermediateQuery query,
                                                                      QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        return pullOut(proposal, query, treeComponent);
    }

    /**
     * TODO: explain
     */
    private NodeCentricOptimizationResults<DataNode> pullOut(PullVariableOutOfDataNodeProposal proposal,
                                                                         IntermediateQuery query,
                                                                         QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, IllegalTreeUpdateException, QueryNodeTransformationException,
            QueryNodeSubstitutionException {
        DataNode originalFocusNode = proposal.getFocusNode();
        ImmutableMap<Integer, VariableRenaming> renamingMap = generateRenamingMap(originalFocusNode, proposal.getIndexes(),
                query);

        FocusNodeUpdate focusNodeUpdate = generateNewFocusNodeAndSubstitution(originalFocusNode, renamingMap);

        treeComponent.replaceNode(originalFocusNode, focusNodeUpdate.newFocusNode);

        QueryNode newNode = propagateUpNewEqualities(treeComponent, focusNodeUpdate.newFocusNode,
                focusNodeUpdate.newEqualities);

        if (focusNodeUpdate.optionalSubstitution.isPresent()) {
            try {
                DescendingPropagationTools.propagateSubstitutionDown(focusNodeUpdate.newFocusNode,
                        focusNodeUpdate.optionalSubstitution.get(), query, treeComponent);
            } catch (EmptyQueryException e) {
                throw new IllegalStateException("EmptyQueryExceptions are not expected when pulling the variables out of data nodes");
            }
        }

        // return new NodeCentricOptimizationResultsImpl<>(query, focusNodeUpdate.newFocusNode);
        return new NodeCentricOptimizationResultsImpl<>(query, query.getNextSibling(newNode),
                query.getParent(newNode));
    }

    /**
     * TODO: explain
     *
     * TODO: make this code more better by not relying that much on instance checking!
     *
     */
    private QueryNode propagateUpNewEqualities(QueryTreeComponent treeComponent,
                                          QueryNode newFocusNode, ImmutableExpression newEqualities)
            throws IllegalTreeUpdateException, InvalidQueryOptimizationProposalException {

        QueryNode lastChildNode = newFocusNode;
        Optional<QueryNode> optionalAncestorNode = treeComponent.getParent(newFocusNode);

        while (optionalAncestorNode.isPresent()) {
            QueryNode ancestorNode = optionalAncestorNode.get();

            if (ancestorNode instanceof CommutativeJoinNode) {
                updateNewJoinLikeNode(treeComponent, (CommutativeJoinNode) ancestorNode, newEqualities);
                return newFocusNode;
            }
            else if (ancestorNode instanceof FilterNode) {
                FilterNode originalFilterNode = (FilterNode) ancestorNode;

                ImmutableExpression newFilteringCondition = immutabilityTools.foldBooleanExpressions(
                        originalFilterNode.getFilterCondition(), newEqualities).get();

                FilterNode newFilterNode = originalFilterNode.changeFilterCondition(newFilteringCondition);
                treeComponent.replaceNode(originalFilterNode, newFilterNode);
                return newFocusNode;
            }
            else if (ancestorNode instanceof LeftJoinNode) {
                BinaryOrderedOperatorNode.ArgumentPosition position =
                        treeComponent.getOptionalPosition(ancestorNode, lastChildNode).get();
                switch (position) {
                    case LEFT:
                        return insertFilterNode(treeComponent, lastChildNode, newEqualities);

                    case RIGHT:
                        updateNewJoinLikeNode(treeComponent, (LeftJoinNode) ancestorNode, newEqualities);
                        return newFocusNode;
                }

            }
            else if (ancestorNode instanceof ConstructionNode || ancestorNode instanceof UnionNode) {
                return insertFilterNode(treeComponent, lastChildNode, newEqualities);
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
    private void updateNewJoinLikeNode(QueryTreeComponent treeComponent, JoinLikeNode originalNode,
                                              ImmutableExpression newEqualities) {

        Optional<ImmutableExpression> optionalOriginalFilterCondition = originalNode.getOptionalFilterCondition();
        ImmutableExpression newFilteringCondition;
        if (optionalOriginalFilterCondition.isPresent()) {
            newFilteringCondition = immutabilityTools.foldBooleanExpressions(optionalOriginalFilterCondition.get(),
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
    private QueryNode insertFilterNode(QueryTreeComponent treeComponent, QueryNode child,
                                         ImmutableExpression newEqualities) throws IllegalTreeUpdateException {
        FilterNode newFilterNode = iqFactory.createFilterNode(newEqualities);
        treeComponent.insertParent(child, newFilterNode);
        return  newFilterNode;
    }



    /**
     * TODO: explain
     */
    private ImmutableMap<Integer, VariableRenaming> generateRenamingMap(DataNode focusNode,
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
    protected FocusNodeUpdate generateNewFocusNodeAndSubstitution(DataNode originalFocusNode,
                                                                  ImmutableMap<Integer, VariableRenaming> renamingMap)
            throws QueryNodeTransformationException, QueryNodeSubstitutionException {

        if (originalFocusNode instanceof ConstructionNode) {
            return generateUpdate4ConstructionNode((ConstructionNode) originalFocusNode, renamingMap);
        }
        else if (originalFocusNode instanceof DataNode) {
            return generateUpdate4DataNode((DataNode) originalFocusNode, renamingMap);
        }
//        else if (originalFocusNode instanceof DelimiterCommutativeJoinNode) {
//            return generateUpdate4DelimiterCommutativeJoinNode((DelimiterCommutativeJoinNode) originalFocusNode, renamingMap);
//        }
        else {
            throw new RuntimeException("Unsupported type of DataNode: " + originalFocusNode.getClass());
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

    private ImmutableExpression convertIntoEqualities(ImmutableMap<Integer, VariableRenaming> renamingMap) {
        if (renamingMap.isEmpty()) {
            throw new IllegalArgumentException("The renaming map must not be empty");
        }

        ImmutableList.Builder<ImmutableExpression> equalityBuilder = ImmutableList.builder();
        for (VariableRenaming renaming : renamingMap.values()) {
            equalityBuilder.add(termFactory.getImmutableExpression(ExpressionOperation.EQ,
                    renaming.originalVariable, renaming.newVariable));
        }
        return immutabilityTools.foldBooleanExpressions(equalityBuilder.build()).get();
    }

    /**
     * TODO: explain
     */
    private DataAtom generateNewStandardDataAtom(DataNode originalFocusNode,
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
        return atomFactory.getDataAtom(formerAtom.getPredicate(), newArgumentBuilder.build());
    }

    /**
     * TODO: explain
     */
//    private FocusNodeUpdate generateUpdate4DelimiterCommutativeJoinNode(DelimiterCommutativeJoinNode originalFocusNode,
//                                                                        ImmutableMap<Integer, VariableRenaming> renamingMap)
//            throws QueryNodeSubstitutionException {
//
//        /**
//         * Generates an injective substitution to be propagated
//         */
//        ImmutableMap.Builder<Variable, Variable> variableBuilder = ImmutableMap.builder();
//        for (VariableRenaming renaming : renamingMap.values()) {
//            variableBuilder.put(renaming.originalVariable, renaming.newVariable);
//        }
//        InjectiveVar2VarSubstitution substitution = new InjectiveVar2VarSubstitutionImpl(variableBuilder.build());
//
//        SubstitutionResults<? extends DelimiterCommutativeJoinNode> substitutionResults =
//                originalFocusNode.applyDescendingSubstitution(substitution);
//
//        Optional<? extends DelimiterCommutativeJoinNode> optionalNewFocusNode = substitutionResults.getOptionalNewNode();
//        Optional<? extends ImmutableSubstitution<? extends VariableOrGroundTerm>> optionalNewSubstitution =
//                substitutionResults.getSubstitutionToPropagate();
//
//        if (!optionalNewFocusNode.isPresent()) {
//            throw new IllegalStateException("A DelimiterCommutativeJoinNode should remain needed " +
//                    "after applying a substitution");
//        }
//        else if ((!optionalNewSubstitution.isPresent()) || (!substitution.equals(optionalNewSubstitution.get()))) {
//            throw new IllegalStateException("This var-2-var substitution is not expected to be changed" +
//                    "after being applied to a DelimiterCommutativeJoinNode.");
//        }
//
//        return new FocusNodeUpdate(optionalNewFocusNode.get(), Optional.of(substitution),
//                convertIntoEqualities(renamingMap));
//    }


}
