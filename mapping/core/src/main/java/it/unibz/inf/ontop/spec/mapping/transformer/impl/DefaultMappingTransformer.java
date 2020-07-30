package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.impl.MappingImpl;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.transformer.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

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

    private MappingDistinctTransformer mappingDistinctTransformer;

    @Inject
    private DefaultMappingTransformer(MappingVariableNameNormalizer mappingNormalizer,
                                      MappingSaturator mappingSaturator,
                                      ABoxFactIntoMappingConverter inserter,
                                      OntopMappingSettings settings,
                                      MappingSameAsInverseRewriter sameAsInverseRewriter,
                                      SpecificationFactory specificationFactory,
                                      RDF rdfFactory,
                                      MappingDistinctTransformer mappingDistinctTransformer) {
        this.mappingNormalizer = mappingNormalizer;
        this.mappingSaturator = mappingSaturator;
        this.factConverter = inserter;
        this.settings = settings;
        this.sameAsInverseRewriter = sameAsInverseRewriter;
        this.specificationFactory = specificationFactory;
        this.rdfFactory = rdfFactory;
        this.mappingDistinctTransformer = mappingDistinctTransformer;
    }

    @Override
    public OBDASpecification transform(ImmutableList<MappingAssertion> mapping, DBParameters dbParameters, Optional<Ontology> ontology) {
        if (ontology.isPresent()) {
            ImmutableList<MappingAssertion> factsAsMapping = factConverter.convert(ontology.get().abox(),
                    settings.isOntologyAnnotationQueryingEnabled());

            ImmutableList<MappingAssertion> mappingWithFacts =
                    Stream.concat(mapping.stream(), factsAsMapping.stream()).collect(ImmutableCollectors.toList());

            return createSpecification(mappingWithFacts, dbParameters, ontology.get().tbox());
        }
        else {
            ClassifiedTBox emptyTBox = OntologyBuilderImpl.builder(rdfFactory).build().tbox();
            return createSpecification(mapping, dbParameters, emptyTBox);
        }
    }

    private OBDASpecification createSpecification(ImmutableList<MappingAssertion> mapping, DBParameters dbParameters, ClassifiedTBox tbox) {

        ImmutableList<MappingAssertion> sameAsOptimizedMapping = sameAsInverseRewriter.rewrite(mapping);
        ImmutableList<MappingAssertion> saturatedMapping = mappingSaturator.saturate(sameAsOptimizedMapping, tbox);
        ImmutableList<MappingAssertion> normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        // Don't insert the distinct if the cardinality preservation is set to LOOSE
        ImmutableList<MappingAssertion> finalMapping = settings.getCardinalityPreservationMode() == LOOSE
                ? normalizedMapping
                : mappingDistinctTransformer.addDistinct(normalizedMapping);

        return specificationFactory.createSpecification(getMapping(finalMapping), dbParameters, tbox);
    }

    private Mapping getMapping(ImmutableList<MappingAssertion> assertions) {
        ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyDefinitions = assertions.stream()
                .filter(e -> !e.getIndex().isClass())
                .map(DefaultMappingTransformer::asCell)
                .collect(ImmutableCollectors.toTable());

        ImmutableTable<RDFAtomPredicate, IRI, IQ> classDefinitions = assertions.stream()
                .filter(e -> e.getIndex().isClass())
                .map(DefaultMappingTransformer::asCell)
                .collect(ImmutableCollectors.toTable());

        return new MappingImpl(propertyDefinitions, classDefinitions);
    }

    private static Table.Cell<RDFAtomPredicate, IRI, IQ> asCell(MappingAssertion assertion) {
        MappingAssertionIndex index = assertion.getIndex();
        return Tables.immutableCell(index.getPredicate(), index.getIri(), assertion.getQuery());
    }
}
