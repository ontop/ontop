package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDistinctTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

public class MappingDistinctTransformerImpl implements MappingDistinctTransformer {

    private final SpecificationFactory specificationFactory;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private MappingDistinctTransformerImpl(SpecificationFactory specificationFactory,
                                           IntermediateQueryFactory iqFactory){
        this.specificationFactory = specificationFactory;
        this.iqFactory = iqFactory;
    }

    public Mapping addDistinct(Mapping mapping){
        return specificationFactory.createMapping(
                mapping.getMetadata(),
                updateQueries(mapping.getRDFPropertyQueries()),
                updateQueries(mapping.getRDFClassQueries())
        );
    }

    private ImmutableTable<RDFAtomPredicate,IRI,IQ> updateQueries(ImmutableSet<Table.Cell<RDFAtomPredicate,IRI,IQ>> entry) {
        return entry.stream()
                .map(e -> Tables.immutableCell(
                        e.getRowKey(),
                        e.getColumnKey(),
                        updateQuery(e.getValue())))
                .collect(ImmutableCollectors.toTable());
    }

    private IQ updateQuery(IQ query) {
        return iqFactory.createIQ(
                query.getProjectionAtom(),
                iqFactory.createUnaryIQTree(
                        iqFactory.createDistinctNode(),
                        query.getTree()
        ));
    }
}
