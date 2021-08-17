package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.AggregationNode;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.optimizer.AggregationUnionLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public class AggregationUnionLifterImpl implements AggregationUnionLifter {

    @Inject
    protected AggregationUnionLifterImpl() {
    }

    @Override
    public IQ optimize(IQ query) {
        // TODO: normalize the query before
        throw new RuntimeException("TODO: implement it");
    }


    /**
     * Assumes that the tree is normalized
     */
    protected static class AggregationUnionLifterTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected AggregationUnionLifterTransformer(CoreSingletons coreSingletons) {
            super(coreSingletons);
        }

        @Override
        public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
            IQTree liftedChild = child.acceptTransformer(this);

            return tryToLift(rootNode, liftedChild)
                    .orElseGet(() -> liftedChild == child
                            ? tree
                            : iqFactory.createUnaryIQTree(rootNode, liftedChild));
        }

        private Optional<IQTree> tryToLift(AggregationNode rootNode, IQTree child) {

            ImmutableSet<Variable> groupingVariables = rootNode.getGroupingVariables();

            /*
             * After normalization, grouping variable definitions are expected to be lifted above
             * the aggregation node except if their definitions are not unique (i.e. blocked by a UNION)
             */
            ImmutableSet<Variable> groupingVariablesWithDifferentDefinitions = groupingVariables.stream()
                    .filter(child::isConstructed)
                    .collect(ImmutableCollectors.toSet());

            if (groupingVariablesWithDifferentDefinitions.isEmpty())
                return Optional.empty();


            Optional<ConstructionNode> childConstructionNode = Optional.of(child.getRootNode())
                    .filter(n -> n instanceof ConstructionNode)
                    .map(n -> (ConstructionNode) n);

            if (childConstructionNode
                    .filter(n ->
                        !Sets.intersection(
                                n.getLocallyDefinedVariables(),
                                groupingVariables).isEmpty())
                    .isPresent())
                throw new MinorOntopInternalBugException("Should not happen in a normalized tree " +
                        "(grouping variable definitions should have been already lifted)");

            IQTree nonConstructionChild = childConstructionNode
                    .map(cst -> ((UnaryIQTree) child).getChild())
                    .orElse(child);

            if (!(nonConstructionChild.getRootNode() instanceof UnionNode))
                // TODO: log a message, as we are in an unexpected situation
                return Optional.empty();

            ImmutableList<IQTree> unionChildren = nonConstructionChild.getChildren();

            ImmutableList<ImmutableList<IQTree>> groups = groupingVariablesWithDifferentDefinitions.stream()
                    .reduce(ImmutableList.of(unionChildren),
                            (gs, v) -> gs.stream()
                                    .flatMap(g -> tryToSplit(g, v))
                                    .collect(ImmutableCollectors.toList()),
                            (gs1,gs2) -> {
                        throw new RuntimeException("Not to be run in // ");
                    });

            if (groups.size() <= 1)
                return Optional.empty();

            return Optional.of(liftUnion(groups, childConstructionNode, rootNode));

        }

        private Stream<ImmutableList<IQTree>> tryToSplit(ImmutableList<IQTree> group, Variable groupingVariable) {
            // TODO: build groups incrementally where a IQTree gets added if its definition is compatible with one of the already added definitions
            // At the end, groups with some common members will be merged. Only mutually exclusive groups will be returned.
            // TODO: be careful about preserving duplicates of the same tree!
            throw new RuntimeException("TODO: implement");
        }

        /**
         * TODO: handle the case where no definition is available -> splitting becomes impossible,
         * as it could match with everything .
         */
        private Stream<NonVariableTerm> getDefinitions(IQTree tree, Variable variable) {
            //return tree.getPossibleVariableDefinitions().stream()
            throw new RuntimeException("TODO: implement");
        }

        protected IQTree liftUnion(ImmutableList<ImmutableList<IQTree>> groups, Optional<ConstructionNode> childConstructionNode,
                                 AggregationNode rootNode) {
            throw new RuntimeException("TODO: implement");
        }
    }

}
