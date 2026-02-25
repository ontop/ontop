package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
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

import static it.unibz.inf.ontop.iq.impl.UnaryIQTreeTools.UnaryIQTreeDecomposition;

/*public*/ final class ProjectOrderByTermsTransformer extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {

    private final boolean onlyInPresenceOfDistinct;
    private final SubstitutionFactory substitutionFactory;
    private final IQTreeTools iqTreeTools;

    ProjectOrderByTermsTransformer(VariableGenerator variableGenerator, boolean onlyInPresenceOfDistinct, CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory(), variableGenerator);
        this.onlyInPresenceOfDistinct = onlyInPresenceOfDistinct;
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
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
            throw new MinorOntopInternalBugException("The dialect requires ORDER BY conditions to be projected but a DISTINCT prevents some of them");
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
