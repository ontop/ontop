package it.unibz.inf.ontop.view;

import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.io.Reader;

public interface OntopViewMetadataProvider extends MetadataProvider {

    interface Factory {
        OntopViewMetadataProvider getMetadataProvider(
                MetadataProvider parentMetadataProvider,
                Reader ontopViewReader) throws MetadataExtractionException;
    }
}
