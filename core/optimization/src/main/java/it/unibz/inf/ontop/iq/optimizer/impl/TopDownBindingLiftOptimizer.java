package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.BindingExtractor;
import it.unibz.inf.ontop.iq.optimizer.BindingLiftOptimizer;
import it.unibz.inf.ontop.iq.optimizer.TrueNodesRemovalOptimizer;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.optimizer.impl.QueryNodeNavigationTools.NextNodeAndQuery;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.iq.proposal.UnionLiftProposal;
import it.unibz.inf.ontop.iq.proposal.impl.SubstitutionPropagationProposalImpl;
import it.unibz.inf.ontop.iq.proposal.impl.UnionLiftProposalImpl;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static it.unibz.inf.ontop.iq.optimizer.impl.QueryNodeNavigationTools.getDepthFirstNextNode;
import static it.unibz.inf.ontop.iq.optimizer.impl.QueryNodeNavigationTools.getNextNodeAndQuery;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

/**
 * Optimizer to extract and propagate bindings in the query up and down the tree.
 * Uses {@link UnionFriendlyBindingExtractor}, {@link SubstitutionPropagationProposal} and {@link UnionLiftProposal}
 *
 */
@Singleton
public class TopDownBindingLiftOptimizer implements BindingLiftOptimizer {

    private final SimpleUnionNodeLifter lifter;
    private final UnionFriendlyBindingExtractor extractor;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    public TopDownBindingLiftOptimizer(SubstitutionFactory substitutionFactory,
                                       UnionFriendlyBindingExtractor extractor) {
        this.substitutionFactory = substitutionFactory;
        this.lifter = new SimpleUnionNodeLifter();
        this.extractor = extractor;
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        // Non-final
        NextNodeAndQuery nextNodeAndQuery = new NextNodeAndQuery(
                Optional.of(query.getRootNode()),
                query);

        //explore the tree lifting the bindings when it is possible
        while (nextNodeAndQuery.getOptionalNextNode().isPresent()) {
            nextNodeAndQuery = liftBindings(nextNodeAndQuery.getNextQuery(),
                    nextNodeAndQuery.getOptionalNextNode().get());

        }

        // remove unnecessary TrueNodes, which may have been introduced during substitution lift
        return new TrueNodesRemovalOptimizer().optimize(nextNodeAndQuery.getNextQuery());
    }

    private NextNodeAndQuery liftBindings(IntermediateQuery currentQuery, QueryNode currentNode)
            throws EmptyQueryException {

        if (currentNode instanceof ConstructionNode) {
            return liftBindingsFromConstructionNode(currentQuery, (ConstructionNode) currentNode);
        }
        else if (currentNode instanceof CommutativeJoinNode) {
            return liftBindingsFromCommutativeJoinNode(currentQuery, (CommutativeJoinNode) currentNode);
        }
        else if (currentNode instanceof LeftJoinNode) {
            return liftBindingsFromLeftJoinNode(currentQuery, (LeftJoinNode) currentNode);
        }
        else if (currentNode instanceof UnionNode) {
            return liftBindingsAndUnion(currentQuery, (UnionNode) currentNode);
        }
        /**
         * Other nodes: does nothing
         */
        else {
            return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentNode), currentQuery);
        }
    }

    /* Lift the bindings of the union to see if it is possible to simplify the tree.
      Otherwise try to lift the union to an ancestor with useful projected variables between its children
      (common with the conflicting bindings of the union).
      */
    private NextNodeAndQuery liftBindingsAndUnion(IntermediateQuery currentQuery, UnionNode initialUnionNode) throws EmptyQueryException {

        Optional<UnionNode> unionNode = Optional.of(initialUnionNode) ;
        QueryNode currentNode = initialUnionNode;


        //extract bindings (liftable bindings and conflicting one) from the union node
        final BindingExtractor.Extraction extraction = extractor.extractInSubTree(
                currentQuery, currentNode);

        //get liftable bindings
        Optional<ImmutableSubstitution<ImmutableTerm>> optionalSubstitution = extraction.getOptionalSubstitution();

        if (optionalSubstitution.isPresent()) {

            //try to lift the bindings up and down the tree
            SubstitutionPropagationProposal<UnionNode> proposal =
                    new SubstitutionPropagationProposalImpl<>(initialUnionNode, optionalSubstitution.get());

            NodeCentricOptimizationResults<UnionNode> results = currentQuery.applyProposal(proposal);
            unionNode = results.getOptionalNewNode();
            currentNode = results.getNewNodeOrReplacingChild()
                    .orElseThrow(() -> new IllegalStateException(
                            "The focus was expected to be kept or replaced, not removed"));

        }

        //if the union node has not been removed
        if (unionNode.isPresent()) {

            //variables of bindings that could not be returned because conflicting or not common in the subtree
            ImmutableSet<Variable> irregularVariables = extraction.getVariablesWithConflictingBindings();

            if(!irregularVariables.isEmpty()) {

                //try to lift the union
                return liftUnionToMatchingVariable(currentQuery, unionNode.get(), irregularVariables);
            }

        }

        return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentNode), currentQuery);


    }


    /*  Lift the union to an ancestor with useful projected variables between its children,
       These variables are common with the bindings of the union. */

    private NextNodeAndQuery liftUnionToMatchingVariable(IntermediateQuery currentQuery, UnionNode currentUnionNode, ImmutableSet<Variable> unionVariables) throws EmptyQueryException {


        Optional<QueryNode> parentNode = lifter.chooseLiftLevel(currentQuery, currentUnionNode, unionVariables);

        if(parentNode.isPresent()){

            UnionLiftProposal proposal = new UnionLiftProposalImpl(currentUnionNode, parentNode.get());
            NodeCentricOptimizationResults<UnionNode> results = currentQuery.applyProposal(proposal);
            currentUnionNode = results.getOptionalNewNode().orElseThrow(() -> new IllegalStateException(
                    "The focus node has to be a union node and be present"));

            return liftBindingsAndUnion(currentQuery, currentUnionNode);
        }

        //no parent with the given variable, I don't lift for the moment

        return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentUnionNode), currentQuery);



    }


    private NextNodeAndQuery liftBindingsFromConstructionNode(IntermediateQuery currentQuery,
                                                              ConstructionNode currentConstructionNode)
            throws EmptyQueryException {

        Optional<QueryNode> parentNode = currentQuery.getParent(currentConstructionNode);
        if(parentNode.isPresent()){
            QueryNode parent = parentNode.get();
            /*
             * Union and LJ (on the right) block substitutions
             */
            if ((parent instanceof UnionNode)
                    || ((parent instanceof LeftJoinNode)
                        && currentQuery.getOptionalPosition(currentConstructionNode)
                            .filter(p -> p == RIGHT)
                            .isPresent())) {
                return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentConstructionNode), currentQuery);
            }
        }
        // Does not lift the root when it is a construction node
        else {
            return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentConstructionNode), currentQuery);
        }

        //extract substitution from the construction node
        Optional<ImmutableSubstitution<ImmutableTerm>> optionalSubstitution = extractor.extractInSubTree(
                currentQuery, currentConstructionNode).getOptionalSubstitution();

        //propagate substitution up and down
        if (optionalSubstitution.isPresent()) {
            SubstitutionPropagationProposal<QueryNode> proposal =
                    new SubstitutionPropagationProposalImpl<>(currentConstructionNode, optionalSubstitution.get());

            NodeCentricOptimizationResults<QueryNode> results = currentQuery.applyProposal(proposal);
            return getNextNodeAndQuery(currentQuery, results);


        }

        return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentConstructionNode), currentQuery);
    }


    /** Lift the bindings for the commutative join
     * I extract the bindings (from union and construction nodes ) iterating through its children
     * @param currentQuery
     * @param initialJoinNode
     * @return
     * @throws EmptyQueryException
     */

    private NextNodeAndQuery liftBindingsFromCommutativeJoinNode(IntermediateQuery currentQuery,
                                                                 CommutativeJoinNode initialJoinNode)
            throws EmptyQueryException {

        // Non-final
        Optional<QueryNode> optionalCurrentChild = currentQuery.getFirstChild(initialJoinNode);
        QueryNode currentJoinNode = initialJoinNode;


        while (optionalCurrentChild.isPresent()) {
            QueryNode currentChild = optionalCurrentChild.get();

            Optional<ImmutableSubstitution<ImmutableTerm>> optionalSubstitution = extractor.extractInSubTree(
                    currentQuery, currentChild).getOptionalSubstitution();

            /**
             * Applies the substitution to the child
             */
            if (optionalSubstitution.isPresent()) {
                SubstitutionPropagationProposal<QueryNode> proposal =
                        new SubstitutionPropagationProposalImpl<>(currentChild, optionalSubstitution.get());

                NodeCentricOptimizationResults<QueryNode> results = currentQuery.applyProposal(proposal);
                optionalCurrentChild = results.getOptionalNextSibling();
                Optional<QueryNode> currentNode = results.getNewNodeOrReplacingChild();

                //node has not been removed
                if(currentNode.isPresent()) {
                    currentJoinNode = currentQuery.getParent(
                            currentNode
                                    .orElseThrow(() -> new IllegalStateException(
                                            "The focus was expected to be kept or replaced, not removed")))
                            .orElseThrow(() -> new IllegalStateException(
                                    "The focus node should still have a parent (a Join node)"));
                }
                else {

                    return getNextNodeAndQuery(currentQuery, results);

                }

            }
            else {
                optionalCurrentChild = currentQuery.getNextSibling(currentChild);
            }
        }


        return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentJoinNode), currentQuery);

    }


    /** Lift bindings from left node extracting first bindings coming from the left side of its subtree.
     *
     * Does not lift the right part (TODO: improve this)
     *
     * @param currentQuery
     * @param initialLeftJoinNode
     * @return
     * @throws EmptyQueryException
     */
    private NextNodeAndQuery liftBindingsFromLeftJoinNode(IntermediateQuery currentQuery, LeftJoinNode initialLeftJoinNode) throws EmptyQueryException {

        QueryNode currentNode = initialLeftJoinNode;

        Optional<QueryNode> optionalLeftChild = currentQuery.getChild(currentNode, LEFT);


        //check bindings of the right side if there are some that are not projected in the second, they can be already pushed
        //substitution coming from the left have more importance than the one coming from the right
        if (optionalLeftChild.isPresent()) {

            Optional<ImmutableSubstitution<ImmutableTerm>> optionalSubstitution = extractor.extractInSubTree(
                    currentQuery, optionalLeftChild.get()).getOptionalSubstitution();

            /*
             * Applies the substitution to the join
             */
            if (optionalSubstitution.isPresent()) {
                SubstitutionPropagationProposal<LeftJoinNode> proposal =
                        new SubstitutionPropagationProposalImpl<>(initialLeftJoinNode, optionalSubstitution.get());

                NodeCentricOptimizationResults<LeftJoinNode> results = currentQuery.applyProposal(proposal);
                Optional<LeftJoinNode> currentJoinNode = results.getOptionalNewNode();

                if(currentJoinNode.isPresent()){
                    currentNode = currentJoinNode.get();
                }
                else{
                    return getNextNodeAndQuery(currentQuery, results);
                }


            }
        }
        else
            {
            throw new IllegalStateException("Left Join needs to have a left child");
        }

        return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentNode), currentQuery);


    }

}
