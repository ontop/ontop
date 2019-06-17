package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.AggregationNode;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.normalization.AggregationNormalizer;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class AggregationNormalizerImpl implements AggregationNormalizer {

    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected AggregationNormalizerImpl(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
    }

    /**
     * Blocks distinct. May block some bindings and some filter conditions.
     *
     * TODO: enable lifting some filter conditions
     * TODO: we may consider remove distincts in the sub-tree when cardinality does not affect the substitution definitions
     */
    @Override
    public IQTree normalizeForOptimization(AggregationNode aggregationNode, IQTree child,
                                           VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree normalizedChild = child.normalizeForOptimization(variableGenerator);

        QueryNode rootNode = normalizedChild.getRootNode();

        // State after lifting the bindings
        Optional<AggregationNormalizationState> bindingLiftState = Optional.of(rootNode)
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .flatMap(n -> normalizeChildConstructionNode(aggregationNode, n,
                        ((UnaryIQTree) normalizedChild).getChild(), variableGenerator));


        IQProperties normalizedProperties = currentIQProperties.declareNormalizedForOptimization();
        // TODO: consider filters

        return bindingLiftState
                .map(s -> createNormalizedTree(s, normalizedProperties, variableGenerator))
                .orElseGet(() -> iqFactory.createUnaryIQTree(aggregationNode, normalizedChild));
    }

    private Optional<AggregationNormalizationState> normalizeChildConstructionNode(AggregationNode aggregationNode,
                                                                                   ConstructionNode childConstructionNode,
                                                                                   IQTree grandChild,
                                                                                   VariableGenerator variableGenerator) {

        throw new RuntimeException("TODO: implement");
    }

    private IQTree createNormalizedTree(AggregationNormalizationState state, IQProperties normalizedProperties,
                                        VariableGenerator variableGenerator) {
        throw new RuntimeException("TODO: continue");
    }


    interface AggregationNormalizationState {

    }

}
