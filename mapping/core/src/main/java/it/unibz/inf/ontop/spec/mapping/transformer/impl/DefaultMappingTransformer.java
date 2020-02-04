package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.transformer.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.injection.OntopModelSettings.CardinalityPreservationMode.LOOSE;

public class DefaultMappingTransformer implements MappingTransformer {

    private final MappingVariableNameNormalizer mappingNormalizer;
    private final MappingSaturator mappingSaturator;
    private final ABoxFactIntoMappingConverter factConverter;
    private final OntopMappingSettings settings;
    private final MappingSameAsInverseRewriter sameAsInverseRewriter;
    private final SpecificationFactory specificationFactory;
    private final RDF rdfFactory;
    private final UnionBasedQueryMerger queryMerger;

    private MappingDistinctTransformer mappingDistinctTransformer;

    @Inject
    private DefaultMappingTransformer(MappingVariableNameNormalizer mappingNormalizer,
                                      MappingSaturator mappingSaturator,
                                      ABoxFactIntoMappingConverter inserter,
                                      OntopMappingSettings settings,
                                      MappingSameAsInverseRewriter sameAsInverseRewriter,
                                      SpecificationFactory specificationFactory,
                                      RDF rdfFactory,
                                      UnionBasedQueryMerger queryMerger,
                                      MappingDistinctTransformer mappingDistinctTransformer) {
        this.mappingNormalizer = mappingNormalizer;
        this.mappingSaturator = mappingSaturator;
        this.factConverter = inserter;
        this.settings = settings;
        this.sameAsInverseRewriter = sameAsInverseRewriter;
        this.specificationFactory = specificationFactory;
        this.rdfFactory = rdfFactory;
        this.mappingDistinctTransformer = mappingDistinctTransformer;
        this.queryMerger = queryMerger;
    }

    @Override
    public OBDASpecification transform(ImmutableList<MappingAssertion> mapping, DBMetadata dbMetadata, Optional<Ontology> ontology) {
        if (ontology.isPresent()) {
            ImmutableList<MappingAssertion> factsAsMapping = factConverter.convert(ontology.get().abox(),
                    settings.isOntologyAnnotationQueryingEnabled());

            ImmutableList<MappingAssertion> mappingWithFacts =
                    Stream.concat(mapping.stream(), factsAsMapping.stream()).collect(ImmutableCollectors.toList());

            return createSpecification(mappingWithFacts, dbMetadata, ontology.get().tbox());
        }
        else {
            ClassifiedTBox emptyTBox = OntologyBuilderImpl.builder(rdfFactory).build().tbox();
            return createSpecification(mapping, dbMetadata, emptyTBox);
        }
    }

    OBDASpecification createSpecification(ImmutableList<MappingAssertion> mapping, DBMetadata dbMetadata, ClassifiedTBox tbox) {

        ImmutableList<MappingAssertion> sameAsOptimizedMapping = sameAsInverseRewriter.rewrite(mapping);
        MappingInTransformation saturatedMapping = mappingSaturator.saturate(sameAsOptimizedMapping, dbMetadata, tbox);
        MappingInTransformation normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        // Don't insert the distinct if the cardinality preservation is set to LOOSE
        MappingInTransformation finalMapping = settings.getCardinalityPreservationMode() == LOOSE
                ? normalizedMapping
                : mappingDistinctTransformer.addDistinct(normalizedMapping);

        return specificationFactory.createSpecification(finalMapping.getMapping(), dbMetadata, tbox);
    }
}
