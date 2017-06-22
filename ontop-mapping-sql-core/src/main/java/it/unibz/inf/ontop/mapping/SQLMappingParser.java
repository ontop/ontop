package it.unibz.inf.ontop.mapping;

import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;

import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.model.SQLPPMapping;
import org.eclipse.rdf4j.model.Model;

import java.io.File;
import java.io.Reader;

public interface SQLMappingParser {

    SQLPPMapping parse(File file) throws InvalidMappingException, DuplicateMappingException, MappingIOException;

    SQLPPMapping parse(Reader reader) throws InvalidMappingException, DuplicateMappingException, MappingIOException;

    SQLPPMapping parse(Model mappingGraph) throws InvalidMappingException, DuplicateMappingException;
}
