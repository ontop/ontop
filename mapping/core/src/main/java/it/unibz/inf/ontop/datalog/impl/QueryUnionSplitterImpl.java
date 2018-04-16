package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.datalog.QueryUnionSplitter;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * Only splits according to the first splittable union found (breadth-first search)
 *
 * HACKY!!! Supposes that the projection atom fits with the UNION
 *
 * TODO: remove it after getting rid of Datalog in the mapping process
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
        VariableGenerator variableGenerator = query.getVariableGenerator();
        DistinctVariableOnlyDataAtom projectionAtom = query.getProjectionAtom();

        return findFirstSplittableUnion(query, projectionAtom)
                .map(unionTree -> unionTree.getChildren().stream()
                        .map(c -> c.liftBinding(variableGenerator))
                        .map(t -> iqFactory.createIQ(projectionAtom, unionTree))
                )
                .orElseGet(() -> Stream.of(query));
    }

    private Optional<NaryIQTree> findFirstSplittableUnion(IQ query, DistinctVariableOnlyDataAtom projectionAtom) {
        Queue<IQTree> nodesToVisit = new LinkedList<>();
        nodesToVisit.add(query.getTree());

        while(!nodesToVisit.isEmpty()) {
            IQTree childTree = nodesToVisit.poll();
            if (childTree.getRootNode() instanceof UnionNode) {
                if (!((UnionNode) childTree.getRootNode()).getVariables().equals(projectionAtom.getVariables()))
                    throw new UnionWithDifferentSignatureException(childTree, projectionAtom);
                else
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

    private static class UnionWithDifferentSignatureException extends OntopInternalBugException {

        protected UnionWithDifferentSignatureException(IQTree childTree, DistinctVariableOnlyDataAtom projectionAtom) {
            super("The following union tree does fit " + projectionAtom + ":\n" + childTree);
        }
    }

}
