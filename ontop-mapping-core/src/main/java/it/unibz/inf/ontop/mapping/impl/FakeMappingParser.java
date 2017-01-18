package it.unibz.inf.ontop.mapping.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.OBDAModel;

import java.io.IOException;

/**
 * Allows to create a OntopMappingConfiguration instance without having a proper MappingParser
 */
public class FakeMappingParser implements MappingParser {

    @Inject
    private FakeMappingParser() {
    }

    @Override
    public OBDAModel getOBDAModel() throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new UnsupportedOperationException("This mapping parser is fake and thus does not parse");
    }
}
