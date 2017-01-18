package it.unibz.inf.ontop.mapping.extraction;


import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.model.DBMetadata;

import java.io.IOException;

public interface MappingAndDBMetadataExtractor {

    MappingAndDBMetadata extract()
            throws InvalidMappingException, IOException, DuplicateMappingException;

    MappingAndDBMetadata extract(DBMetadata dbMetadata)
            throws InvalidMappingException, IOException, DuplicateMappingException;

}
