package it.unibz.inf.ontop.iq.planner.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * When an UNION appears as a child of an inner join, looks for other siblings that could be "pushed under the union".
 * <p>
 * Criteria for selecting siblings: must be leaf and must naturally join (i.e. share a variable) with the union.
 *
 *
 * Example:
 * <pre>
 *   JOIN
 *     T1(x,y)
 *     UNION(x,z)
 *       T2(x,z)
 *       T3(x,z)
 *     T4(w)
 * </pre>
 *  
 *  becomes
 *  
 *  <pre>
 *    JOIN
 *      UNION(x,z)
 *        JOIN
 *          T2(x,z)
 *          T1(x,y)
 *        JOIN
 *          T3(x,z)
 *          T1(x,y)
 *      T4(w)
 * </pre>
 * TODO: shall we consider also the joining condition for pushing more siblings?
 */
@Singleton
public class AvoidJoinAboveUnionPlanner implements QueryPlanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvoidJoinAboveUnionPlanner.class);
    
    private final GeneralStructuralAndSemanticIQOptimizer generalOptimizer;
    private final AvoidJoinAboveUnionTransformer transformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected AvoidJoinAboveUnionPlanner(GeneralStructuralAndSemanticIQOptimizer generalOptimizer,
                                         AvoidJoinAboveUnionTransformer transformer,
                                         IntermediateQueryFactory iqFactory) {
        this.generalOptimizer = generalOptimizer;
        this.transformer = transformer;
        this.iqFactory = iqFactory;
    }

    /**
     * Tries to push down some inner joins under some unions.
     * If something has been pushed, it re-applies the structural and semantic optimizations.
     */
    @Override
    public IQ optimize(IQ query) {
        IQ liftedQuery = lift(query);
        return liftedQuery.equals(query)
                ? query
                // Re-applies the structural and semantic optimizations
                : generalOptimizer.optimize(liftedQuery);
    }

    protected IQ lift(IQ query) {
        IQTree tree = query.getTree();
        IQTree newTree = transformer.transform(tree);

        IQ newIQ = newTree.equals(tree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);

        LOGGER.debug("Planned IQ:\n{}\n", newIQ);
        return newIQ;
    }

    @Singleton
    protected static class AvoidJoinAboveUnionTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        @Inject
        protected AvoidJoinAboveUnionTransformer(IntermediateQueryFactory iqFactory) {
            super(iqFactory);
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> initialChildren) {

            //Non-final
            ImmutableList<IQTree> children = initialChildren;
            while(true) {
                // NB: for compilation purposes
                ImmutableList<IQTree> currentChildren = children;

                Optional<Map.Entry<NaryIQTree, ImmutableList<Integer>>> selectedEntry = children.stream()
                        .filter(c -> c.getRootNode() instanceof UnionNode)
                        .map(c -> (NaryIQTree) c)
                        .map(c -> extractPushableSiblings(c, currentChildren))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst();

                if (selectedEntry.isPresent()) {
                    children = updateChildren(selectedEntry.get().getKey(), selectedEntry.get().getValue(), children);
                }
                else {
                    if(children.equals(initialChildren))
                        return tree;

                    switch(children.size()) {
                        case 0:
                            throw new MinorOntopInternalBugException("At least one child should remain");
                        case 1:
                            return rootNode.getOptionalFilterCondition()
                                    .map(iqFactory::createFilterNode)
                                    .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, currentChildren.get(0)))
                                    .orElseGet(() -> currentChildren.get(0));
                        default:
                            return iqFactory.createNaryIQTree(rootNode, children);
                    }
                }
            }
        }

        /**
         * Criteria for selecting siblings: must be leaf and must naturally join (i.e. share a variable) with the union
         */
        protected Optional<Map.Entry<NaryIQTree, ImmutableList<Integer>>> extractPushableSiblings(NaryIQTree unionTree,
                                                                                             ImmutableList<IQTree> children) {
            ImmutableSet<Variable> unionVariables = unionTree.getVariables();

            ImmutableList<Integer> pushableSiblings = IntStream.range(0, children.size())
                    // Leaf siblings ...
                    .filter(i -> (children.get(i) instanceof LeafIQTree)
                            // ... that naturally joins (i.e. share a variable) with the union
                            && !Sets.intersection(unionVariables, children.get(i).getVariables()).isEmpty())
                    .boxed()
                    .collect(ImmutableCollectors.toList());

            return pushableSiblings.isEmpty()
                    ? Optional.empty()
                    : Optional.of(Maps.immutableEntry(unionTree, pushableSiblings));
        }

        private ImmutableList<IQTree> updateChildren(NaryIQTree unionTree, ImmutableList<Integer> pushableSiblingIndexes,
                                                     ImmutableList<IQTree> children) {

            ImmutableList<IQTree> pushedSiblings = pushableSiblingIndexes.stream()
                    .map(children::get)
                    .collect(ImmutableCollectors.toList());

            ImmutableList<IQTree> newUnionChildren = unionTree.getChildren().stream()
                    .map(c -> Stream.concat(Stream.of(c),
                            pushedSiblings.stream()).collect(ImmutableCollectors.toList()))
                    .map(cs -> iqFactory.createNaryIQTree(
                            iqFactory.createInnerJoinNode(),
                            cs))
                    .collect(ImmutableCollectors.toList());

            ImmutableSet<Variable> newUnionVariables = Sets.union(
                    unionTree.getVariables(),
                    pushedSiblings.stream()
                            .flatMap(s -> s.getVariables().stream())
                            .collect(ImmutableCollectors.toSet()))
                    .immutableCopy();

            NaryIQTree newUnionTree = iqFactory.createNaryIQTree(
                    iqFactory.createUnionNode(newUnionVariables),
                    newUnionChildren);

            return IntStream.range(0, children.size())
                    .filter(i -> !pushableSiblingIndexes.contains(i))
                    .mapToObj(children::get)
                    .map(c -> (c == unionTree) ? newUnionTree : c)
                    .collect(ImmutableCollectors.toList());
        }
    }
}
