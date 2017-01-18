package it.unibz.inf.ontop.mapping.extraction;


import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.DBMetadata;

/**
 * TODO: find a better name
 */
public interface MappingAndDBMetadata {

    Mapping getMapping();

    DBMetadata getDBMetadata();
}
