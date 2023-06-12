package it.unibz.inf.ontop.spec;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedTriplesMap;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface OBDASpecificationExtractor {

    OBDASpecification extract(@Nonnull OBDASpecInput specInput,
                              @Nonnull Optional<Ontology> ontology,
                              @Nonnull ImmutableSet<RDFFact> facts)
            throws OBDASpecificationException;

    OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping<? extends PreProcessedTriplesMap> ppMapping,
                              @Nonnull Optional<Ontology> ontology, @Nonnull ImmutableSet<RDFFact> facts)
            throws OBDASpecificationException;

}
