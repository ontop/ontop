package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.AggregationNode;
import it.unibz.inf.ontop.iq.optimizer.AggregationSimplifier;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transformer.DefinitionPushDownTransformer;
import it.unibz.inf.ontop.iq.transformer.impl.RDFTypeDependentSimplifyingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class AggregationSimplifierImpl implements AggregationSimplifier {

    private final IntermediateQueryFactory iqFactory;
    private final OptimizationSingletons optimizationSingletons;

    @Inject
    private AggregationSimplifierImpl(IntermediateQueryFactory iqFactory, OptimizationSingletons optimizationSingletons) {
        this.iqFactory = iqFactory;
        this.optimizationSingletons = optimizationSingletons;
    }

    @Override
    public IQ optimize(IQ query) {
        VariableGenerator variableGenerator = query.getVariableGenerator();
        IQTreeTransformer transformer = createTransformer(variableGenerator);
        IQTree newTree = transformer.transform(query.getTree())
                .normalizeForOptimization(variableGenerator);
        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected IQTreeTransformer createTransformer(VariableGenerator variableGenerator) {
        return new AggregationSimplifyingTransformer(variableGenerator, optimizationSingletons);
    }

    protected static class AggregationSimplifyingTransformer extends RDFTypeDependentSimplifyingTransformer {

        private final VariableGenerator variableGenerator;
        private final SubstitutionFactory substitutionFactory;

        protected AggregationSimplifyingTransformer(VariableGenerator variableGenerator,
                                                    OptimizationSingletons optimizationSingletons) {
            super(optimizationSingletons);
            this.variableGenerator = variableGenerator;
            this.substitutionFactory = optimizationSingletons.getCoreSingletons().getSubstitutionFactory();
        }

        @Override
        public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
            // In case of aggregation nodes in the sub-tree
            IQTree normalizedChild = child.normalizeForOptimization(variableGenerator);

            ImmutableSubstitution<ImmutableFunctionalTerm> initialSubstitution = rootNode.getSubstitution();

            ImmutableMap<Variable, Optional<AggregationSimplification>> simplificationMap =
                    initialSubstitution.getImmutableMap().entrySet().stream()
                            .collect(ImmutableCollectors.toMap(
                                    Map.Entry::getKey,
                                    e -> simplifyAggregationFunctionalTerm(e.getValue(), normalizedChild)));

            ImmutableSubstitution<ImmutableFunctionalTerm> newAggregationSubstitution =
                    substitutionFactory.getSubstitution(simplificationMap.entrySet().stream()
                            .flatMap(e -> e.getValue()
                                    // Takes the entries in the SubTermSubstitutionMap
                                    .map(s -> s.decomposition.getSubTermSubstitutionMap()
                                            .map(sub -> sub.entrySet().stream())
                                            // If none, no entry
                                            .orElseGet(Stream::of))
                                    // Otherwise (if no simplification), keeps the former definition
                                    .orElseGet(() -> Stream.of(Maps.immutableEntry(e.getKey(), initialSubstitution.get(e.getKey())))))
                            .collect(ImmutableCollectors.toMap()));

            AggregationNode newNode = iqFactory.createAggregationNode(rootNode.getGroupingVariables(), newAggregationSubstitution);

            Stream<DefinitionPushDownTransformer.DefPushDownRequest> definitionsToPushDown = simplificationMap.values().stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(s -> s.pushDownRequests.stream());

            IQTree pushDownChildTree = pushDownDefinitions(normalizedChild, definitionsToPushDown);

            UnaryIQTree newAggregationTree = iqFactory.createUnaryIQTree(newNode, pushDownChildTree.acceptTransformer(this));

            // Substitution of the new parent construction node (containing typically the RDF function)
            ImmutableMap<Variable, ImmutableTerm> parentSubstitutionMap = simplificationMap.entrySet().stream()
                    .filter(e -> e.getValue().isPresent())
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> (ImmutableTerm) e.getValue().get().decomposition.getLiftableTerm()));

            return parentSubstitutionMap.isEmpty()
                    ? newAggregationTree
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(rootNode.getVariables(),
                                    substitutionFactory.getSubstitution(parentSubstitutionMap)),
                            newAggregationTree);
        }

        protected Optional<AggregationSimplification> simplifyAggregationFunctionalTerm(ImmutableFunctionalTerm aggregationFunctionalTerm,
                                                                              IQTree child) {
            throw new RuntimeException("TODO: implement");
        }




    }

    protected static class AggregationSimplification {
        public final ImmutableFunctionalTerm.FunctionalTermDecomposition decomposition;
        public final ImmutableSet<DefinitionPushDownTransformer.DefPushDownRequest> pushDownRequests;

        public AggregationSimplification(ImmutableFunctionalTerm.FunctionalTermDecomposition decomposition,
                                         ImmutableSet<DefinitionPushDownTransformer.DefPushDownRequest> pushDownRequests) {
            this.decomposition = decomposition;
            this.pushDownRequests = pushDownRequests;
        }
    }
}
