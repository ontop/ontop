package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedTriplesMap;
import it.unibz.inf.ontop.spec.ontology.Ontology;

import javax.annotation.Nonnull;
import java.util.Optional;


public class FakeMappingExtractor implements MappingExtractor {

    private static final String MESSAGE = "Using a FakeMappingExtractor! Please use a proper implementation instead";

    @Override
    public MappingAndDBParameters extract(@Nonnull OBDASpecInput specInput,
                                          @Nonnull Optional<Ontology> ontology) {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public MappingAndDBParameters extract(@Nonnull PreProcessedMapping<? extends PreProcessedTriplesMap> ppMapping,
                                          @Nonnull OBDASpecInput specInput,
                                          @Nonnull Optional<Ontology> ontology) {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
