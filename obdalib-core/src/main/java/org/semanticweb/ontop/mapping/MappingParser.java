package org.semanticweb.ontop.mapping;

import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.mapping.sql.ParsedSQLMapping;

import org.semanticweb.ontop.model.OBDAMappingAxiom;
import java.util.List;

public interface MappingParser {

    List<OBDAMappingAxiom> parse() throws InvalidMappingException;
}
