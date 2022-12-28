package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.io.Reader;

public interface LensMetadataProvider extends MetadataProvider {

    interface Factory {
        LensMetadataProvider getMetadataProvider(
                MetadataProvider parentMetadataProvider,
                Reader lensReader) throws MetadataExtractionException;
    }
}
