package it.unibz.inf.ontop.spec.trans;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.mapping.MappingWithProvenance;

public interface MappingDatatypeFiller {

    MappingWithProvenance inferMissingDatatypes(MappingWithProvenance mapping, DBMetadata dbMetadata)
            throws DBMetadataExtractionException;
}
