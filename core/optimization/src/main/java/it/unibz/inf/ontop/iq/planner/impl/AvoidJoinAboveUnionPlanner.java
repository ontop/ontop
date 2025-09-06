package it.unibz.inf.ontop.iq.planner.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIQOptimizer;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * When a UNION appears as a child of an inner join, looks for other siblings that could be "pushed under the union".
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
public class AvoidJoinAboveUnionPlanner extends AbstractIQOptimizer implements QueryPlanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvoidJoinAboveUnionPlanner.class);
    
    private final GeneralStructuralAndSemanticIQOptimizer generalOptimizer;
    private final IQTreeTools iqTreeTools;

    private final AvoidJoinAboveUnionTransformer transformer;

    @Inject
    protected AvoidJoinAboveUnionPlanner(GeneralStructuralAndSemanticIQOptimizer generalOptimizer,
                                         IntermediateQueryFactory iqFactory,
                                         IQTreeTools iqTreeTools) {
        super(iqFactory);
        this.generalOptimizer = generalOptimizer;
        this.iqTreeTools = iqTreeTools;
        this.transformer = new AvoidJoinAboveUnionTransformer();
    }

    /**
     * Tries to push down some inner joins under some unions.
     * If something has been pushed, it re-applies the structural and semantic optimizations.
     */
    @Override
    public IQ optimize(IQ query) {
        IQ liftedQuery = super.optimize(query);
        LOGGER.debug("Planned IQ:\n{}\n", liftedQuery);
        return liftedQuery.equals(query)
                ? query
                // Re-applies the structural and semantic optimizations
                : generalOptimizer.optimize(liftedQuery, null);
    }

    @Override
    protected IQTree transformTree(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(transformer);
    }

    private class AvoidJoinAboveUnionTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        AvoidJoinAboveUnionTransformer() {
            super(AvoidJoinAboveUnionPlanner.this.iqFactory);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> initialChildren) {

            //Non-final
            ImmutableList<IQTree> children = initialChildren;
            while(true) {
                // NB: for compilation purposes
                ImmutableList<IQTree> currentChildren = children;
                Optional<ImmutableList<IQTree>> push = currentChildren.stream()
                        .map(NaryIQTreeTools.UnionDecomposition::of)
                        .filter(IQTreeTools.IQTreeDecomposition::isPresent)
                        .map(union -> pushLeafIQTreeSiblingsIntoUnion(union, currentChildren))
                        .flatMap(Optional::stream)
                        .findFirst();

                if (push.isEmpty())
                    break;

                children = push.get();
            }
            if (children.equals(initialChildren))
                return tree;

            return iqTreeTools.createOptionalInnerJoinTree(rootNode.getOptionalFilterCondition(), children)
                    .orElseThrow(() -> new MinorOntopInternalBugException("At least one child should remain"));
        }

        /**
         * Criteria for selecting siblings: must be leaf and must naturally join (i.e. share a variable) with the union
         */
        Optional<ImmutableList<IQTree>> pushLeafIQTreeSiblingsIntoUnion(NaryIQTreeTools.UnionDecomposition union, ImmutableList<IQTree> siblings) {

            ImmutableSet<Variable> unionVariables = union.getNode().getVariables();
            ImmutableList<Integer> pushableSiblingIndexes = IntStream.range(0, siblings.size())
                    // Leaf siblings ...
                    .filter(i -> (siblings.get(i) instanceof LeafIQTree)
                            // ... that naturally joins (i.e. share a variable) with the union
                            && !Sets.intersection(unionVariables, siblings.get(i).getVariables()).isEmpty())
                    .boxed()
                    .collect(ImmutableCollectors.toList());

            if (pushableSiblingIndexes.isEmpty())
                return Optional.empty();

            ImmutableList<IQTree> pushedSiblings = pushableSiblingIndexes.stream()
                    .map(siblings::get)
                    .collect(ImmutableCollectors.toList());

            ImmutableList<IQTree> newUnionChildren = union.transformChildren(
                    c -> iqTreeTools.createInnerJoinTree(
                            Stream.concat(Stream.of(c), pushedSiblings.stream()).collect(ImmutableCollectors.toList())));

            ImmutableSet<Variable> newUnionVariables = Stream.concat(
                            unionVariables.stream(),
                            pushedSiblings.stream()
                                    .flatMap(s -> s.getVariables().stream()))
                    .collect(ImmutableCollectors.toSet());

            IQTree newUnionTree = iqTreeTools.createUnionTree(newUnionVariables, newUnionChildren);

            ImmutableList<IQTree> newChildren = IntStream.range(0, siblings.size())
                    .filter(i -> !pushableSiblingIndexes.contains(i))
                    .mapToObj(siblings::get)
                    .map(c -> (c == union.getTree()) ? newUnionTree : c)
                    .collect(ImmutableCollectors.toList());

            return Optional.of(newChildren);
        }
    }
}
