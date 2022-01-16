package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.io.Reader;

public interface SerializedMetadataProvider extends MetadataProvider {
    interface Factory {
        SerializedMetadataProvider getMetadataProvider(Reader dbMetadataReader) throws MetadataExtractionException;

        /**
         * The parent provider may only be used for creating black-box views
         */
        SerializedMetadataProvider getMetadataProvider(Reader dbMetadataReader, MetadataLookupSupplier parentProviderSupplier)
                throws MetadataExtractionException;
    }

    @FunctionalInterface
    interface MetadataLookupSupplier {
        MetadataLookup get() throws MetadataExtractionException;
    }
}
