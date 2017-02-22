package it.unibz.inf.ontop.mapping;

import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;

import it.unibz.inf.ontop.model.OBDAModel;
import org.eclipse.rdf4j.model.Model;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

public interface SQLMappingParser {

    OBDAModel parse(File file) throws InvalidMappingException, IOException, DuplicateMappingException;

    OBDAModel parse(Reader reader) throws InvalidMappingException, IOException, DuplicateMappingException;

    OBDAModel parse(Model mappingGraph) throws InvalidMappingException, IOException, DuplicateMappingException;
}
