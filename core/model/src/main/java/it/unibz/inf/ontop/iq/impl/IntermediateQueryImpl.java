package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.IllegalTreeException;
import it.unibz.inf.ontop.iq.impl.tree.QueryTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;

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
    public static class InconsistentIntermediateQueryException extends RuntimeException {
        public InconsistentIntermediateQueryException(String message) {
            super(message);
        }
    }


    /**
     * Highly mutable (low control) so MUST NOT BE SHARED (except with InternalProposalExecutor)!
     */
    private final QueryTree tree;

    private final DistinctVariableOnlyDataAtom projectionAtom;


    /**
     * For IntermediateQueryBuilders ONLY!!
     */
    public IntermediateQueryImpl(DistinctVariableOnlyDataAtom projectionAtom, QueryTree tree) {
        this.projectionAtom = projectionAtom;
        this.tree = tree;
    }

    @Override
    public DistinctVariableOnlyDataAtom getProjectionAtom() {
        return projectionAtom;
    }

    @Override
    public ImmutableSet<Variable> getVariables(QueryNode node) {
        if (node instanceof ExplicitVariableProjectionNode) {
            return ((ExplicitVariableProjectionNode) node).getVariables();
        }
        else {
            return getProjectedVariableStream(node)
                    .collect(ImmutableCollectors.toSet());
        }
    }

    private Stream<Variable> getProjectedVariableStream(QueryNode node) {
        if (node instanceof ExplicitVariableProjectionNode) {
            return ((ExplicitVariableProjectionNode) node).getVariables().stream();
        }
        else {
            return getChildren(node).stream()
                    .flatMap(this::getProjectedVariableStream);
        }
    }


    @Override
    public QueryNode getRootNode() throws InconsistentIntermediateQueryException{
        try {
            return tree.getRootNode();
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public ImmutableList<QueryNode> getNodesInTopDownOrder() {
        try {
            return tree.getNodesInTopDownOrder();
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public ImmutableList<QueryNode> getChildren(QueryNode node) {
        return tree.getChildren(node);
    }


    @Override
    public String toString() {
        return  getProjectionAtom() + "\n"
                + stringifySubTree(getRootNode(), "");
    }

    private static final String TAB_STR = "   ";

    /**
     * Recursive method.
     */
    private String stringifySubTree(QueryNode subTreeRoot, String rootOffsetString) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(rootOffsetString).append(subTreeRoot).append("\n");

        for (QueryNode child : getChildren(subTreeRoot)) {
            strBuilder.append(stringifySubTree(child, rootOffsetString + TAB_STR));
        }
        return strBuilder.toString();
    }

}
