package it.unibz.inf.ontop.spec;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedTriplesMap;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface OBDASpecificationExtractor {

    OBDASpecification extract(@Nonnull OBDASpecInput specInput,
                              @Nonnull Optional<Ontology> ontology)
            throws OBDASpecificationException;

    OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping<? extends PreProcessedTriplesMap> ppMapping,
                              @Nonnull Optional<Ontology> ontology)
            throws OBDASpecificationException;

}
