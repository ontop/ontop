package it.unibz.inf.ontop.spec.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedTriplesMap;
import it.unibz.inf.ontop.spec.fact.FactExtractor;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor.MappingAndDBParameters;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingTransformer;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.spec.rule.RuleExtractor;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DefaultOBDASpecificationExtractor implements OBDASpecificationExtractor {

    private final MappingExtractor mappingExtractor;
    private final MappingTransformer mappingTransformer;
    private final FactExtractor factExtractor;
    private final RuleExtractor ruleExtractor;

    @Inject
    private DefaultOBDASpecificationExtractor(MappingExtractor mappingExtractor, MappingTransformer mappingTransformer,
                                              FactExtractor factExtractor, RuleExtractor ruleExtractor, OntopMappingSettings settings) {
        this.mappingExtractor = mappingExtractor;
        this.mappingTransformer = mappingTransformer;
        this.factExtractor = factExtractor;
        this.ruleExtractor = ruleExtractor;
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput,
                                     @Nonnull Optional<Ontology> optionalOntology, @Nonnull ImmutableSet<RDFFact> previousFacts)
            throws OBDASpecificationException {
        ImmutableSet<RDFFact> facts = Sets.union(factExtractor.extractAndSelect(optionalOntology), previousFacts).immutableCopy();

        try {
            MappingAndDBParameters mappingAndDBMetadata = mappingExtractor.extract(specInput, optionalOntology);
            ImmutableList<IQ> rules = ruleExtractor.extract(specInput);
            return mappingTransformer.transform(
                    mappingAndDBMetadata.getMapping(), mappingAndDBMetadata.getDBParameters(), optionalOntology, facts,
                    rules);
        }
        catch (MetadataExtractionException e) {
            throw new MappingIOException(e);
        }
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping<? extends PreProcessedTriplesMap> ppMapping,
                                     @Nonnull Optional<Ontology> optionalOntology, @Nonnull ImmutableSet<RDFFact> previousFacts) throws OBDASpecificationException {

        try {
            MappingAndDBParameters mappingAndDBMetadata = mappingExtractor.extract(ppMapping, specInput, optionalOntology);
            ImmutableList<IQ> rules = ruleExtractor.extract(specInput);

            ImmutableSet<RDFFact> facts = Sets.union(factExtractor.extractAndSelect(optionalOntology), previousFacts).immutableCopy();

            return mappingTransformer.transform(
                    mappingAndDBMetadata.getMapping(), mappingAndDBMetadata.getDBParameters(), optionalOntology, facts,
                    rules);
        }
        catch (MetadataExtractionException e) {
            throw new MappingIOException(e);
        }
    }
}
