package it.unibz.inf.ontop.iq.tools.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.concurrent.atomic.AtomicInteger;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

@Singleton
public class IQConverterImpl implements IQConverter {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private IQConverterImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ convert(IntermediateQuery query) {
        IQTree tree = convertTree(query, query.getRootNode());
        return iqFactory.createIQ(query.getProjectionAtom(), tree);
    }

    /**
     * Recursive
     */
    @Override
    public IQTree convertTree(IntermediateQuery query, QueryNode rootNode) {
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


    @Override
    public IntermediateQuery convert(IQ query) throws EmptyQueryException {
        if (query.getTree().isDeclaredAsEmpty())
            throw new EmptyQueryException();

        IntermediateQueryBuilder queryBuilder = iqFactory.createIQBuilder();
        IQTree topTree = query.getTree();
        QueryNode rootNode = topTree.getRootNode();
        queryBuilder.init(query.getProjectionAtom(), rootNode);

        insertChildren(rootNode, topTree.getChildren(), queryBuilder);

        return queryBuilder.build();
    }

    /**
     * Recursive
     */
    private void insertChildren(QueryNode parentNode, ImmutableList<IQTree> childrenTrees,
                                IntermediateQueryBuilder queryBuilder) {
        AtomicInteger i = new AtomicInteger(0);
        childrenTrees.stream()
                .map(IQTree::getRootNode)
                .map(n -> queryBuilder.contains(n) ? n.clone() : n)
                .forEach(t -> insertChildTree(parentNode, t, childrenTrees.get(i.get()), queryBuilder, i.getAndIncrement()));
    }

    private void insertChildTree(QueryNode parent, QueryNode child, IQTree childTree, IntermediateQueryBuilder queryBuilder, int position) {
        if (parent instanceof BinaryOrderedOperatorNode) {
            queryBuilder.addChild(parent, child, (position == 0)? LEFT : RIGHT);
        } else {
            queryBuilder.addChild(parent, child);
        }
        insertChildren(child, childTree.getChildren(), queryBuilder);
    }
}
