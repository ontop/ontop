package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Singleton
public class IQTreeTools {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final QueryRenamer queryRenamer;

    @Inject
    private IQTreeTools(IntermediateQueryFactory iqFactory, TermFactory termFactory, SubstitutionFactory substitutionFactory, QueryRenamer queryRenamer) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.queryRenamer = queryRenamer;
    }

    public static ImmutableSet<Variable> computeStrictDependentsFromFunctionalDependencies(IQTree tree) {
        FunctionalDependencies functionalDependencies = tree.inferFunctionalDependencies();
        ImmutableSet<Variable> dependents = functionalDependencies.stream()
                .flatMap(e -> e.getValue().stream())
                .collect(ImmutableCollectors.toSet());
        ImmutableSet<Variable> determinants = functionalDependencies.stream()
                .flatMap(e -> e.getKey().stream())
                .collect(ImmutableCollectors.toSet());
        return Sets.difference(dependents, determinants).immutableCopy();
    }


    public EmptyNode createEmptyNode(ImmutableSet<Variable> projectedVariables) {
        return iqFactory.createEmptyNode(projectedVariables);
    }

    public IQ createMappingIQ(DistinctVariableOnlyDataAtom atom, Substitution<?> substitution, IQTree child) {
        return iqFactory.createIQ(atom,
                iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(atom.getVariables(), substitution),
                        child));
    }

    public Optional<FilterNode> createOptionalFilterNode(Optional<ImmutableExpression> expression) {
        return expression.map(iqFactory::createFilterNode);
    }

    public Optional<OrderByNode> createOptionalOrderByNode(ImmutableList<OrderByNode.OrderComparator> comparators) {
        return comparators.isEmpty()
                ? Optional.empty()
                : Optional.of(iqFactory.createOrderByNode(comparators));
    }

    public ImmutableList<OrderByNode.OrderComparator> transformComparators(ImmutableList<OrderByNode.OrderComparator> comparators, Function<? super NonGroundTerm, ImmutableTerm> transformer) {
        return comparators.stream()
                .flatMap(c -> Stream.of(c.getTerm())
                        .map(transformer)
                        .filter(t -> t instanceof NonGroundTerm)
                        .map(t -> (NonGroundTerm) t)
                        .map(t -> iqFactory.createOrderComparator(t, c.isAscending())))
                .collect(ImmutableCollectors.toList());
    }

    public ConstructionNode createExtendingConstructionNode(Set<Variable> subTreeVariables, Substitution<?> extendingSubstitution) {
        return iqFactory.createConstructionNode(
                Sets.union(subTreeVariables, extendingSubstitution.getDomain()).immutableCopy(),
                extendingSubstitution);
    }

    public <T extends ImmutableTerm> ConstructionNode replaceSubstitution(ConstructionNode cn, Function<Substitution<ImmutableTerm>, Substitution<T>> substitutionTransformer) {
        return iqFactory.createConstructionNode(
                cn.getVariables(),
                substitutionTransformer.apply(cn.getSubstitution()));
    }

    public Optional<ConstructionNode> createOptionalConstructionNode(Supplier<ImmutableSet<Variable>> projectedVariables, Substitution<?> substitution) {
        return substitution.isEmpty()
            ? Optional.empty()
            : Optional.of(iqFactory.createConstructionNode(projectedVariables.get(), substitution));
    }

    public Optional<ConstructionNode> createOptionalConstructionNode(ImmutableSet<Variable> signature, Substitution<?> substitution, IQTree newTree) {
        return substitution.isEmpty() && signature.equals(newTree.getVariables())
                ? Optional.empty()
                : Optional.of(iqFactory.createConstructionNode(signature, substitution));
    }

    public Optional<DistinctNode> createOptionalDistinctNode(boolean f) {
        return f ? Optional.of(iqFactory.createDistinctNode()) : Optional.empty();
    }

    public IQTree createUnionTree(ImmutableSet<Variable> variables, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(iqFactory.createUnionNode(variables), children);
    }

    public Optional<IQTree> createOptionalInnerJoinTree(Optional<ImmutableExpression> filter, ImmutableList<? extends IQTree> list) {
        switch (list.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(unaryIQTreeBuilder()
                        .append(createOptionalFilterNode(filter))
                        .build(list.get(0)));
            default:
                return Optional.of(createInnerJoinTree(filter, (ImmutableList<IQTree>)list));
        }
    }

    public NaryIQTree createInnerJoinTree(Optional<ImmutableExpression> filter, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(filter), children);
    }

    public NaryIQTree createInnerJoinTree(ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(), children);
    }

    public BinaryNonCommutativeIQTree createLeftJoinTree(Optional<ImmutableExpression> filter, IQTree leftChild, IQTree rightChild) {
        return iqFactory.createBinaryNonCommutativeIQTree(iqFactory.createLeftJoinNode(filter), leftChild, rightChild);
    }

    public BinaryNonCommutativeIQTree createLeftJoinTree(ImmutableExpression filter, IQTree leftChild, IQTree rightChild) {
        return iqFactory.createBinaryNonCommutativeIQTree(iqFactory.createLeftJoinNode(filter), leftChild, rightChild);
    }

    public <T extends UnaryOperatorNode> UnaryIQTreeBuilder<T> unaryIQTreeBuilder() {
        return new UnaryIQTreeBuilder<>(iqFactory, ImmutableList.of(), ImmutableMap.of(), Optional.empty());
    }

    public <T extends UnaryOperatorNode> UnaryIQTreeBuilder<T> unaryIQTreeBuilder(ImmutableSet<Variable> signature) {
        return new UnaryIQTreeBuilder<>(iqFactory, ImmutableList.of(), ImmutableMap.of(), Optional.of(signature));
    }

    public ImmutableExpression getConjunction(ImmutableExpression expression1, ImmutableExpression expression2) {
        return termFactory.getConjunction(expression1, expression2);
    }

    public ImmutableExpression getConjunction(Optional<ImmutableExpression> optionalExpression, ImmutableExpression expression) {
        return optionalExpression
                .map(c -> termFactory.getConjunction(c, expression))
                .orElse(expression);
    }

    public Optional<ImmutableExpression> getConjunction(Optional<ImmutableExpression> optionalExpression1, Optional<ImmutableExpression> optionalExpression2) {
        return termFactory.getConjunction(Stream.concat(optionalExpression1.stream(), optionalExpression2.stream()));
    }

    // TODO: to be eliminated later, but some tests depend on the order of conjuncts
    public ImmutableExpression getConjunction(ImmutableExpression expression, Optional<ImmutableExpression> optionalExpression) {
        return optionalExpression
                .map(c -> termFactory.getConjunction(expression, c))
                .orElse(expression);
    }

    public static class IQTreeDecomposition<N extends QueryNode, T extends IQTree> {
        protected final N node; // nullable
        protected final T tree; // nullable

        protected IQTreeDecomposition(N node, T tree) {
            this.node = node;
            this.tree = tree;
        }

        @Nonnull
        public Optional<N> getOptionalNode() {
            return Optional.ofNullable(node);
        }

        public boolean isPresent() {
            return node != null;
        }

        @Nonnull
        public N getNode() {
            return Objects.requireNonNull(node);
        }

        @Nonnull
        public T getTree() {
            return Objects.requireNonNull(tree);
        }
    }


    public static <T extends QueryNode> boolean contains(IQTree tree, Class<T> nodeClass) {
        return nodeClass.isInstance(tree.getRootNode()) ||
                tree.getChildren().stream().anyMatch(t -> contains(t, nodeClass));
    }


    public DownPropagation getDownPropagation(ConditionSimplifier.ExpressionAndSubstitution expressionAndSubstitution, ImmutableSet<Variable> projectedVariables, VariableGenerator variablesGenerator) throws DownPropagation.InconsistentDownPropagationException {
        return createDownPropagation(expressionAndSubstitution.getSubstitution(), expressionAndSubstitution.getOptionalExpression(), projectedVariables, variablesGenerator);
    }

    /**
     * Creates a down propagation object, which consists of a descending substitution and a constraint.
     *
     * The descending substitution is applied to a given IQTree:
     * each free occurrence of a variable from the descending substitution's domain
     * is replaced by the respective term in the IQTree.
     * <p>
     * The constraint is an expression (usually, a set of equalities)
     * assumed to be applied (like a filter) after the descending substitution
     * (the constraint thus can use the variables introduced by the descending substitution).
     * <p>
     * The constructed down propagation object contains a descending substitution and a constraint
     * appropriately restricted:<ul>
     *  <li>the domain of the descending substitution is restricted to the set of variables, and</li>
     *  <li>all the components of the constraint that have no variable from the set of variables</li>
     *  <emph>after</emph> the descending substitution is applied.</ul>
     *
     * @param descendingSubstitution a given unrestricted descending substitution
     * @param optionalConstraint an optional unrestricted constraint
     * @param variables the set of variables projected by the IQTree, to which the down propagation will be applied
     * @param variableGenerator a variable generator for the enclosing IQTree
     * @return a down propagation object
     *
     * @throws DownPropagation.InconsistentDownPropagationException if a "null" variable is propagated down
     */

    public DownPropagation createDownPropagation(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                 Optional<ImmutableExpression> optionalConstraint,
                                                 ImmutableSet<Variable> variables,
                                                 VariableGenerator variableGenerator) throws DownPropagation.InconsistentDownPropagationException {
        return AbstractDownPropagation.createDownPropagation(descendingSubstitution, optionalConstraint, variables, variableGenerator, termFactory);
    }


    /**
     * Creates a down propagation object, which consists of a constraint only
     * (this is simply a faster version of {@link IQTreeTools#createDownPropagation(Substitution, Optional, ImmutableSet, VariableGenerator)}.
     * <p>
     * The constraint is an expression (usually, a set of equalities).
     * <p>
     * The constructed down propagation object contains a constraint appropriately restricted:<ul>
     *  <li>all the components of the constraint that have no variable from the set of variables.</li></ul>
     *
     * @param optionalConstraint an optional unrestricted constraint
     * @param variables the set of variables projected by the IQTree, to which the down propagation will be applied
     * @param variableGenerator a variable generator for the enclosing IQTree
     * @return a down propagation object
     */

    public DownPropagation createDownPropagation(Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        return AbstractDownPropagation.createDownPropagation(optionalConstraint, variables, variableGenerator, termFactory);
    }


    /**
     * Applies renaming to the projected variables in the IQTree.
     *
     * @param renaming an injective variable-to-variable substitution
     * @param tree an IQTree
     * @return resulting IQTree
     */

    public IQTree applyDownPropagation(InjectiveSubstitution<Variable> renaming, IQTree tree) {
        DownPropagation dp = createDownPropagation(renaming, tree.getVariables());
        return dp.propagate(tree);
    }

    private DownPropagation createDownPropagation(InjectiveSubstitution<Variable> renaming, ImmutableSet<Variable> variables) {
        InjectiveSubstitution<Variable> restriction = renaming.restrictDomainTo(variables);
        // variable generator is null as it is not used in the implementation of propagation
        return restriction.isEmpty()
                ? new ConstraintOnlyDownPropagation(Optional.empty(), variables, null, termFactory)
                : new RenamingDownPropagation(restriction, Optional.empty(), variables, null, termFactory);
    }

    /**
     * Creates a fresh copy of a given IQ
     * and updated the variable generator with the fresh variable names.
     *
     * @param iq IQ
     * @param variableGenerator a variable generator
     * @return a fresh copy of the IQ
     */

    public IQ getFreshInstance(IQ iq, VariableGenerator variableGenerator) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(
                variableGenerator, iq.getTree().getKnownVariables());

        return queryRenamer.applyInDepthRenaming(renamingSubstitution, iq);
    }

    public <T extends ImmutableTerm> Stream<ImmutableExpression> getRemainingEqualitiesInverse(ImmutableList<? extends Map.Entry<T, ? extends ImmutableTerm>> equalities, Substitution<T> sub) {
        return equalities.stream()
                .map(e -> Maps.immutableEntry(e.getValue(), e.getKey())) // swapped!
                .filter(e -> sub.stream().noneMatch(e::equals))
                .map(e -> termFactory.getStrictEquality(sub.applyToTerm(e.getKey()), e.getValue()));
    }

    public <T extends ImmutableTerm> Stream<ImmutableExpression> getRemainingEqualitiesSimple(Substitution<? extends ImmutableTerm> equalities, Substitution<T> sub) {
        return equalities.stream()
                .map(e -> Maps.immutableEntry(e.getKey(), e.getValue()))
                .filter(e -> sub.stream().noneMatch(e::equals))
                .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue())); // no sub!
    }

    public IQTree createFilterTreeForBlockedSubstitution(Substitution<? extends ImmutableTerm> blockedSubstitution, IQTree tree, ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator) {
        if (blockedSubstitution.isEmpty())
            return tree;

        // Blocked entries -> reconverted into a filter
        ImmutableExpression condition = termFactory.getConjunction(
                blockedSubstitution.builder().toStream(termFactory::getStrictEquality).collect(ImmutableCollectors.toList()));

        InjectiveSubstitution<Variable> renaming = condition.getVariableStream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

        IQTree filterTree = applyDownPropagation(renaming,
                iqFactory.createUnaryIQTree(iqFactory.createFilterNode(condition), tree));

        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(projectedVariables),
                filterTree);
    }
}
