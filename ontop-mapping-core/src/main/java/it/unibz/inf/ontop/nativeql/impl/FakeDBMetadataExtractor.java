package it.unibz.inf.ontop.nativeql.impl;

import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.nativeql.DBConnectionWrapper;
import it.unibz.inf.ontop.nativeql.DBMetadataException;
import it.unibz.inf.ontop.nativeql.DBMetadataExtractor;

import javax.annotation.Nullable;

public class FakeDBMetadataExtractor implements DBMetadataExtractor {
    @Override
    public DBMetadata extract(OBDAModel model, @Nullable DBConnectionWrapper dbConnection) throws DBMetadataException {
        throw new UnsupportedOperationException("This DBMetadataExtractor is fake");
    }

    @Override
    public DBMetadata extract(OBDAModel model, @Nullable DBConnectionWrapper dbConnection, DBMetadata partiallyDefinedMetadata) throws DBMetadataException {
        throw new UnsupportedOperationException("This DBMetadataExtractor is fake");
    }
}
