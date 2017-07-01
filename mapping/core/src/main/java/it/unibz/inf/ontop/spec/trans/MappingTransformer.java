package it.unibz.inf.ontop.spec.trans;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.spec.OBDASpecification;

/**
 * TODO: find a better name
 */
public interface MappingTransformer {

    OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata, Ontology ontology, TBoxReasoner tBox, ExecutorRegistry executorRegistry)
            throws MappingException, OntologyException;
}
