package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

public class IQConverterImpl {

    private final IntermediateQueryFactory iqFactory;

    IQConverterImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    IQ convert(IntermediateQuery query) {
        IQTree tree = convertTree(query, query.getRootNode());
        return iqFactory.createIQ(query.getProjectionAtom(), tree);
    }

    /**
     * Recursive
     */
    private IQTree convertTree(IntermediateQuery query, QueryNode rootNode) {
        if (rootNode instanceof LeafIQTree) {
            return (LeafIQTree) rootNode;
        }
        else if (rootNode instanceof UnaryOperatorNode) {
            // Recursive
            IQTree childTree = convertTree(query, query.getFirstChild(rootNode).get());
            return iqFactory.createUnaryIQTree((UnaryOperatorNode) rootNode, childTree);
        }
        else if (rootNode instanceof BinaryNonCommutativeOperatorNode) {
            IQTree leftChildTree = convertTree(query, query.getChild(rootNode, LEFT).get());
            IQTree rightChildTree = convertTree(query, query.getChild(rootNode, RIGHT).get());

            return iqFactory.createBinaryNonCommutativeIQTree((BinaryNonCommutativeOperatorNode) rootNode,
                    leftChildTree, rightChildTree);
        }
        else if (rootNode instanceof NaryOperatorNode) {
            ImmutableList<IQTree> childTrees = query.getChildren(rootNode).stream()
                    .map(c -> convertTree(query, c))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree((NaryOperatorNode) rootNode, childTrees);
        }

        throw new MinorOntopInternalBugException("Unexpected type of query node: " + rootNode);
    }
}
