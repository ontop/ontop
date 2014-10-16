package org.semanticweb.ontop.mapping;

import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.mapping.sql.ParsedSQLMapping;

import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;

import java.util.List;

/**
 *
 */
public interface MappingParser {

    OBDAModel getOBDAModel() throws InvalidMappingException;
}
