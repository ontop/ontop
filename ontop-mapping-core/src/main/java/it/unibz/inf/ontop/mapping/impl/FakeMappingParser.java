package it.unibz.inf.ontop.mapping.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.OBDAModel;
import org.eclipse.rdf4j.model.Model;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * Allows to create a OntopMappingConfiguration instance without having a proper MappingParser
 */
public class FakeMappingParser implements MappingParser {

    @Inject
    private FakeMappingParser() {
    }

    @Override
    public OBDAModel parse(File file) throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new UnsupportedOperationException("This mapping parser is fake and thus does not parse");
    }

    @Override
    public OBDAModel parse(Reader reader) throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new UnsupportedOperationException("This mapping parser is fake and thus does not parse");
    }

    @Override
    public OBDAModel parse(Model mappingGraph) throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new UnsupportedOperationException("This mapping parser is fake and thus does not parse");
    }
}
