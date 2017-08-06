package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.ontology.Ontology;

import javax.annotation.Nonnull;
import java.util.Optional;

public class TemporalOBDASpecificationExtractorImpl implements OBDASpecificationExtractor {
    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry) throws OBDASpecificationException {
        return null;
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping ppMapping, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry) throws OBDASpecificationException {
        return null;
    }
}
