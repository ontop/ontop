package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import it.unibz.inf.ontop.dbschema.ImmutableMetadata;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.io.IOException;

public interface RDBMetadataLoader {
    ImmutableMetadata loadAndDeserialize() throws MetadataExtractionException, IOException;
}
