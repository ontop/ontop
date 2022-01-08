package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.AggregationSplitter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AggregationSplitterImpl implements AggregationSplitter {

    private final CoreSingletons coreSingletons;

    @Inject
    protected AggregationSplitterImpl(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQ optimize(IQ query) {
        IQ normalizedQuery = query.normalizeForOptimization();
        AggregationUnionLifterTransformer transformer = new AggregationUnionLifterTransformer(coreSingletons, query.getVariableGenerator());

        IQTree tree = normalizedQuery.getTree();
        IQTree newTree = transformer.transform(tree);
        return newTree == tree
                ? normalizedQuery
                : coreSingletons.getIQFactory().createIQ(normalizedQuery.getProjectionAtom(), newTree)
                .normalizeForOptimization();
    }


    /**
     * Assumes that the tree is normalized
     */
    protected static class AggregationUnionLifterTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final VariableGenerator variableGenerator;
        private final SubstitutionFactory substitutionFactory;
        private final TermFactory termFactory;

        protected AggregationUnionLifterTransformer(CoreSingletons coreSingletons, VariableGenerator variableGenerator) {
            super(coreSingletons);
            this.variableGenerator = variableGenerator;
            this.substitutionFactory = coreSingletons.getSubstitutionFactory();
            this.termFactory = coreSingletons.getTermFactory();
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

            Optional<ConstructionNode> topChildConstructionNode = Optional.of(child.getRootNode())
                    .filter(n -> n instanceof ConstructionNode)
                    .map(n -> (ConstructionNode) n);

            IQTree nonTopConstructionDescendant = topChildConstructionNode
                    .map(cst -> ((UnaryIQTree) child).getChild())
                    .orElse(child);

            Optional<DistinctNode> distinctDescendantNode = Optional.of(nonTopConstructionDescendant)
                    .map(IQTree::getRootNode)
                    .filter(n -> n instanceof DistinctNode)
                    .map(n -> (DistinctNode) n);

            IQTree nonTopConstructionNonDistinctDescendant = distinctDescendantNode
                    .map(cst -> ((UnaryIQTree) nonTopConstructionDescendant).getChild())
                    .orElse(nonTopConstructionDescendant);

            Optional<ConstructionNode> subConstructionDescendantNode = Optional.of(nonTopConstructionNonDistinctDescendant)
                    .map(IQTree::getRootNode)
                    .filter(n -> n instanceof ConstructionNode)
                    .map(n -> (ConstructionNode) n);

            IQTree nonConstructionNonDistinctDescendant = subConstructionDescendantNode
                    .map(cst -> ((UnaryIQTree) nonTopConstructionNonDistinctDescendant).getChild())
                    .orElse(nonTopConstructionNonDistinctDescendant);

            if (!(nonConstructionNonDistinctDescendant.getRootNode() instanceof UnionNode))
                // TODO: log a message, as we are in an unexpected situation
                return Optional.empty();

            /*
             * After normalization, grouping variable definitions are expected to be lifted above
             * the aggregation node except if their definitions are not unique (i.e. blocked by a UNION).
             *
             * For the sake of simplicity, we exclude variables defined by the child construction node.
             */
            ImmutableSet<Variable> groupingVariablesWithDifferentDefinitions = groupingVariables.stream()
                    .filter(child::isConstructed)
                    .filter(v -> !topChildConstructionNode
                            .filter(n -> n.getSubstitution().isDefining(v))
                            .isPresent())
                    .filter(v -> !subConstructionDescendantNode
                            .filter(n -> n.getSubstitution().isDefining(v))
                            .isPresent())
                    .collect(ImmutableCollectors.toSet());

            if (groupingVariablesWithDifferentDefinitions.isEmpty())
                return Optional.empty();

            ImmutableMultiset<IQTree> unionChildren = ImmutableMultiset.copyOf(nonConstructionNonDistinctDescendant.getChildren());

            VariableNullability variableNullability = nonConstructionNonDistinctDescendant.getVariableNullability();

            ImmutableList<ImmutableSet<IQTree>> groups = groupingVariablesWithDifferentDefinitions.stream()
                    .reduce(ImmutableList.of(ImmutableSet.copyOf(unionChildren)),
                            (gs, v) -> gs.stream()
                                    .flatMap(g -> tryToSplit(g, v, variableNullability))
                                    .collect(ImmutableCollectors.toList()),
                            (gs1,gs2) -> {
                        throw new RuntimeException("Not to be run in // ");
                    });

            if (groups.size() <= 1)
                return Optional.empty();

            return Optional.of(liftUnion(groups, topChildConstructionNode, distinctDescendantNode, subConstructionDescendantNode,
                    rootNode, unionChildren));

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
                    .map(s -> s.applyToVariable(variable))
                    .collect(ImmutableCollectors.toSet());

            // If a definition is not available (e.g. the possible value is a variable), everything is possible
            // so we cannot split the aggregation based on this variable.
            if (possibleValues.isEmpty() || possibleValues.stream().anyMatch(t -> t instanceof Variable))
                return Optional.empty();
            else
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

        protected IQTree liftUnion(ImmutableList<ImmutableSet<IQTree>> groups, Optional<ConstructionNode> childConstructionNode,
                                   Optional<DistinctNode> distinctDescendantNode, Optional<ConstructionNode> subConstructionDescendantNode,
                                   AggregationNode initialAggregationNode, ImmutableMultiset<IQTree> unionChildren) {
            Sets.SetView<Variable> nonGroupingVariables = Sets.difference(initialAggregationNode.getChildVariables(), initialAggregationNode.getGroupingVariables());

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
                                return iqFactory.createUnaryIQTree(
                                        initialAggregationNode,
                                        buildSubAggregateTree(g.get(0), childConstructionNode, distinctDescendantNode, subConstructionDescendantNode));
                            default:
                                IQTree lowUnion = iqFactory.createNaryIQTree(
                                        iqFactory.createUnionNode(initialAggregationNode.getChildVariables()),
                                        g);

                                return iqFactory.createUnaryIQTree(
                                        initialAggregationNode,
                                        buildSubAggregateTree(lowUnion, childConstructionNode, distinctDescendantNode, subConstructionDescendantNode));
                        }
                    })
                    .map(t -> renameSomeUnprojectedVariables(t, nonGroupingVariables))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree(
                    iqFactory.createUnionNode(initialAggregationNode.getVariables()),
                    topUnionChildren);
        }

        private IQTree buildSubAggregateTree(IQTree bottomTree, Optional<ConstructionNode> childConstructionNode,
                                             Optional<DistinctNode> distinctDescendantNode,
                                             Optional<ConstructionNode> subConstructionDescendantNode) {
            IQTree level3Tree = subConstructionDescendantNode
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, bottomTree))
                    .orElse(bottomTree);

            IQTree level2Tree = distinctDescendantNode
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, level3Tree))
                    .orElse(level3Tree);

            return childConstructionNode
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, level2Tree))
                    .orElse(level2Tree);
        }

        private IQTree renameSomeUnprojectedVariables(IQTree tree, Set<Variable> nonGroupingVariables) {
            InjectiveVar2VarSubstitution renaming = substitutionFactory.getInjectiveVar2VarSubstitution(
                    nonGroupingVariables.stream(), variableGenerator::generateNewVariableFromVar);

            return tree.applyFreshRenamingToAllVariables(renaming);
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
