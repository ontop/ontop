package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.impl.AbstractIQTreeToStreamVisitingTransformer;
import it.unibz.inf.ontop.spec.mapping.transformer.QueryUnionSplitter;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Only splits according to the first splittable union found
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

        return query.getTree().acceptVisitor(new AbstractIQTreeToStreamVisitingTransformer<NaryIQTree>() {
            @Override
            public Stream<NaryIQTree> transformUnion(NaryIQTree tree, UnionNode node, ImmutableList<IQTree> children) {
                return Stream.of(tree);
            }
            @Override
            public Stream<NaryIQTree> transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode node, IQTree leftChild, IQTree rightChild) {
                return leftChild.acceptVisitor(this);
            }
        }).findAny();
    }
}
