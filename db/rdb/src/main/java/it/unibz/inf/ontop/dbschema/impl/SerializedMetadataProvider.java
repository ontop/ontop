package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.DBMetadataProvider;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.io.Reader;

public interface SerializedMetadataProvider extends DBMetadataProvider {


    /*DBMetadataProvider loadanddeserialize(Reader dbMetadataReader);*/

    interface Factory {
    SerializedMetadataProvider getMetadataProvider(Reader dbMetadataReader,
                                                   QuotedIDFactory quotedIDFactory,
                                                   MetadataProvider provider) throws MetadataExtractionException;

}

}
