package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.PreventDistinctOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PreventDistinctOptimizerImpl implements PreventDistinctOptimizer {

    private final IntermediateQueryFactory iqFactory;
    private final OptimizationSingletons optimizationSingletons;

    @Inject
    private PreventDistinctOptimizerImpl(IntermediateQueryFactory iqFactory, OptimizationSingletons optimizationSingletons) {
        this.iqFactory = iqFactory;
        this.optimizationSingletons = optimizationSingletons;
    }

    @Override
    public IQ optimize(IQ query) {
        VariableGenerator variableGenerator = query.getVariableGenerator();
        IQTreeTransformer transformer = createTransformer(variableGenerator);
        IQTree newTree = transformer.transform(query.getTree());
        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected IQTreeTransformer createTransformer(VariableGenerator variableGenerator) {
        return new PreventDistinctTransformer(variableGenerator, optimizationSingletons);
    }

    private static class PreventDistinctTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final TermFactory termFactory;
        private final SubstitutionFactory substitutionFactory;
        private final VariableGenerator variableGenerator;

        public PreventDistinctTransformer(VariableGenerator variableGenerator, OptimizationSingletons optimizationSingletons) {
            super(optimizationSingletons.getCoreSingletons());
            this.variableGenerator = variableGenerator;
            this.termFactory = optimizationSingletons.getCoreSingletons().getTermFactory();
            this.substitutionFactory = optimizationSingletons.getCoreSingletons().getSubstitutionFactory();
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
            var decomposition = Decomposition.decomposeTree(child);
            if(decomposition.distinctNode.isPresent()) {
                return performTransformation(rootNode, tree, decomposition);
            }
            return super.transformConstruction(tree, rootNode, child);
        }

        private IQTree performTransformation(ConstructionNode constructionNode, IQTree originalTree, Decomposition decomposition) {
            var subtree = decomposition.descendantTree;


            //Find terms that need to be pushed below the distinct.
            ImmutableMap.Builder<Variable, ImmutableTerm> substitutionBuilder = ImmutableMap.builder();
            var updatedTerms = constructionNode.getSubstitution().stream()
                    .map(entry -> Maps.immutableEntry(entry.getKey(), splitByPreventDistinctType(entry.getValue(), substitutionBuilder)))
                    .collect(ImmutableCollectors.toSet());
            if(substitutionBuilder.build().isEmpty())
                return originalTree;
            var substitution = substitutionBuilder.build();
            if(!substitution.values().stream()
                    .allMatch(this::isDeterministic))
                throw new IllegalArgumentException("Unable to push non-supported terms below DISTINCT, because they include a non-deterministic function.");


            //Check if all variables used by these terms have a functional dependency pointing to them.
            var pushedVariables = substitution.entrySet().stream()
                    .flatMap(entry -> entry.getValue().getVariableStream())
                    .collect(ImmutableCollectors.toSet());
            var functionalDependencies = subtree.inferFunctionalDependencies();
            if(pushedVariables.stream()
                    .anyMatch(v -> functionalDependencies.getDeterminantsOf(v).stream()
                            .noneMatch(determinants -> Sets.intersection(determinants, pushedVariables).isEmpty())))
                throw new IllegalArgumentException(String.format("No Functional Dependency could be used to push the variables %s down.", pushedVariables));


            //Construct the updated IQTree.
            var topConstruction = iqFactory.createConstructionNode(
                    constructionNode.getVariables(),
                    updatedTerms.stream()
                            .filter(entry -> !(entry.getValue() instanceof Variable))
                            .collect(substitutionFactory.toSubstitution())
            );
            var bottomSubstitution = substitution.entrySet().stream()
                    .map(entry -> Maps.immutableEntry(
                            updatedTerms.stream().filter(e -> e.getValue().equals(entry.getKey())).findFirst().map(e -> e.getKey()).orElse(entry.getKey()),
                            entry.getValue()))
                    .collect(substitutionFactory.toSubstitution());
            var bottomConstruction = iqFactory.createConstructionNode(
                    Sets.difference(
                            Sets.union(constructionNode.getChildVariables(), bottomSubstitution.getDomain()),
                            pushedVariables
                    ).immutableCopy(),
                    bottomSubstitution
            );

            var newSubtree = decomposition.rebuildWithNewDescendantTree(iqFactory.createUnaryIQTree(bottomConstruction, subtree.acceptTransformer(this)), iqFactory);
            return iqFactory.createUnaryIQTree(topConstruction, newSubtree);
        }

        private boolean isDeterministic(ImmutableTerm term) {
            if(!(term instanceof ImmutableFunctionalTerm))
                return true;
            var f = (ImmutableFunctionalTerm) term;
            if(!f.getFunctionSymbol().isDeterministic())
                return false;
            return f.getTerms().stream()
                            .allMatch(this::isDeterministic);
        }

        private boolean shouldPreventDistinct(ImmutableTerm term) {
            var type = term.inferType();
            return type.flatMap(TermTypeInference::getTermType)
                    .filter(t -> t instanceof DBTermType)
                    .map(t -> (DBTermType) t)
                    .map(DBTermType::isPreventDistinctRecommended)
                    .orElse(false);
        }

        private ImmutableTerm splitByPreventDistinctType(ImmutableTerm term, ImmutableMap.Builder<Variable, ImmutableTerm> substitutionBuilder) {
            if(shouldPreventDistinct(term)) {
                throw new IllegalArgumentException(String.format("The term %s does not allow DISTINCT and could not be separated and pushed down", term));
            }

            if(!(term instanceof ImmutableFunctionalTerm)) {
                return term;
            }

            var f = (ImmutableFunctionalTerm) term;

            if(f.getTerms().stream().anyMatch(this::shouldPreventDistinct)
                    || IntStream.range(0, f.getArity())
                            .mapToObj(i -> f.getFunctionSymbol().getExpectedBaseType(i))
                            .filter(t -> t instanceof DBTermType)
                            .anyMatch(t -> ((DBTermType) t).isPreventDistinctRecommended())) {
                var variable = variableGenerator.generateNewVariable("pushPreventDistinct");
                substitutionBuilder.put(variable, term);
                return variable;
            }

            var childTerms = f.getTerms().stream()
                    .map(t -> splitByPreventDistinctType(t, substitutionBuilder))
                    .collect(ImmutableCollectors.toList());
            return termFactory.getImmutableFunctionalTerm(
                    f.getFunctionSymbol(),
                    childTerms
            );
        }

    }

    /**
     * Below the Construction, we may find the following nodes: DistinctNode, SliceNode.
     * Some nodes may be missing but the order must be respected. There are no multiple instances of the same kind of node.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static class Decomposition {
        final Optional<SliceNode> sliceNode;
        final Optional<DistinctNode> distinctNode;

        final IQTree descendantTree;

        private Decomposition(Optional<SliceNode> sliceNode,
                              Optional<DistinctNode> distinctNode,
                              IQTree descendantTree) {
            this.sliceNode = sliceNode;
            this.distinctNode = distinctNode;
            this.descendantTree = descendantTree;
        }

        IQTree rebuildWithNewDescendantTree(IQTree newDescendantTree, IntermediateQueryFactory iqFactory) {
            return Stream.of(distinctNode, sliceNode)
                    .flatMap(Optional::stream)
                    .reduce(newDescendantTree,
                            (t, n) -> iqFactory.createUnaryIQTree(n, t),
                            (t1, t2) -> { throw new MinorOntopInternalBugException("Must not be run in parallel"); });
        }

        static Decomposition decomposeTree(IQTree tree) {

            QueryNode rootNode = tree.getRootNode();
            Optional<SliceNode> sliceNode = Optional.of(rootNode)
                    .filter(n -> n instanceof SliceNode)
                    .map(n -> (SliceNode) n);

            IQTree firstNonSliceTree = sliceNode
                    .map(n -> ((UnaryIQTree) tree).getChild())
                    .orElse(tree);

            Optional<DistinctNode> distinctNode = Optional.of(firstNonSliceTree)
                    .map(IQTree::getRootNode)
                    .filter(n -> n instanceof DistinctNode)
                    .map(n -> (DistinctNode) n);

            IQTree descendantTree = distinctNode
                    .map(n -> ((UnaryIQTree) firstNonSliceTree).getChild())
                    .orElse(firstNonSliceTree);

            return new Decomposition(sliceNode, distinctNode, descendantTree);
        }
    }

}
