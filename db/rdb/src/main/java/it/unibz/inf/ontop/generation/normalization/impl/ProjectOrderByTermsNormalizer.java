package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.NonDeterministicDBFunctionSymbol;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


public class ProjectOrderByTermsNormalizer implements DialectExtraNormalizer {

    private final boolean onlyInPresenceOfDistinct;
    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;

    protected ProjectOrderByTermsNormalizer(boolean onlyInPresenceOfDistinct, CoreSingletons coreSingletons) {
        this.onlyInPresenceOfDistinct = onlyInPresenceOfDistinct;
        this.iqFactory = coreSingletons.getIQFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptTransformer(new Transformer(variableGenerator));
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        private final VariableGenerator variableGenerator;

        Transformer(VariableGenerator variableGenerator) {
            super(ProjectOrderByTermsNormalizer.this.iqFactory);
            this.variableGenerator = variableGenerator;
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            return transformConstructionSliceDistinctOrOrderByTree(tree);
        }

        @Override
        public IQTree transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
            return transformConstructionSliceDistinctOrOrderByTree(tree);
        }

        @Override
        public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
            return transformConstructionSliceDistinctOrOrderByTree(tree);
        }

        @Override
        public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
            return transformConstructionSliceDistinctOrOrderByTree(tree);
        }

        private IQTree transformConstructionSliceDistinctOrOrderByTree(IQTree tree) {
            IQTree newTree = applyTransformerToDescendantTree(tree);

            Decomposition decomposition = Decomposition.decomposeTree(newTree);

            return decomposition.orderByNode
                    .filter(o -> decomposition.distinctNode.isPresent() || (!onlyInPresenceOfDistinct))
                    .map(o -> new Analysis(decomposition.distinctNode.isPresent(), decomposition.constructionNode, o.getComparators(),
                            decomposition.distinctNode.isPresent()
                                    ? decomposition.descendantTree
                                    .normalizeForOptimization(variableGenerator)
                                    .inferFunctionalDependencies()
                                    : FunctionalDependencies.empty()))
                    .map(a -> normalize(newTree, a))
                    .orElse(newTree);
        }

        /**
         * Applies the transformer to a descendant tree, and rebuilds the parent tree.
         *
         * The root node of the parent tree must be a ConstructionNode, a SliceNode, a DistinctNode or an OrderByNode
         */
        private IQTree applyTransformerToDescendantTree(IQTree tree) {
            Decomposition decomposition = Decomposition.decomposeTree(tree);

            //Recursive
            IQTree newDescendantTree = transform(decomposition.descendantTree);

            return decomposition.descendantTree.equals(newDescendantTree)
                    ? tree
                    : decomposition.rebuildWithNewDescendantTree(newDescendantTree, iqFactory);
        }

        private IQTree normalize(IQTree tree, Analysis analysis) {

            ImmutableSet<Variable> projectedVariables = tree.getVariables();

            ImmutableSet<ImmutableTerm> alreadyDefinedTerms = Sets.union(
                            projectedVariables,
                            analysis.getSubstitution().getRangeSet())
                    .immutableCopy();

            ImmutableMap<Variable, NonGroundTerm> newBindings = analysis.sortConditions.stream()
                    .map(OrderByNode.OrderComparator::getTerm)
                    .filter(t -> !alreadyDefinedTerms.contains(t))
                    .distinct() // keep only the first occurrence of the sorting term
                    .map(t -> Maps.immutableEntry(
                            (t instanceof Variable)
                                    ? (Variable) t
                                    : variableGenerator.generateNewVariable(),
                            t))
                    .collect(ImmutableCollectors.toMap());

            if (newBindings.isEmpty())
                return tree;

            // decides whether the new bindings can be added
            if (analysis.hasDistinct && newBindings.values().stream()
                    .anyMatch(t -> mayImpactDistinct(t, alreadyDefinedTerms, analysis.descendantTreeFunctionalDependencies))) {
                throw new DistinctOrderByDialectLimitationException();
            }

            ImmutableSet<Variable> newProjectedVariables = Sets.union(projectedVariables, newBindings.keySet()).immutableCopy();

            Substitution<ImmutableTerm> newSubstitution = substitutionFactory.union(
                    newBindings.entrySet().stream().collect(substitutionFactory.toSubstitutionSkippingIdentityEntries()),
                    analysis.getSubstitution());

            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(newProjectedVariables, newSubstitution);

            return analysis.constructionNode
                    .map(n -> updateTopConstructionNode(tree, newConstructionNode))
                    .orElseGet(() -> insertConstructionNode(tree, newConstructionNode));
        }

        /**
         * TODO: explain
         */
        private boolean mayImpactDistinct(ImmutableTerm term, ImmutableSet<ImmutableTerm> alreadyProjectedTerms,
                FunctionalDependencies descendantTreeFunctionalDependencies) {
            if (term instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;
                if (functionalTerm.getFunctionSymbol() instanceof NonDeterministicDBFunctionSymbol)
                    return true;
                else if (alreadyProjectedTerms.contains(term))
                    return false;
                else
                    return functionalTerm.getTerms().stream()
                            // Recursive
                            .anyMatch(t -> mayImpactDistinct(t, alreadyProjectedTerms, descendantTreeFunctionalDependencies));
            }
            else if (term instanceof Variable) {
                if (alreadyProjectedTerms.contains(term))
                    return false;
                return descendantTreeFunctionalDependencies.getDeterminantsOf((Variable) term).stream()
                        .noneMatch(alreadyProjectedTerms::containsAll);
            }
            // Constant
            else
                return false;
        }

        /**
         * Recursive
         */
        private IQTree updateTopConstructionNode(IQTree tree, ConstructionNode newConstructionNode) {
            return UnaryIQTreeDecomposition.of(tree, ConstructionNode.class)
                    .map((cn, t) -> iqFactory.createUnaryIQTree(newConstructionNode, t))

                    .or(() -> UnaryIQTreeDecomposition.of(tree, UnaryOperatorNode.class)
                            // Recursive
                            .map((u, t) -> iqFactory.createUnaryIQTree(u, updateTopConstructionNode(t, newConstructionNode))))

                    .orElseThrow(() -> new MinorOntopInternalBugException("Was expected to reach a ConstructionNode before a non-unary node"));
        }

        /**
         * Recursive
         */
        private IQTree insertConstructionNode(IQTree tree, ConstructionNode newConstructionNode) {
            return UnaryIQTreeDecomposition.of(tree, DistinctNode.class)
                    // Recursive
                    .map((n, t) -> iqFactory.createUnaryIQTree(n, insertConstructionNode(t, newConstructionNode)))

                    .or(() -> UnaryIQTreeDecomposition.of(tree, SliceNode.class)
                            // Recursive
                            .map((n, t) -> iqFactory.createUnaryIQTree(n, insertConstructionNode(t, newConstructionNode))))

                    .orElseGet(() -> iqFactory.createUnaryIQTree(newConstructionNode, tree));
        }
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final class Analysis {
        final boolean hasDistinct;
        final Optional<ConstructionNode> constructionNode;
        final ImmutableList<OrderByNode.OrderComparator> sortConditions;
        final FunctionalDependencies descendantTreeFunctionalDependencies;

        private Analysis(boolean hasDistinct, Optional<ConstructionNode> constructionNode,
                         ImmutableList<OrderByNode.OrderComparator> sortConditions,
                         FunctionalDependencies descendantTreeFunctionalDependencies) {
            this.hasDistinct = hasDistinct;
            this.constructionNode = constructionNode;
            this.sortConditions = sortConditions;
            this.descendantTreeFunctionalDependencies = descendantTreeFunctionalDependencies;
        }

        Substitution<ImmutableTerm> getSubstitution() {
            return constructionNode.map(ConstructionNode::getSubstitution).orElseGet(substitutionFactory::getSubstitution);
        }
    }

    /**
     * Supposed to be an "internal bug" has the restriction should have been detected before, at the SPARQL level
     * (detection to be implemented)
     */
    private static class DistinctOrderByDialectLimitationException extends OntopInternalBugException {

        protected DistinctOrderByDialectLimitationException() {
            super("The dialect requires ORDER BY conditions to be projected but a DISTINCT prevents some of them");
        }
    }

    /**
     * As the ancestors, we may get the following nodes: OrderByNode, ConstructionNode, DistinctNode, SliceNode.
     * NB: this order is bottom-up.
     * Some nodes may be missing but the order must be respected. There are no multiple instances of the same kind of node.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static class Decomposition {
        final Optional<SliceNode> sliceNode;
        final Optional<DistinctNode> distinctNode;
        final Optional<ConstructionNode> constructionNode;
        final Optional<OrderByNode> orderByNode;

        final IQTree descendantTree;

        private Decomposition(Optional<SliceNode> sliceNode,
                              Optional<DistinctNode> distinctNode,
                              Optional<ConstructionNode> constructionNode,
                              Optional<OrderByNode> orderByNode,
                              IQTree descendantTree) {
            this.sliceNode = sliceNode;
            this.distinctNode = distinctNode;
            this.constructionNode = constructionNode;
            this.orderByNode = orderByNode;
            this.descendantTree = descendantTree;
        }

        IQTree rebuildWithNewDescendantTree(IQTree newDescendantTree, IntermediateQueryFactory iqFactory) {
            return Stream.of(orderByNode, constructionNode, distinctNode, sliceNode)
                    .flatMap(Optional::stream)
                    .reduce(newDescendantTree,
                            (t, n) -> iqFactory.createUnaryIQTree(n, t),
                            (t1, t2) -> { throw new MinorOntopInternalBugException("Must not be run in parallel"); });
        }

        static Decomposition decomposeTree(IQTree tree) {
            var slice = UnaryIQTreeDecomposition.of(tree, SliceNode.class);
            var distinct = UnaryIQTreeDecomposition.of(slice.getChild(), DistinctNode.class);
            var construction = UnaryIQTreeDecomposition.of(distinct.getChild(), ConstructionNode.class);
            var orderBy = UnaryIQTreeDecomposition.of(construction.getChild(), OrderByNode.class);

            return new Decomposition(
                    slice.getOptionalNode(),
                    distinct.getOptionalNode(),
                    construction.getOptionalNode(),
                    orderBy.getOptionalNode(),
                    orderBy.getChild());
        }
    }
}
