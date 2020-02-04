package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.transformer.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;

import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.injection.OntopModelSettings.CardinalityPreservationMode.LOOSE;

public class DefaultMappingTransformer implements MappingTransformer {

    private final MappingVariableNameNormalizer mappingNormalizer;
    private final MappingSaturator mappingSaturator;
    private final ABoxFactIntoMappingConverter factConverter;
    private final ProvenanceMappingFactory mappingFactory;
    private final OntopMappingSettings settings;
    private final MappingSameAsInverseRewriter sameAsInverseRewriter;
    private final SpecificationFactory specificationFactory;
    private final RDF rdfFactory;
    private MappingDistinctTransformer mappingDistinctTransformer;

    @Inject
    private DefaultMappingTransformer(MappingVariableNameNormalizer mappingNormalizer,
                                      MappingSaturator mappingSaturator,
                                      ABoxFactIntoMappingConverter inserter,
                                      ProvenanceMappingFactory mappingFactory,
                                      OntopMappingSettings settings,
                                      MappingSameAsInverseRewriter sameAsInverseRewriter,
                                      SpecificationFactory specificationFactory,
                                      RDF rdfFactory,
                                      MappingDistinctTransformer mappingDistinctTransformer) {
        this.mappingNormalizer = mappingNormalizer;
        this.mappingSaturator = mappingSaturator;
        this.factConverter = inserter;
        this.mappingFactory = mappingFactory;
        this.settings = settings;
        this.sameAsInverseRewriter = sameAsInverseRewriter;
        this.specificationFactory = specificationFactory;
        this.rdfFactory = rdfFactory;
        this.mappingDistinctTransformer = mappingDistinctTransformer;
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
        MappingInTransformation sameAsOptimizedMapping = sameAsInverseRewriter.rewrite(mappingFactory.create(mapping).toRegularMapping());
        MappingInTransformation saturatedMapping = mappingSaturator.saturate(sameAsOptimizedMapping, dbMetadata, tbox);
        MappingInTransformation normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        // Don't insert the distinct if the cardinality preservation is set to LOOSE
        MappingInTransformation finalMapping = settings.getCardinalityPreservationMode() == LOOSE
                ? normalizedMapping
                : mappingDistinctTransformer.addDistinct(normalizedMapping);

        return specificationFactory.createSpecification(finalMapping.getMapping(), dbMetadata, tbox);
    }
}
