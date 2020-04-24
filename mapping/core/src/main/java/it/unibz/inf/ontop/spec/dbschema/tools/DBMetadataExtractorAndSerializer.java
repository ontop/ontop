package it.unibz.inf.ontop.spec.dbschema.tools;

import it.unibz.inf.ontop.exception.MetadataExtractionException;

public interface DBMetadataExtractorAndSerializer {

    String extractAndSerialize() throws MetadataExtractionException;
}
