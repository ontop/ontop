package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.optimizer.FlattenUnionOptimizer;
import it.unibz.inf.ontop.iq.optimizer.MappingUnionNormalizer;
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
public class MappingUnionNormalizerImpl implements MappingUnionNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final FlattenUnionOptimizer unionflattener;

    private class MappingUnionNormalizationException extends OntopInternalBugException {
        protected MappingUnionNormalizationException(String message) {
            super(message);
        }
    }

    @Inject
    private MappingUnionNormalizerImpl(IntermediateQueryFactory iqFactory, FlattenUnionOptimizer unionflattener) {
        this.iqFactory = iqFactory;
        this.unionflattener = unionflattener;
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) {

        IntermediateQuery copy;
        IntermediateQuery updatedQuery = query;
        do {
            copy = updatedQuery;
            IntermediateQuery queryAfterUnionLift = liftUnionsAboveCn(updatedQuery);
            updatedQuery = flattenUnions(queryAfterUnionLift);
        } while (!IQSyntacticEquivalenceChecker.areEquivalent(updatedQuery, copy));
        return updatedQuery;
    }

    private IntermediateQuery liftUnionsAboveCn(IntermediateQuery query) {
        if(query.getChildren(query.getRootNode()).isEmpty()){
            return query;
        }
        IntermediateQueryBuilder builder = iqFactory.createIQBuilder(
                query.getDBMetadata(),
                query.getExecutorRegistry()
        );
        lift(builder, query.getRootNode(), Optional.empty(), query, query.getRootNode(), query.getProjectionAtom());
        return builder.build();
    }

    private void lift(IntermediateQueryBuilder builder, QueryNode bufferNode,
                      Optional<QueryNode> parentInBuilder,
                      IntermediateQuery sourceQuery, QueryNode sourceQueryNode,
                      DistinctVariableOnlyDataAtom projectionAtom) {
        // Construction node: buffer it, and check the successor in the source query
        if (bufferNode instanceof ConstructionNode) {
            liftCn(
                    builder,
                    (ConstructionNode) bufferNode,
                    parentInBuilder,
                    sourceQuery,
                    sourceQuery.getChildren(sourceQueryNode).iterator().next(),
                    projectionAtom
            );
        } else {
            // 'Flush' the buffer node: append it to the tree and go on with child(ren) as buffer(s)
            append(builder, parentInBuilder, bufferNode, projectionAtom);
            sourceQuery.getChildren(sourceQueryNode)
                    .forEach(n -> lift(
                            builder,
                            n,
                            Optional.of(bufferNode),
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

    private void liftCn(IntermediateQueryBuilder builder, ConstructionNode bufferNode,
                        Optional<QueryNode> parentInBuilder, IntermediateQuery sourceQuery,
                        QueryNode sourceQueryNode, DistinctVariableOnlyDataAtom projectionAtom) {
        // if the source query node is a union, lift it
        if (sourceQueryNode instanceof UnionNode) {
            // update the variables projected by the union
            UnionNode updatedUnion = iqFactory.createUnionNode(bufferNode.getVariables());
            // append the union to the builder
            append(builder, parentInBuilder, updatedUnion, projectionAtom);
            // go on for each operand of the union, with (a clone of) the current Cn buffer, and the union as parent
            sourceQuery.getChildren(sourceQueryNode).forEach(c -> liftCn(
                    builder,
                    bufferNode.clone(),
                    Optional.of(updatedUnion),
                    sourceQuery,
                    c,
                    projectionAtom
            ));
            // if the source query node is a construction node, merge it
        } else if (sourceQueryNode instanceof ConstructionNode) {
            liftCn(
                    builder,
                    mergeCns(
                            bufferNode,
                            (ConstructionNode) sourceQueryNode
                    ),
                    parentInBuilder,
                    sourceQuery,
                    sourceQuery.getChildren(sourceQueryNode).iterator().next(),
                    projectionAtom
            );
            // Otherwise add the buffer node to the builder
        } else {
            append(builder, parentInBuilder, bufferNode, projectionAtom);
            // and go on with the source query node as buffer
            lift(
                    builder,
                    sourceQueryNode,
                    Optional.of(bufferNode),
                    sourceQuery,
                    sourceQueryNode,
                    projectionAtom
            );
        }
    }

    private ConstructionNode mergeCns(ConstructionNode parent, ConstructionNode child) {
        ImmutableSubstitution composition = child.getSubstitution()
                .composeWith(parent.getSubstitution())
                .reduceDomainToIntersectionWith(parent.getVariables());
        return iqFactory.createConstructionNode(parent.getVariables(), composition);
    }

    private IntermediateQuery flattenUnions(IntermediateQuery query) {
        try {
            return unionflattener.optimize(query);
        } catch (EmptyQueryException e) {
            throw new MappingUnionNormalizationException("This normalization should not empty the query");
        }
    }
}