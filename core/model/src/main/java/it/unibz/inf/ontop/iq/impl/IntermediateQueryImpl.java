package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.IllegalTreeException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.validation.IntermediateQueryValidator;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;

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
    private final QueryTreeComponent treeComponent;

    private final DistinctVariableOnlyDataAtom projectionAtom;

    private final IntermediateQueryValidator validator;

    private final OntopModelSettings settings;

    private final IntermediateQueryFactory iqFactory;


    /**
     * For IntermediateQueryBuilders ONLY!!
     */
    public IntermediateQueryImpl(DistinctVariableOnlyDataAtom projectionAtom,
                                 QueryTreeComponent treeComponent,
                                 IntermediateQueryValidator validator, OntopModelSettings settings,
                                 IntermediateQueryFactory iqFactory) {
        this.projectionAtom = projectionAtom;
        this.treeComponent = treeComponent;
        this.validator = validator;
        this.settings = settings;
        this.iqFactory = iqFactory;

        if (settings.isTestModeEnabled())
            this.validator.validate(this);
    }

    @Override
    public DistinctVariableOnlyDataAtom getProjectionAtom() {
        return projectionAtom;
    }

    @Override
    public ImmutableSet<Variable> getVariables(QueryNode subTreeRootNode) {
        return treeComponent.getVariables(subTreeRootNode);
    }


    @Override
    public QueryNode getRootNode() throws InconsistentIntermediateQueryException{
        try {
            return treeComponent.getRootNode();
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public ImmutableList<QueryNode> getNodesInTopDownOrder() {
        try {
            return treeComponent.getNodesInTopDownOrder();
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public ImmutableList<QueryNode> getChildren(QueryNode node) {
        return treeComponent.getChildren(node);
    }


    /**
     * Not appearing in the interface because users do not
     * have to worry about it.
     */
    @Override
    public IntermediateQuery clone() {
        return new IntermediateQueryImpl(projectionAtom, treeComponent.createSnapshot(),
                validator, settings, iqFactory);
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
