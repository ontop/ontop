package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Singleton
public class IQTreeTools {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    private IQTreeTools(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
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

    /**
     * Excludes the variables that are not projected by the IQTree
     *
     * If a "null" variable is propagated down, throws an UnsatisfiableDescendingSubstitutionException.
     *
     */
    public Optional<Substitution<? extends VariableOrGroundTerm>> normalizeDescendingSubstitution(
            IQTree tree, Substitution<? extends VariableOrGroundTerm> descendingSubstitution)
            throws UnsatisfiableDescendingSubstitutionException {

        Substitution<? extends VariableOrGroundTerm> reducedSubstitution = descendingSubstitution.restrictDomainTo(tree.getVariables());

        if (reducedSubstitution.isEmpty())
            return Optional.empty();

        if (reducedSubstitution.rangeAnyMatch(ImmutableTerm::isNull)) {
            throw new UnsatisfiableDescendingSubstitutionException();
        }

        return Optional.of(reducedSubstitution);
    }

    public ImmutableSet<Variable> computeNewProjectedVariables(
            Substitution<? extends ImmutableTerm> descendingSubstitution,
            ImmutableSet<Variable> projectedVariables) {

        ImmutableSet<Variable> newVariables = descendingSubstitution.restrictDomainTo(projectedVariables).getRangeVariables();

        return Sets.union(newVariables, Sets.difference(projectedVariables, descendingSubstitution.getDomain())).immutableCopy();
    }

    public IQTree createConstructionNodeTreeIfNontrivial(IQTree child, Substitution<?> substitution, Supplier<ImmutableSet<Variable>> projectedVariables) {
        return substitution.isEmpty()
                ? child
                : iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(projectedVariables.get(), substitution), child);
    }

    public IQTree createConstructionNodeTreeIfNontrivial(IQTree child, ImmutableSet<Variable> variables) {
        return child.getVariables().equals(variables)
                ? child
                : iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(variables), child);
    }

    // TODO: merge with above
    public IQTree createIQTreeWithSignature(ImmutableSet<Variable> signature, IQTree child) {
        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(signature),
                child);
    }

    public ImmutableSet<Variable> getChildrenVariables(ImmutableList<IQTree> children) {
         return children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());
    }

    public ImmutableSet<Variable> getChildrenVariables(IQTree leftChild, IQTree rightChild) {
        return Sets.union(leftChild.getVariables(), rightChild.getVariables()).immutableCopy();
    }

    public ImmutableSet<Variable> getChildrenVariables(IQTree child, Variable newVariable) {
        return Sets.union(child.getVariables(), ImmutableSet.of(newVariable)).immutableCopy();
    }

    public ImmutableSet<Variable> extractChildVariables(ImmutableSet<Variable> groupingVariables,
                                                               Substitution<ImmutableFunctionalTerm> substitution) {
        return Sets.union(groupingVariables, substitution.getRangeVariables()).immutableCopy();
    }

    public LeftJoinNode updateLeftJoinNodeWithConjunct(LeftJoinNode leftJoinNode, Optional<ImmutableExpression> conjunct) {
        return conjunct
                .map(c -> iqFactory.createLeftJoinNode(termFactory.getConjunction(leftJoinNode.getOptionalFilterCondition(), Stream.of(c))))
                .orElse(leftJoinNode);
    }

    public InnerJoinNode updateInnerJoinNodeWithConjunct(InnerJoinNode innerJoinNode, Optional<ImmutableExpression> conjunct) {
        return conjunct
                .map(c -> iqFactory.createInnerJoinNode(termFactory.getConjunction(innerJoinNode.getOptionalFilterCondition(), Stream.of(c))))
                .orElse(innerJoinNode);
    }

    public IQTree createOptionalUnaryIQTree(Optional<? extends UnaryOperatorNode> optionalNode, IQTree tree) {
        return optionalNode
                .<IQTree>map(n -> iqFactory.createUnaryIQTree(n, tree))
                .orElse(tree);
    }

    public IQTree createAncestorsUnaryIQTree(ImmutableList<? extends UnaryOperatorNode> ancestors, IQTree tree) {
        return ancestors.stream()
                .reduce(tree,
                        (t, a) -> iqFactory.createUnaryIQTree(a, t),
                        (t1, t2) -> { throw new MinorOntopInternalBugException("No merge was expected"); });
    }

    public IQTree createOptionalAncestorsUnaryIQTree(ImmutableList<Optional<? extends UnaryOperatorNode>> ancestors, IQTree tree) {
        return ancestors.stream()
                .flatMap(Optional::stream)
                .collect(ImmutableCollectors.toList())
                .reverse().stream()
                .reduce(tree,
                        (t, a) -> iqFactory.createUnaryIQTree(a, t),
                        (t1, t2) -> { throw new MinorOntopInternalBugException("No merge was expected"); });
    }

    public ImmutableList<IQTree> createUnaryOperatorChildren(UnaryOperatorNode node, IQTree child) {
         return child.getChildren().stream()
                .<IQTree>map(c -> iqFactory.createUnaryIQTree(node, c))
                .collect(ImmutableCollectors.toList());
    }


    public static class UnaryIQTreeDecomposition<T extends UnaryOperatorNode> {
        private final T node; // nullable
        private final IQTree child;

        private UnaryIQTreeDecomposition(T node, IQTree child) {
            this.node = node;
            this.child = child;
        }

        public Optional<T> getOptionalNode() {
            return Optional.ofNullable(node);
        }

        public IQTree getChild() {
            return child;
        }

        public boolean isPresent() {
            return node != null ;
        }

        public T get() {
            return Objects.requireNonNull(node);
        }

        public <U> Optional<U> map(BiFunction<? super T, IQTree, ? extends U> function) {
            return Optional.ofNullable(node).map(n -> function.apply(n, child));
        }

        public static <T extends UnaryOperatorNode> UnaryIQTreeDecomposition<T> of(IQTree tree, Class<T> nodeClass) {
            return nodeClass.isInstance(tree.getRootNode())
                    ? new UnaryIQTreeDecomposition<>(nodeClass.cast(tree.getRootNode()), ((UnaryIQTree)tree).getChild())
                    : new UnaryIQTreeDecomposition<>(null, tree);
        }

        public static <T extends UnaryOperatorNode> ImmutableList<UnaryIQTreeDecomposition<T>> of(ImmutableList<IQTree> list, Class<T> nodeClass) {
            return list.stream()
                    .map(c -> UnaryIQTreeDecomposition.of(c, nodeClass))
                    .collect(ImmutableCollectors.toList());
        }

        public static <T extends UnaryOperatorNode> ImmutableList<IQTree> getChildren(ImmutableList<UnaryIQTreeDecomposition<T>> list) {
            return list.stream()
                    .map(UnaryIQTreeDecomposition::getChild)
                    .collect(ImmutableCollectors.toList());
        }
    }


    /**
     * If the substitution is a fresh renaming, returns it as an injective substitution
     */
    public Optional<InjectiveSubstitution<Variable>> extractFreshRenaming(Substitution<? extends ImmutableTerm> descendingSubstitution,
                                                                          ImmutableSet<Variable> projectedVariables) {

        Substitution<Variable> var2VarFragment = descendingSubstitution.restrictRangeTo(Variable.class);
        int size = descendingSubstitution.getDomain().size();

        if (var2VarFragment.getDomain().size() != size
                || Sets.difference(var2VarFragment.getRangeSet(), projectedVariables).size() != size)
            return Optional.empty();

        return Optional.of(var2VarFragment.injective());
    }

    public Substitution<NonVariableTerm> getEmptyNonVariableSubstitution() {
        return substitutionFactory.getSubstitution();
    }


    /**
     * Typically thrown when a "null" variable is propagated down
     *
     */
    public static class UnsatisfiableDescendingSubstitutionException extends Exception {
    }


}
