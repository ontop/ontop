package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.IllegalTreeException;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.validation.IntermediateQueryValidator;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;

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
     * TODO: use Guice to replace it.
     */
    private static final IntermediateQueryPrinter PRINTER = new BasicQueryTreePrinter();


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
            validate();
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


    @Override
    public Optional<QueryNode> getChild(QueryNode currentNode, BinaryOrderedOperatorNode.ArgumentPosition position) {
        return getChildren(currentNode).stream()
                .filter(c -> getOptionalPosition(currentNode, c)
                        .filter(position::equals)
                        .isPresent())
                .findFirst();
    }


    @Override
    public Optional<BinaryOrderedOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode,
                                                                                    QueryNode childNode) {
        return treeComponent.getOptionalPosition(parentNode, childNode);
    }


    @Override
    public Optional<QueryNode> getFirstChild(QueryNode node) {
        return treeComponent.getFirstChild(node);
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
        return PRINTER.stringify(this);
    }

    private void validate() throws InvalidIntermediateQueryException {
        validator.validate(this);
    }
}
