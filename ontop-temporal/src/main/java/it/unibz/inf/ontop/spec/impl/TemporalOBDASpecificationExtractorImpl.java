package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import it.unibz.inf.ontop.spec.TOBDASpecInput;
import it.unibz.inf.ontop.spec.datalogmtl.DatalogMTLProgramExtractor;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.TemporalMappingExtractor;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingTransformer;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingTransformer;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Optional;

public class TemporalOBDASpecificationExtractorImpl implements OBDASpecificationExtractor {
    private final MappingExtractor mappingExtractor;
    private final TemporalMappingExtractor temporalMappingExtractor;
    private final MappingTransformer mappingTransformer;
    private final TemporalMappingTransformer temporalMappingTransformer;
    private final OntopMappingSettings settings;
    private final DatalogMTLProgramExtractor ruleExtractor;

    @Inject
    private TemporalOBDASpecificationExtractorImpl(
            MappingExtractor mappingExtractor,
            MappingTransformer mappingTransformer,
            TemporalMappingExtractor temporalMappingExtractor,
            TemporalMappingTransformer temporalMappingTransformer,
            OntopMappingSettings settings, DatalogMTLProgramExtractor ruleExtractor) {
        this.mappingExtractor = mappingExtractor;
        this.temporalMappingExtractor = temporalMappingExtractor;
        this.mappingTransformer = mappingTransformer;
        this.temporalMappingTransformer = temporalMappingTransformer;
        this.settings = settings;
        this.ruleExtractor = ruleExtractor;
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> optionalOntology, ExecutorRegistry executorRegistry) throws OBDASpecificationException {


        MappingExtractor.MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(specInput, dbMetadata, optionalOntology, executorRegistry);

        TemporalMappingExtractor.MappingAndDBMetadata temporalMappingAndDBMetadata = temporalMappingExtractor.extract(specInput, dbMetadata, optionalOntology, executorRegistry, Optional.ofNullable(((RDBMetadata)mappingAndDBMetadata.getDBMetadata())));

        DatalogMTLProgram datalogMTLProgram = ruleExtractor.extract((TOBDASpecInput) specInput, mappingAndDBMetadata.getMapping());

        return temporalMappingTransformer.transform(mappingAndDBMetadata.getMapping(), mappingAndDBMetadata.getDBMetadata(), optionalOntology,
                temporalMappingAndDBMetadata.getTemporalMapping(),
                temporalMappingAndDBMetadata.getDBMetadata(),
                datalogMTLProgram);
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping ppMapping,
                                     @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> optionalOntology,
                                     ExecutorRegistry executorRegistry) throws OBDASpecificationException {

        MappingExtractor.MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(
                ppMapping, specInput, dbMetadata, optionalOntology, executorRegistry);

        TemporalMappingExtractor.MappingAndDBMetadata temporalMappingAndDBMetadata = temporalMappingExtractor.extract(ppMapping, specInput, dbMetadata,
                    optionalOntology, executorRegistry, Optional.ofNullable(((RDBMetadata)mappingAndDBMetadata.getDBMetadata())));

        DatalogMTLProgram datalogMTLProgram = ruleExtractor.extract((TOBDASpecInput) specInput, mappingAndDBMetadata.getMapping());

        return temporalMappingTransformer.transform(mappingAndDBMetadata.getMapping(), mappingAndDBMetadata.getDBMetadata(), optionalOntology,
                temporalMappingAndDBMetadata.getTemporalMapping(),
                temporalMappingAndDBMetadata.getDBMetadata(),
                datalogMTLProgram);
    }
}
