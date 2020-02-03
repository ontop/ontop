package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDistinctTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MappingDistinctTransformerImpl implements MappingDistinctTransformer {

    private final SpecificationFactory specificationFactory;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private MappingDistinctTransformerImpl(SpecificationFactory specificationFactory,
                                           IntermediateQueryFactory iqFactory){
        this.specificationFactory = specificationFactory;
        this.iqFactory = iqFactory;
    }

    public MappingInTransformation addDistinct(MappingInTransformation mapping){
        return specificationFactory.createMapping(Stream.concat(
                updateQueries(mapping.getRDFPropertyQueries(), false),
                updateQueries(mapping.getRDFClassQueries(), true))
                .collect(ImmutableCollectors.toMap()));
    }

    private Stream<ImmutableMap.Entry<MappingAssertionIndex, IQ>> updateQueries(ImmutableSet<Table.Cell<RDFAtomPredicate,IRI,IQ>> entry, boolean isClass) {
        return entry.stream()
                .map(e -> Maps.immutableEntry(
                        new MappingAssertionIndex(e.getRowKey(),
                        e.getColumnKey(), isClass),
                        updateQuery(e.getValue())));
    }

    /**
     * Puts a DISTINCT above the mapping definition and normalizes it.
     *
     * In the special case of CONSTRUCTION -> DISTINCT -> UNION, we try to push
     * the DISTINCT under the union.
     */
    private IQ updateQuery(IQ query) {
        VariableGenerator variableGenerator = query.getVariableGenerator();

        IQTree distinctTree = iqFactory.createUnaryIQTree(
                iqFactory.createDistinctNode(),
                query.getTree())
                .normalizeForOptimization(variableGenerator);

        Optional<ConstructionNode> topConstructionNode = Optional.of(distinctTree.getRootNode())
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n);

        Optional<IQTree> distinctUnionTree = topConstructionNode
                .map(n -> ((UnaryIQTree) distinctTree).getChild())
                .filter(t -> t.getRootNode() instanceof DistinctNode)
                .map(t -> ((UnaryIQTree) t).getChild())
                .filter(t -> t.getRootNode() instanceof UnionNode)
                .map(t -> ((UnionNode) t.getRootNode()).makeDistinct(t.getChildren()));

        IQTree newTree = distinctUnionTree
                .map(t -> iqFactory.createUnaryIQTree(topConstructionNode.get(), t))
                .map(t -> t.normalizeForOptimization(variableGenerator))
                .orElse(distinctTree);

        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }
}
