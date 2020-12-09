package it.unibz.inf.ontop.spec.dbschema.tools;

import it.unibz.inf.ontop.dbschema.ImmutableMetadata;
import it.unibz.inf.ontop.dbschema.impl.ImmutableMetadataImpl;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface RDBMetadataLoader {
    List<ImmutableMetadataImpl> loadAndDeserialize(File dbMetadataFile) throws MetadataExtractionException, IOException;
    // return ImmutableMetadata
}
