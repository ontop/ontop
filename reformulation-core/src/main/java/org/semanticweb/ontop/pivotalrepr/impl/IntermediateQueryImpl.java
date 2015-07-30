package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.executor.InternalProposalExecutor;
import org.semanticweb.ontop.executor.join.JoinBooleanExpressionExecutor;
import org.semanticweb.ontop.executor.renaming.PredicateRenamingExecutor;
import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.proposal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * TODO: describe
 *
 * BEWARE: this class has a non-trivial mutable internal state!
 */
public class IntermediateQueryImpl implements IntermediateQuery {

    /**
     * Thrown when the internal state of the intermediate query is found to be inconsistent.
     *
     * Should not be expected (internal error).
     *
     */
    protected static class InconsistentIntermediateQueryException extends RuntimeException {
        protected InconsistentIntermediateQueryException(String message) {
            super(message);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(IntermediateQueryImpl.class);

    /**
     * TODO: use Guice to replace it.
     */
    private static final IntermediateQueryPrinter PRINTER = new BasicQueryTreePrinter();


    /**
     * Highly mutable (low control) so MUST NOT BE SHARED (except with InternalProposalExecutor)!
     */
    private final QueryTreeComponent treeComponent;

    /**
     * TODO: explain
     */
    private static final ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends StandardProposalExecutor>> STD_EXECUTOR_CLASSES;
    static {
        STD_EXECUTOR_CLASSES = ImmutableMap.<Class<? extends QueryOptimizationProposal>, Class<? extends StandardProposalExecutor>>of(
                UnionLiftProposal.class, UnionLiftProposalExecutor.class,
                PredicateRenamingProposal.class, PredicateRenamingExecutor.class);
    }

    /**
     * TODO: explain
     */
    private static final ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>> INTERNAL_EXECUTOR_CLASSES;
    static {
        INTERNAL_EXECUTOR_CLASSES = ImmutableMap.<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>>of(
                SubstitutionLiftProposal.class, SubstitutionLiftProposalExecutor.class,
                InnerJoinOptimizationProposal.class, JoinBooleanExpressionExecutor.class);
    }


    /**
     * For IntermediateQueryBuilders ONLY!!
     */
    protected IntermediateQueryImpl(QueryTreeComponent treeComponent) {
        this.treeComponent = treeComponent;
    }

    @Override
    public ConstructionNode getRootConstructionNode() throws InconsistentIntermediateQueryException{
        try {
            return treeComponent.getRootConstructionNode();
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public ImmutableList<QueryNode> getNodesInBottomUpOrder() throws InconsistentIntermediateQueryException {
        try {
            return treeComponent.getNodesInBottomUpOrder();
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node) {
        return treeComponent.getCurrentSubNodesOf(node);
    }

    @Override
    public ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentNode) {
        return treeComponent.getSubTreeNodesInTopDownOrder(currentNode);
    }

    @Override
    public boolean contains(QueryNode node) {
        return treeComponent.contains(node);
    }

    /**
     * TODO: make this extensible by using Guice as a dependency-injection solution for loading arbitrary ProposalExecutor
     */
    @Override
    public IntermediateQuery applyProposal(QueryOptimizationProposal proposal)
            throws InvalidQueryOptimizationProposalException {

        /**
         * It assumes that the concrete proposal classes DIRECTLY
         * implements a registered interface (extending QueryOptimizationProposal).
         */
        Class<?>[] proposalClassHierarchy = proposal.getClass().getInterfaces();

        /**
         * First look for a standard executor
         */
        for (Class proposalClass : proposalClassHierarchy) {
            if (STD_EXECUTOR_CLASSES.containsKey(proposalClass)) {
                StandardProposalExecutor executor;
                try {
                    executor = STD_EXECUTOR_CLASSES.get(proposalClass).newInstance();
                } catch (InstantiationException | IllegalAccessException e ) {
                    throw new RuntimeException(e.getMessage());
                }
                return executor.apply(proposal, this);
            }
        }

        /**
         * Then, look for a internal one
         */
        for (Class proposalClass : proposalClassHierarchy) {
            if (INTERNAL_EXECUTOR_CLASSES.containsKey(proposalClass)) {
                InternalProposalExecutor executor;
                try {
                    executor = INTERNAL_EXECUTOR_CLASSES.get(proposalClass).newInstance();
                } catch (InstantiationException | IllegalAccessException e ) {
                    throw new RuntimeException(e.getMessage());
                }
                /**
                 * Has a SIDE-EFFECT on the tree component.
                 */
                executor.apply(proposal, this, treeComponent);
                return this;
            }
        }
        throw new RuntimeException("No executor found for a proposal of the type " + proposal.getClass());
    }

    @Override
    public Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode,
                                                                                      QueryNode childNode) {
        return treeComponent.getOptionalPosition(parentNode, childNode);
    }

    @Override
    public ImmutableList<QueryNode> getAncestors(QueryNode descendantNode) {
        try {
            return treeComponent.getAncestors(descendantNode);
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public Optional<QueryNode> getParent(QueryNode node) {
        try {
            return treeComponent.getParent(node);
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    /**
     * TODO: explain
     */
    @Override
    public void mergeSubQuery(final IntermediateQuery originalSubQuery) throws QueryMergingException {
        /**
         * TODO: explain
         */
        List<OrdinaryDataNode> localDataNodes = findOrdinaryDataNodes(originalSubQuery.getRootConstructionNode().getProjectionAtom());
        if (localDataNodes.isEmpty())
            throw new QueryMergingException("No OrdinaryDataNode matches " + originalSubQuery.getRootConstructionNode().getProjectionAtom());


        for (OrdinaryDataNode localDataNode : localDataNodes) {
            // TODO: make it be incremental
            ImmutableSet<VariableImpl> localVariables = VariableCollector.collectVariables(this);

            try {
                IntermediateQuery cloneSubQuery = SubQueryUnificationTools.unifySubQuery(originalSubQuery,
                            localDataNode.getAtom(), localVariables);

                ConstructionNode subQueryRootNode = cloneSubQuery.getRootConstructionNode();
                treeComponent.replaceNode(localDataNode, subQueryRootNode);

                treeComponent.addSubTree(cloneSubQuery, subQueryRootNode);
            } catch (SubQueryUnificationTools.SubQueryUnificationException e) {
                throw new QueryMergingException(e.getMessage());
            }
        }
    }

    /**
     * Finds ordinary data nodes.
     *
     * TODO: explain
     */
    private ImmutableList<OrdinaryDataNode> findOrdinaryDataNodes(DataAtom subsumingDataAtom)
            throws InconsistentIntermediateQueryException {
        ImmutableList.Builder<OrdinaryDataNode> listBuilder = ImmutableList.builder();
        try {
            for(QueryNode node : treeComponent.getNodesInBottomUpOrder()) {
                if (node instanceof OrdinaryDataNode) {
                    OrdinaryDataNode dataNode = (OrdinaryDataNode) node;
                    if (subsumingDataAtom.hasSamePredicateAndArity(dataNode.getAtom()))
                        listBuilder.add(dataNode);
                }
            }
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
        return listBuilder.build();
    }


    /**
     * Not appearing in the interface because users do not
     * have to worry about it.
     */
    @Override
    public IntermediateQuery clone() {
        try {
            return IntermediateQueryUtils.convertToBuilder(this).build();
        } catch (IntermediateQueryBuilderException e) {
            throw new RuntimeException("BUG (internal error)!" + e.getLocalizedMessage());
        }
    }

    @Override
    public String toString() {
        return PRINTER.stringify(this);
    }
}
