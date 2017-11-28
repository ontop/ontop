package it.unibz.inf.ontop.iq.tools.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.*;

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


    @Override
    public IntermediateQuery convert(IQ query, DBMetadata dbMetadata, ExecutorRegistry executorRegistry) {
        IntermediateQueryBuilder queryBuilder = iqFactory.createIQBuilder(dbMetadata, executorRegistry);
        IQTree topTree = query.getTree();
        QueryNode rootNode = topTree.getRootNode();
        queryBuilder.init(query.getProjectionAtom(), rootNode);

        insertChildren(topTree, queryBuilder);

        return queryBuilder.build();
    }

    /**
     * Recursive
     */
    private void insertChildren(IQTree parentTree, IntermediateQueryBuilder queryBuilder) {
        QueryNode parentNode = parentTree.getRootNode();

        if (parentTree instanceof BinaryNonCommutativeIQTree) {
            BinaryNonCommutativeIQTree binaryParentTree = (BinaryNonCommutativeIQTree) parentTree;
            queryBuilder.addChild(parentNode, binaryParentTree.getLeftChild().getRootNode(), LEFT);
            queryBuilder.addChild(parentNode, binaryParentTree.getRightChild().getRootNode(), RIGHT);
        }
        else {
            parentTree.getChildren()
                    .forEach(c -> queryBuilder.addChild(parentNode, c.getRootNode()));
        }

        parentTree.getChildren()
                .forEach(c -> insertChildren(c, queryBuilder));
    }
}
