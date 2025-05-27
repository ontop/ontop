package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
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

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


public class ProjectOrderByTermsNormalizer implements DialectExtraNormalizer {

    private final boolean onlyInPresenceOfDistinct;
    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;

    protected ProjectOrderByTermsNormalizer(boolean onlyInPresenceOfDistinct, CoreSingletons coreSingletons) {
        this.onlyInPresenceOfDistinct = onlyInPresenceOfDistinct;
        this.iqFactory = coreSingletons.getIQFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
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
            var slice = UnaryIQTreeDecomposition.of(tree, SliceNode.class);
            var distinct = UnaryIQTreeDecomposition.of(slice.getTail(), DistinctNode.class);
            var construction = UnaryIQTreeDecomposition.of(distinct.getTail(), ConstructionNode.class);
            var orderBy = UnaryIQTreeDecomposition.of(construction.getTail(), OrderByNode.class);

            var initialDescendantTree = orderBy.getTail();

            //Recursive
            IQTree newDescendantTree = transform(initialDescendantTree);
            IQTree newTree = iqTreeTools.unaryIQTreeBuilder()
                    .append(slice.getOptionalNode())
                    .append(distinct.getOptionalNode())
                    .append(construction.getOptionalNode())
                    .append(orderBy.getOptionalNode())
                    .build(newDescendantTree);

            return orderBy
                    .getOptionalNode()
                    .filter(o -> distinct.isPresent() || (!onlyInPresenceOfDistinct))
                    .map(o -> new Analysis(distinct.isPresent(), construction.getOptionalNode(), o.getComparators(),
                            distinct.isPresent()
                                    ? newDescendantTree
                                            .normalizeForOptimization(variableGenerator)
                                            .inferFunctionalDependencies()
                                    : FunctionalDependencies.empty()))
                    .map(a -> normalize(newTree, a))
                    .orElse(newTree);
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
            var construct = UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
            if (construct.isPresent())
                return iqFactory.createUnaryIQTree(newConstructionNode, construct.getChild());

            // Recursive
            var unary = UnaryIQTreeDecomposition.of(tree, UnaryOperatorNode.class);
            if (unary.isPresent())
                 return iqFactory.createUnaryIQTree(unary.getNode(), updateTopConstructionNode(unary.getChild(), newConstructionNode));

            throw new MinorOntopInternalBugException("Was expected to reach a ConstructionNode before a non-unary node");
        }

        /**
         * Recursive
         */
        private IQTree insertConstructionNode(IQTree tree, ConstructionNode newConstructionNode) {
            // Recursive
            var distinct = UnaryIQTreeDecomposition.of(tree, DistinctNode.class);
            if (distinct.isPresent())
                    return iqFactory.createUnaryIQTree(
                            distinct.getNode(),
                            insertConstructionNode(distinct.getChild(), newConstructionNode));

            // Recursive
            var slice = UnaryIQTreeDecomposition.of(tree, SliceNode.class);
            if (slice.isPresent())
                return iqFactory.createUnaryIQTree(
                        slice.getNode(),
                        insertConstructionNode(slice.getChild(), newConstructionNode));

            return iqFactory.createUnaryIQTree(newConstructionNode, tree);
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
}
