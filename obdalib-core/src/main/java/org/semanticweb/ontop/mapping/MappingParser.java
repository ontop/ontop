package org.semanticweb.ontop.mapping;

import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.io.InvalidDataSourceException;
import org.semanticweb.ontop.mapping.sql.ParsedSQLMapping;

import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public interface MappingParser {

    OBDAModel getOBDAModel() throws InvalidMappingException, IOException, InvalidDataSourceException, DuplicateMappingException;
}
