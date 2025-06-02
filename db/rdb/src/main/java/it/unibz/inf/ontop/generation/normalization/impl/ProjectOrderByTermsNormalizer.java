package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
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
            return transformSliceDistinctConstructionOrderByTree(tree);
        }

        @Override
        public IQTree transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
            return transformSliceDistinctConstructionOrderByTree(tree);
        }

        @Override
        public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
            return transformSliceDistinctConstructionOrderByTree(tree);
        }

        @Override
        public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
            return transformSliceDistinctConstructionOrderByTree(tree);
        }

        private IQTree transformSliceDistinctConstructionOrderByTree(IQTree tree) {
            var slice = UnaryIQTreeDecomposition.of(tree, SliceNode.class);
            var distinct = UnaryIQTreeDecomposition.of(slice, DistinctNode.class);
            var construction = UnaryIQTreeDecomposition.of(distinct, ConstructionNode.class);
            var orderBy = UnaryIQTreeDecomposition.of(construction, OrderByNode.class);

            var initialDescendantTree = orderBy.getTail();

            //Recursive
            IQTree newDescendantTree = transform(initialDescendantTree);

            Optional<ConstructionNode> newOptionalConstructionNode = orderBy.isPresent()
                    ? normalize(distinct.getOptionalNode(),
                            construction.getOptionalNode(),
                            orderBy.getNode(),
                            newDescendantTree)
                        .or(construction::getOptionalNode)
                    : construction.getOptionalNode();

            return iqTreeTools.unaryIQTreeBuilder()
                            .append(slice.getOptionalNode())
                            .append(distinct.getOptionalNode())
                            .append(newOptionalConstructionNode)
                            .append(orderBy.getOptionalNode())
                            .build(newDescendantTree);
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<ConstructionNode> normalize(Optional<DistinctNode> optionalDistinct,
                                           Optional<ConstructionNode> optionalConstruction,
                                           OrderByNode orderBy,
                                           IQTree newDescendantTree) {

            if (!(optionalDistinct.isPresent() || (!onlyInPresenceOfDistinct)))
                return Optional.empty();

            FunctionalDependencies descendantTreeFunctionalDependencies = optionalDistinct.isPresent()
                    ? newDescendantTree
                            .normalizeForOptimization(variableGenerator)
                            .inferFunctionalDependencies()
                    : FunctionalDependencies.empty();

            ImmutableSet<Variable> projectedVariables = optionalConstruction.map(ConstructionNode::getVariables)
                    .orElseGet(newDescendantTree::getVariables);

            var substitution = optionalConstruction.map(ConstructionNode::getSubstitution)
                    .orElseGet(substitutionFactory::getSubstitution);

            ImmutableSet<ImmutableTerm> alreadyDefinedTerms = Sets.union(
                            projectedVariables,
                            substitution.getRangeSet())
                    .immutableCopy();

            ImmutableMap<Variable, NonGroundTerm> newBindings = orderBy.getComparators().stream()
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
                return Optional.empty();

            // decides whether the new bindings can be added
            if (optionalDistinct.isPresent() && newBindings.values().stream()
                    .anyMatch(t -> mayImpactDistinct(t, alreadyDefinedTerms, descendantTreeFunctionalDependencies))) {
                throw new DistinctOrderByDialectLimitationException();
            }

            ImmutableSet<Variable> newProjectedVariables = Sets.union(projectedVariables, newBindings.keySet()).immutableCopy();

            Substitution<ImmutableTerm> newSubstitution = substitutionFactory.union(
                    newBindings.entrySet().stream().collect(substitutionFactory.toSubstitutionSkippingIdentityEntries()),
                    substitution);

            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(newProjectedVariables, newSubstitution);

            return Optional.of(newConstructionNode);
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
