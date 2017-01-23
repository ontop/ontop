package it.unibz.inf.ontop.mapping.conversion;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.ontology.Ontology;

import java.util.Optional;

public interface SQLPPMapping2DSModelConverter {

    OBDASpecification convert(OBDAModel ppMapping, Optional<DBMetadata> dbMetadata, Optional<Ontology> ontology)
            throws OBDASpecificationException;
}
