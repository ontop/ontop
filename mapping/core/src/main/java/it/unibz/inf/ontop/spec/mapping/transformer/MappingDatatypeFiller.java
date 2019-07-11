package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;

public interface MappingDatatypeFiller {

    MappingWithProvenance inferMissingDatatypes(MappingWithProvenance mapping)
            throws DBMetadataExtractionException, UnknownDatatypeException;
}
