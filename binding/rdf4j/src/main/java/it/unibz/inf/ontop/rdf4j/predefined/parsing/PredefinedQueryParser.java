package it.unibz.inf.ontop.rdf4j.predefined.parsing;

import it.unibz.inf.ontop.rdf4j.predefined.PredefinedQueries;

import java.io.Reader;

public interface PredefinedQueryParser {

    PredefinedQueries parse(Reader configReader, Reader queryReader) throws PredefinedQueryConfigException;
    PredefinedQueries parse(Reader configReader, Reader queryReader, Reader contextReader) throws PredefinedQueryConfigException;
}
