package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.spec.mapping.transformer.QueryUnionSplitter;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * Only splits according to the first splittable union found (breadth-first search)
 *
 */
@Singleton
public class QueryUnionSplitterImpl implements QueryUnionSplitter {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private QueryUnionSplitterImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public Stream<IQ> splitUnion(IQ query) {
        DistinctVariableOnlyDataAtom projectionAtom = query.getProjectionAtom();
        VariableGenerator variableGenerator = query.getVariableGenerator();
        IQTree tree = query.getTree();

        return findFirstSplittableUnion(query)
                .map(unionTree -> unionTree.getChildren().stream()
                        .map(c -> tree.replaceSubTree(unionTree, c))
                        .map(t -> t.normalizeForOptimization(variableGenerator))
                        .map(t -> iqFactory.createIQ(projectionAtom, t))
                        .map(IQ::normalizeForOptimization))
                .orElseGet(() -> Stream.of(query));
    }

    private Optional<NaryIQTree> findFirstSplittableUnion(IQ query) {
        Queue<IQTree> nodesToVisit = new LinkedList<>();
        nodesToVisit.add(query.getTree());

        while(!nodesToVisit.isEmpty()) {
            IQTree childTree = nodesToVisit.poll();
            if (childTree.getRootNode() instanceof UnionNode) {
                return Optional.of((NaryIQTree) childTree);
            }
            else {
                nodesToVisit.addAll(extractChildrenToVisit(childTree));
            }
        }

        return Optional.empty();
    }

    private ImmutableList<IQTree> extractChildrenToVisit(IQTree tree) {
        QueryNode node = tree.getRootNode();
        if (node instanceof BinaryNonCommutativeOperatorNode) {
            if (node instanceof LeftJoinNode) {
                return ImmutableList.of(((BinaryNonCommutativeIQTree)tree).getLeftChild());
            }
            /*
             * Not supported BinaryNonCommutativeOperatorNode: we ignore them
             */
            else {
                return ImmutableList.of();
            }
        }
        else {
            return tree.getChildren();
        }
    }

}
