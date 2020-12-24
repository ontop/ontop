package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.DBMetadataProvider;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.io.Reader;

public interface SerializedMetadataProvider extends DBMetadataProvider {
    interface Factory {
        SerializedMetadataProvider getMetadataProvider(Reader dbMetadataReader) throws MetadataExtractionException;
    }
}
