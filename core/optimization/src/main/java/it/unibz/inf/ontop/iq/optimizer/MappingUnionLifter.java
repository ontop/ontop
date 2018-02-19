package it.unibz.inf.ontop.iq.optimizer;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

/**
 * Temporary fix.
 * To be deleted when integrating the IQ interface.
 * <p>
 * Lifts unions above projections.
 * Also merges consecutive projections.
 * <p>
 * This normalization may be needed for datalog-based mapping optimizers.
 */
public class MappingUnionLifter {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private MappingUnionLifter(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    public IntermediateQuery optimizeQuery(IntermediateQuery query, IntermediateQueryFactory iqFactory) {
        IntermediateQueryBuilder builder = iqFactory.createIQBuilder(
                query.getDBMetadata(),
                query.getExecutorRegistry()
        );
        optimize(builder, query.getRootNode(), Optional.empty(), query, query.getRootNode(), query.getProjectionAtom());
        return builder.build();
    }

    private void optimize(IntermediateQueryBuilder builder, QueryNode currentNode, Optional<QueryNode> parentInBuilder,
                          IntermediateQuery sourceQuery, QueryNode sourceQueryNode, DistinctVariableOnlyDataAtom projectionAtom) {

        if (currentNode instanceof ConstructionNode) {
            optimizeCn(builder, (ConstructionNode) currentNode, parentInBuilder, sourceQuery, sourceQueryNode, projectionAtom);
        } else {
            append(builder, parentInBuilder, currentNode, projectionAtom);
            sourceQuery.getChildren(sourceQueryNode)
                    .forEach(n -> optimize(
                            builder,
                            n,
                            Optional.of(currentNode),
                            sourceQuery,
                            n,
                            projectionAtom
                    ));
        }
    }

    private void append(IntermediateQueryBuilder builder, Optional<QueryNode> parentInBuilder, QueryNode currentnode,
                        DistinctVariableOnlyDataAtom projectionAtom) {
        if (parentInBuilder.isPresent()) {
            builder.addChild(parentInBuilder.get(), currentnode);
        } else {
            builder.init(projectionAtom, currentnode);
        }
    }

    private void optimizeCn(IntermediateQueryBuilder builder, ConstructionNode currentNode,
                            Optional<QueryNode> parentInBuilder, IntermediateQuery sourceQuery,
                            QueryNode sourceQueryNode, DistinctVariableOnlyDataAtom projectionAtom) {
        // if the source query node is a union, lift it
        if (sourceQueryNode instanceof UnionNode) {
            sourceQuery.getChildren(sourceQueryNode).forEach(c -> optimize(
                    builder,
                    currentNode,
                    Optional.of(sourceQueryNode),
                    sourceQuery,
                    c,
                    projectionAtom
            ));
            // if the source query node is a construction node, merge it
        } else if (sourceQueryNode instanceof ConstructionNode) {
            optimizeCn(
                    builder,
                    mergeCns(
                            currentNode,
                            (ConstructionNode) sourceQueryNode
                    ),
                    parentInBuilder,
                    sourceQuery,
                    sourceQuery.getChildren(sourceQueryNode).iterator().next(),
                    projectionAtom
            );
            // Otherwise add the current node to the builder
        } else {
            parentInBuilder.ifPresent(p -> builder.addChild(p, currentNode));
            sourceQuery.getChildren(sourceQueryNode)
                    .forEach(n -> optimize(
                            builder,
                            n,
                            Optional.of(currentNode),
                            sourceQuery,
                            n,
                            projectionAtom
                    ));
        }
    }

    private ConstructionNode mergeCns(ConstructionNode parent, ConstructionNode child) {
        ImmutableSubstitution composition = parent.getSubstitution().composeWith(child.getSubstitution());
        return iqFactory.createConstructionNode(parent.getVariables(), composition);
    }
}