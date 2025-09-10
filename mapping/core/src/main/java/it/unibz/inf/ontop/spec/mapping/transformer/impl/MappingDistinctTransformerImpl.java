package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDistinctTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


public class MappingDistinctTransformerImpl implements MappingDistinctTransformer {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private MappingDistinctTransformerImpl(IntermediateQueryFactory iqFactory){
        this.iqFactory = iqFactory;
    }

    public ImmutableList<MappingAssertion> addDistinct(ImmutableList<MappingAssertion> mapping) {
        return mapping.stream()
                .map(this::updateQuery)
                .collect(ImmutableCollectors.toList());
    }

    /**
     * Puts a DISTINCT above the mapping definition and normalizes it.
     *
     * In the special case of CONSTRUCTION -> DISTINCT -> UNION, we try to push
     * the DISTINCT under the union.
     */
    private MappingAssertion updateQuery(MappingAssertion assertion) {
        VariableGenerator variableGenerator = assertion.getQuery().getVariableGenerator();

        IQTree distinctTree = iqFactory.createUnaryIQTree(
                iqFactory.createDistinctNode(),
                assertion.getQuery().getTree())
                .normalizeForOptimization(variableGenerator);

        var topConstruction = UnaryIQTreeDecomposition.of(distinctTree, ConstructionNode.class);
        var distinct = UnaryIQTreeDecomposition.of(topConstruction, DistinctNode.class);

        var union = NaryIQTreeTools.UnionDecomposition.of(distinct);
        IQTree newTree;
        if (union.isPresent()) {
            IQTree t = union.getNode().makeDistinct(union.getChildren());
            newTree = iqFactory.createUnaryIQTree(topConstruction.getNode(), t)
                                .normalizeForOptimization(variableGenerator);
        }
        else {
            newTree = distinctTree;
        }

        return assertion.copyOf(newTree, iqFactory);
    }
}
