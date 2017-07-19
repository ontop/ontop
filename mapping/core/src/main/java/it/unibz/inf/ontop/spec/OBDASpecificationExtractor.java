package it.unibz.inf.ontop.spec;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface OBDASpecificationExtractor {

    OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata,
                              @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException;

    OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping ppMapping,
                              @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology,
                              ExecutorRegistry executorRegistry)
            throws OBDASpecificationException;

}
