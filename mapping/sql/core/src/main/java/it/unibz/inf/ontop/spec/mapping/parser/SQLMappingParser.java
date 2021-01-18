package it.unibz.inf.ontop.spec.mapping.parser;

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.apache.commons.rdf.api.Graph;

import java.io.File;
import java.io.Reader;

public interface SQLMappingParser extends MappingParser<SQLPPTriplesMap> {

    SQLPPMapping parse(File file) throws InvalidMappingException, MappingIOException;

    /**
     * Must close the reader
     */
    SQLPPMapping parse(Reader reader) throws InvalidMappingException, MappingIOException;

    SQLPPMapping parse(Graph mappingGraph) throws InvalidMappingException;
}
