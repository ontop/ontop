package it.unibz.inf.ontop.spec.mapping.parser;

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import org.apache.commons.rdf.api.Graph;

import java.io.Reader;
import java.io.File;

public interface MappingParser {

    PreProcessedMapping parse(File file) throws InvalidMappingException, MappingIOException;

    PreProcessedMapping parse(Reader reader) throws InvalidMappingException, MappingIOException;

    PreProcessedMapping parse(Graph reader) throws InvalidMappingException, MappingIOException;

}
