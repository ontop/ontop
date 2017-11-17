package it.unibz.inf.ontop.iq.tools.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.QueryUnionSplitter;
import it.unibz.inf.ontop.iq.optimizer.BindingLiftOptimizer;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * Only splits according to the first splittable union found (breadth-first search)
 */
@Singleton
public class QueryUnionSplitterImpl implements QueryUnionSplitter {

    private final BindingLiftOptimizer bindingLiftOptimizer;

    @Inject
    private QueryUnionSplitterImpl(BindingLiftOptimizer bindingLiftOptimizer) {
        // TODO: use dependency injection instead
        this.bindingLiftOptimizer = bindingLiftOptimizer;
    }

    @Override
    public Stream<IntermediateQuery> splitUnion(IntermediateQuery query) {
        return findFirstSplittableUnion(query)
                .map(unionNode ->
                        split(query, unionNode)
                        .flatMap(this::lift))
                .orElseGet(() -> Stream.of(query));
    }

    private Optional<UnionNode> findFirstSplittableUnion(IntermediateQuery query) {
        Queue<QueryNode> nodesToVisit = new LinkedList<>(query.getChildren(query.getRootNode()));

        while(!nodesToVisit.isEmpty()) {
            QueryNode node = nodesToVisit.poll();
            if (node instanceof UnionNode) {
                return Optional.of((UnionNode)node);
            }
            else {
                nodesToVisit.addAll(extractChildrenToVisit(query, node));
            }
        }

        return Optional.empty();
    }

    private ImmutableList<QueryNode> extractChildrenToVisit(IntermediateQuery query, QueryNode node) {
        if (node instanceof BinaryNonCommutativeOperatorNode) {
            if (node instanceof LeftJoinNode) {
                return ImmutableList.of(query.getChild(node, BinaryOrderedOperatorNode.ArgumentPosition.LEFT)
                        .orElseThrow(() -> new InvalidIntermediateQueryException("A left join must have a left child")));
            }
            /*
             * Not supported BinaryNonCommutativeOperatorNode: we ignore them
             */
            else {
                return ImmutableList.of();
            }
        }
        else {
            return query.getChildren(node);
        }
    }

    private Stream<IntermediateQuery> split(IntermediateQuery query, UnionNode unionNode) {
        return query.getChildren(unionNode).stream()
                .map(child -> split(query, unionNode, child));
    }

    private IntermediateQuery split(IntermediateQuery originalQuery, UnionNode unionNode, QueryNode replacingChildNode) {
        IntermediateQueryBuilder queryBuilder = originalQuery.newBuilder();
        QueryNode rootNode = originalQuery.getRootNode();
        queryBuilder.init(originalQuery.getProjectionAtom(), rootNode);

        Queue<QueryNode> parentNodes = new LinkedList<>();
        parentNodes.add(rootNode);

        while(!parentNodes.isEmpty()) {
            QueryNode parentNode = parentNodes.poll();

            for(QueryNode originalChildNode : originalQuery.getChildren(parentNode)) {

                QueryNode childNode = originalChildNode == unionNode
                        ? replacingChildNode
                        : originalChildNode;

                queryBuilder.addChild(parentNode, childNode, originalQuery.getOptionalPosition(
                        parentNode, originalChildNode));

                parentNodes.add(childNode);
            }
        }
        return queryBuilder.build();
    }


    private Stream<IntermediateQuery> lift(IntermediateQuery query) {
        try {
            return Stream.of(bindingLiftOptimizer.optimize(query));
        } catch (EmptyQueryException e) {
            return Stream.empty();
        }
    }

}
