package it.unibz.inf.ontop.mapping.extraction.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.mapping.extraction.MappingAndDBMetadata;
import it.unibz.inf.ontop.mapping.extraction.MappingAndDBMetadataExtractor;
import it.unibz.inf.ontop.model.DBMetadata;

import java.io.IOException;

public class FakeMappingAndDBMetadataExtractor implements MappingAndDBMetadataExtractor {

    @Inject
    private FakeMappingAndDBMetadataExtractor(){
    }

    @Override
    public MappingAndDBMetadata extract() throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new UnsupportedOperationException("This FakeMappingAndDBMetadataExtractor is fake and thus does not extract");
    }

    @Override
    public MappingAndDBMetadata extract(DBMetadata dbMetadata) throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new UnsupportedOperationException("This FakeMappingAndDBMetadataExtractor is fake and thus does not extract");
    }
}
