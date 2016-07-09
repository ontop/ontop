package it.unibz.inf.ontop.mapping;

import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.io.InvalidDataSourceException;

import it.unibz.inf.ontop.model.OBDAModel;

import java.io.IOException;

/**
 *
 */
public interface MappingParser {

    OBDAModel getOBDAModel() throws InvalidMappingException, IOException, InvalidDataSourceException, DuplicateMappingException;
}
