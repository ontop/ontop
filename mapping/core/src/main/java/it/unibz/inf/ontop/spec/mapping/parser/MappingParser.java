package it.unibz.inf.ontop.spec.mapping.parser;

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedTriplesMap;
import org.apache.commons.rdf.api.Graph;

import java.io.Reader;
import java.io.File;

public interface MappingParser<T extends PreProcessedTriplesMap> {

    PreProcessedMapping<T> parse(File file) throws InvalidMappingException, MappingIOException;

    PreProcessedMapping<T> parse(Reader reader) throws InvalidMappingException, MappingIOException;

    PreProcessedMapping<T> parse(Graph reader) throws InvalidMappingException, MappingIOException;

}
