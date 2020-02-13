package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.MappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import org.apache.commons.rdf.api.Graph;

import javax.annotation.Nonnull;
import java.io.Reader;
import java.io.File;
import java.util.Optional;

public abstract class AbstractMappingExtractor<T1 extends PreProcessedMapping, T2 extends DBMetadata,
        T3 extends MappingParser, T4 extends OntopMappingSettings> implements MappingExtractor{


    private final MappingOntologyComplianceValidator ontologyComplianceValidator;
    protected final T3 mappingParser;

    protected AbstractMappingExtractor(MappingOntologyComplianceValidator ontologyComplianceValidator, T3 mappingParser) {
        this.ontologyComplianceValidator = ontologyComplianceValidator;
        this.mappingParser = mappingParser;
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull OBDASpecInput specInput,
                                        @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<Ontology> ontology,
                                        @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException {

        T1 ppMapping = extractPPMapping(specInput);

        return extract(ppMapping, specInput, dbMetadata, ontology, executorRegistry);
    }

    protected T1 extractPPMapping(OBDASpecInput specInput)
            throws DuplicateMappingException, MappingIOException, InvalidMappingException {

        Optional<File> optionalMappingFile = specInput.getMappingFile();
        if (optionalMappingFile.isPresent())
            return (T1) mappingParser.parse(optionalMappingFile.get());

        Optional<Reader> optionalMappingReader = specInput.getMappingReader();
        if (optionalMappingReader.isPresent())
            return (T1) mappingParser.parse(optionalMappingReader.get());

        Optional<Graph> optionalMappingGraph = specInput.getMappingGraph();
        if (optionalMappingGraph.isPresent())
            return (T1) mappingParser.parse(optionalMappingGraph.get());

        throw new IllegalArgumentException("Bad internal configuration: no mapping input provided in the OBDASpecInput!\n" +
                " Should have been detected earlier (in case of an user mistake)");
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull PreProcessedMapping ppMapping, @Nonnull OBDASpecInput specInput,
                                        @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<Ontology> ontology,
                                        @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException {

        return convertPPMapping(castPPMapping(ppMapping), castDBMetadata(dbMetadata), specInput, ontology,
                executorRegistry);
    }

    /**
     * Validation:
     * - Mismatch between the ontology and the mapping
     */
    protected void validateMapping(Optional<Ontology> ontology,
                                 ImmutableList<MappingAssertion> filledProvMapping) throws MappingOntologyMismatchException {
        if (ontology.isPresent()) {
            ontologyComplianceValidator.validate(filledProvMapping, ontology.get());
        }
    }

    protected abstract MappingAndDBMetadata convertPPMapping(T1 ppMapping, Optional<T2> dbMetadata, OBDASpecInput specInput, Optional<Ontology> ontology, ExecutorRegistry executorRegistry) throws MetaMappingExpansionException, DBMetadataExtractionException, MappingOntologyMismatchException, InvalidMappingSourceQueriesException, NullVariableInMappingException, UnknownDatatypeException;

    protected abstract Optional<T2> castDBMetadata(Optional<DBMetadata> dbMetadata);

    protected abstract T1 castPPMapping(PreProcessedMapping ppMapping);

    protected abstract T1 expandPPMapping(T1 ppMapping, T4 settings, T2 dbMetadata) throws MetaMappingExpansionException;
}
