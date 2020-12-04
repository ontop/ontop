package it.unibz.inf.ontop.spec.dbschema.tools;

import it.unibz.inf.ontop.dbschema.ImmutableMetadata;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.io.File;
import java.io.IOException;

public interface RDBMetadataLoader {
    ImmutableMetadata loadAndDeserialize(File dbMetadataFile) throws MetadataExtractionException, IOException;
}
