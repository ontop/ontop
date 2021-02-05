package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.io.Reader;

public interface SerializedMetadataProvider extends MetadataProvider {
    interface Factory {
        SerializedMetadataProvider getMetadataProvider(Reader dbMetadataReader) throws MetadataExtractionException;
    }
}
