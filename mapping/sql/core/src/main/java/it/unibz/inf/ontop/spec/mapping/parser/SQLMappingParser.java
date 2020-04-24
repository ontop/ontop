package it.unibz.inf.ontop.spec.mapping.parser;

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.apache.commons.rdf.api.Graph;

import java.io.File;
import java.io.Reader;

public interface SQLMappingParser extends MappingParser {

    SQLPPMapping parse(File file) throws InvalidMappingException, MappingIOException;

    SQLPPMapping parse(Reader reader) throws InvalidMappingException, MappingIOException;

    SQLPPMapping parse(Graph mappingGraph) throws InvalidMappingException;
}
