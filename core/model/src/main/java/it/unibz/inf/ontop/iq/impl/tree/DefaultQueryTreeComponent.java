package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.iq.exception.IllegalTreeException;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.tools.VariableCollector;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;


/**
 * TODO: describe
 *
 * Every time a node is added, please call the method collectPossiblyNewVariables()!
 */
public class DefaultQueryTreeComponent implements QueryTreeComponent {

    private final QueryTree tree;
    private final VariableGenerator variableGenerator;

    /**
     * TODO: explain
     */
    protected DefaultQueryTreeComponent(QueryTree tree, CoreUtilsFactory coreUtilsFactory) {
        this(tree,
                coreUtilsFactory.createVariableGenerator(
                        VariableCollector.collectVariables(
                                tree.getNodesInTopDownOrder())));
    }

    private DefaultQueryTreeComponent(QueryTree tree, VariableGenerator variableGenerator) {
        this.tree = tree;
        this.variableGenerator = variableGenerator;
    }

    @Override
    public ImmutableList<QueryNode> getChildren(QueryNode node) {
        return tree.getChildren(node);
    }

    @Override
    public Stream<QueryNode> getChildrenStream(QueryNode node) {
        return tree.getChildrenStream(node);
    }

    @Override
    public QueryNode getRootNode() {
        return tree.getRootNode();
    }

    @Override
    public ImmutableList<QueryNode> getNodesInTopDownOrder() throws IllegalTreeException {
        return tree.getNodesInTopDownOrder();
    }

    @Override
    public void addChild(QueryNode parentNode, QueryNode childNode,
                         Optional<ArgumentPosition> optionalPosition, boolean canReplace)
            throws IllegalTreeUpdateException {
        collectPossiblyNewVariables(childNode);
        tree.addChild(parentNode, childNode, optionalPosition, true, canReplace);
    }

    @Override
    public QueryTreeComponent createSnapshot() {
        return new DefaultQueryTreeComponent(tree.createSnapshot(), variableGenerator.createSnapshot());
    }

    /**
     * TODO: optimize by it but materializing (and maintaining) the results.
     */
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
            return getChildrenStream(node)
                    .flatMap(this::getProjectedVariableStream);
        }
    }


    /**
     * To be called every time a new node is added to the tree component.
     */
    private void collectPossiblyNewVariables(QueryNode newNode) {
        variableGenerator.registerAdditionalVariables(newNode.getLocalVariables());
    }
}
