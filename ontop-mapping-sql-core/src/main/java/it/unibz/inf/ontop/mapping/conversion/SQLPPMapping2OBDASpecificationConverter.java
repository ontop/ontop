package it.unibz.inf.ontop.mapping.conversion;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.Ontology;

import java.io.File;
import java.util.Optional;

public interface SQLPPMapping2OBDASpecificationConverter {

    OBDASpecification convert(OBDAModel ppMapping, Optional<DBMetadata> dbMetadata, Optional<Ontology> ontology,
                              Optional<File> constraintFile, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException;
}
