package it.unibz.inf.ontop.spec;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface TemporalOBDASpecificationExtractor {

    TemporalOBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata,
                              @Nonnull Optional<Ontology> ontology, @Nonnull Optional<DatalogMTLProgram> datalogMTLProgram,
                              ExecutorRegistry executorRegistry)
            throws OBDASpecificationException;

    TemporalOBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping ppMapping,
                              @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology,
                              @Nonnull Optional<DatalogMTLProgram> datalogMTLProgram,
                              ExecutorRegistry executorRegistry)
            throws OBDASpecificationException;

}
