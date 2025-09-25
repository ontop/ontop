package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.AggregationSplitter;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;


public class AggregationSplitterImpl extends AbstractIQOptimizer implements AggregationSplitter {

    private final IQTreeTools iqTreeTools;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final QueryRenamer queryRenamer;

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    protected AggregationSplitterImpl(CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory());
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.queryRenamer = coreSingletons.getQueryRenamer();

        this.transformer = IQTreeVariableGeneratorTransformer.of(
                IQTree::normalizeForOptimization,
                IQTreeVariableGeneratorTransformer.of(AggregationUnionLifterTransformer::new),
                IQTree::normalizeForOptimization);
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }


    /**
     * Assumes that the tree is normalized
     */
    private class AggregationUnionLifterTransformer extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {

        AggregationUnionLifterTransformer(VariableGenerator variableGenerator) {
            super(AggregationSplitterImpl.this.iqFactory, variableGenerator);
        }

        // uses == for withTransformedChild
        @Override
        public IQTree transformAggregation(UnaryIQTree tree, AggregationNode rootNode, IQTree child) {
            IQTree liftedChild = transformChild(child);

            ImmutableSet<Variable> groupingVariables = rootNode.getGroupingVariables();

            var construction = UnaryIQTreeDecomposition.of(liftedChild, ConstructionNode.class);
            var distinct = UnaryIQTreeDecomposition.of(construction, DistinctNode.class);
            var subConstruction = UnaryIQTreeDecomposition.of(distinct, ConstructionNode.class);
            var union = NaryIQTreeTools.UnionDecomposition.of(subConstruction);
            if (!union.isPresent())
                // TODO: log a message, as we are in an unexpected situation
                return withTransformedChild(tree, liftedChild);

            /*
             * After normalization, grouping variable definitions are expected to be lifted above
             * the aggregation node except if their definitions are not unique (i.e. blocked by a UNION).
             *
             * For the sake of simplicity, we exclude variables defined by the child construction node.
             */
            ImmutableSet<Variable> groupingVariablesWithDifferentDefinitions = groupingVariables.stream()
                    .filter(liftedChild::isConstructed)
                    .filter(v -> construction.getOptionalNode()
                            .filter(n -> n.getSubstitution().isDefining(v))
                            .isEmpty())
                    .filter(v -> subConstruction.getOptionalNode()
                            .filter(n -> n.getSubstitution().isDefining(v))
                            .isEmpty())
                    .collect(ImmutableCollectors.toSet());

            if (groupingVariablesWithDifferentDefinitions.isEmpty())
                return withTransformedChild(tree, liftedChild);

            ImmutableMultiset<IQTree> unionChildren = ImmutableMultiset.copyOf(union.getChildren());
            VariableNullability variableNullability = union.getTree().getVariableNullability();

            ImmutableList<ImmutableSet<IQTree>> groups = groupingVariablesWithDifferentDefinitions.stream()
                    .reduce(ImmutableList.of(ImmutableSet.copyOf(unionChildren)),
                            (gs, v) -> gs.stream()
                                    .flatMap(g -> tryToSplit(g, v, variableNullability))
                                    .collect(ImmutableCollectors.toList()),
                            (gs1,gs2) -> {
                        throw new RuntimeException("Not to be run in // ");
                    });

            if (groups.size() <= 1)
                return withTransformedChild(tree, liftedChild);

            return liftUnion(
                            groups,
                            UnaryOperatorSequence.of()
                                    .append(rootNode)
                                    .append(construction.getOptionalNode())
                                    .append(distinct.getOptionalNode())
                                    .append(subConstruction.getOptionalNode()),
                            rootNode,
                            unionChildren);
        }

        private Stream<ImmutableSet<IQTree>> tryToSplit(ImmutableSet<IQTree> initialGroup, Variable groupingVariable,
                                                        VariableNullability variableNullability) {
            List<ChildGroup> groups = Lists.newArrayList();

            for (IQTree tree : initialGroup) {
                Optional<ImmutableSet<NonVariableTerm>> optionalDefinitions = getDefinitions(tree, groupingVariable);

                // Cannot split on this grouping variable as it can match everything
                if (!optionalDefinitions.isPresent())
                    return Stream.of(initialGroup);

                ImmutableSet<NonVariableTerm> treeDefinitions = optionalDefinitions.get();

                // Non-final
                boolean foundAGroup = false;
                for (ChildGroup group : groups) {
                    if (group.addIfCompatible(tree, treeDefinitions, variableNullability, termFactory))
                        foundAGroup = true;
                }

                // Creates a new group when no matching group has been found
                if (!foundAGroup) {
                    groups.add(new ChildGroup(tree, treeDefinitions));
                }
            }

            if (groups.size() < 2)
                return Stream.of(initialGroup);

            return mergeGroups(groups);
        }

        private Optional<ImmutableSet<NonVariableTerm>> getDefinitions(IQTree tree, Variable variable) {
            ImmutableSet<ImmutableTerm> possibleValues = tree.getPossibleVariableDefinitions().stream()
                    .map(s -> s.apply(variable))
                    .collect(ImmutableCollectors.toSet());

            // If a definition is not available (e.g. the possible value is a variable), everything is possible
            // so we cannot split the aggregation based on this variable.
            if (possibleValues.isEmpty() || possibleValues.stream().anyMatch(t -> t instanceof Variable))
                return Optional.empty();
            else
                // correctness of the cast is ensured above
                return Optional.of((ImmutableSet<NonVariableTerm>)(ImmutableSet<?>)possibleValues);
        }

        private Stream<ImmutableSet<IQTree>> mergeGroups(List<ChildGroup> nonMergedGroups) {
            List<ChildGroup> mergedGroups = Lists.newArrayList();

            for (ChildGroup nonMergedGroup : nonMergedGroups) {
                // Non-final
                boolean mergedInAGroup = false;
                for (ChildGroup group : mergedGroups) {
                    // Side effect
                    if (group.mergeIfCompatible(nonMergedGroup)) {
                        mergedInAGroup = true;
                        break;
                    }
                }

                // Creates a new group when no matching group has been found
                if (!mergedInAGroup) {
                    mergedGroups.add(nonMergedGroup);
                }
            }
            return mergedGroups.stream()
                    .map(ChildGroup::getTrees);
        }

        private IQTree liftUnion(ImmutableList<ImmutableSet<IQTree>> groups, UnaryOperatorSequence<? extends UnaryOperatorNode> nodes,
                                   AggregationNode initialAggregationNode, ImmutableMultiset<IQTree> unionChildren) {
            Set<Variable> nonGroupingVariables = Sets.difference(initialAggregationNode.getChildVariables(), initialAggregationNode.getGroupingVariables());

            ImmutableList<ImmutableList<IQTree>> multiGroups = groups.stream()
                    .map(g -> unionChildren.entrySet().stream()
                            .filter(e -> g.contains(e.getElement()))
                            .flatMap(e -> IntStream.range(0, e.getCount())
                                    .mapToObj(i -> e.getElement()))
                            .collect(ImmutableCollectors.toList()))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<IQTree> topUnionChildren = multiGroups.stream()
                    .map(g -> {
                        switch (g.size()) {
                            case 0:
                                throw new MinorOntopInternalBugException("Should not be empty");
                            case 1:
                                return g.get(0);
                            default:
                                return iqTreeTools.createUnionTree(initialAggregationNode.getChildVariables(), g);
                        }
                    })
                    .map(t -> iqTreeTools.unaryIQTreeBuilder().append(nodes).build(t))
                    .map(t -> renameSomeUnprojectedVariables(t, nonGroupingVariables))
                    .collect(ImmutableCollectors.toList());

            return iqTreeTools.createUnionTree(initialAggregationNode.getVariables(),
                    topUnionChildren);
        }

        private IQTree renameSomeUnprojectedVariables(IQTree tree, Set<Variable> nonGroupingVariables) {
            InjectiveSubstitution<Variable> renaming = nonGroupingVariables.stream()
                    .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

            return queryRenamer.applyInDepthRenaming(renaming, tree);
        }
    }

    /**
     * Mutable
     */
    protected static class ChildGroup {
        // Mutable
        private final Set<IQTree> trees;
        // Mutable
        private final Set<NonVariableTerm> definitions;

        public ChildGroup(IQTree tree, Set<NonVariableTerm> treeDefinitions) {
            this.trees = Sets.newHashSet(tree);
            this.definitions = Sets.newHashSet(treeDefinitions);
        }

        /**
         * Returns true if the tree is compatible and has been added.
         *
         * Has side effect.
         */
        public boolean addIfCompatible(IQTree tree, Set<NonVariableTerm> treeDefinitions, VariableNullability variableNullability,
                                       TermFactory termFactory) {
            for (NonVariableTerm definition : treeDefinitions) {
                if (definitions.contains(definition)
                        || definitions.stream().anyMatch(d -> areCompatibleGroupingConditions(d, definition, variableNullability, termFactory))) {
                    trees.add(tree);
                    definitions.addAll(treeDefinitions);
                    return true;
                }
            }
            return false;
        }

        /**
         * Compatible: if they can produce the same value, NULL included (-> same group).
         *
         * Must not produce any false negative.
         */
        private boolean areCompatibleGroupingConditions(NonVariableTerm t1, NonVariableTerm t2, VariableNullability variableNullability, TermFactory termFactory) {
            // Special case of incompatibility: one is null and the other one is not nullable
            if ((t1.isNull() && (!t2.isNullable(variableNullability.getNullableVariables())))
                    || (t2.isNull() && (!t1.isNullable(variableNullability.getNullableVariables()))))
                return false;

            return !termFactory.getStrictEquality(t1, t2)
                    .evaluate2VL(variableNullability).isEffectiveFalse();
        }

        /**
         * Returns true if they merged.
         *
         * Has side effect.
         */
        public boolean mergeIfCompatible(ChildGroup group) {
            if (group.definitions.stream()
                    .anyMatch(definitions::contains)) {
                definitions.addAll(group.definitions);
                trees.addAll(group.trees);
                return true;
            }
            return false;
        }

        public ImmutableSet<IQTree> getTrees() {
            return ImmutableSet.copyOf(trees);
        }

    }
}
