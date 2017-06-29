package it.unibz.inf.ontop.mapping.conversion;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.model.SQLPPMapping;
import it.unibz.inf.ontop.ontology.Ontology;

import java.io.File;
import java.util.Optional;

public interface SQLPPMapping2OBDASpecificationConverter {

    OBDASpecification convert(SQLPPMapping ppMapping, Optional<DBMetadata> dbMetadata, Optional<Ontology> ontology,
                              Optional<File> constraintFile, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException;
}
