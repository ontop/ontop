package it.unibz.inf.ontop.spec.dbschema.tools;

import it.unibz.inf.ontop.exception.DBMetadataExtractionException;

public interface DBMetadataExtractorAndSerializer {

    String extractAndSerialize() throws DBMetadataExtractionException;
}
