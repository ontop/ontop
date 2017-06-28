package it.unibz.inf.ontop.mapping.transf;

import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;

public interface ABoxFactsMappingExpander {

    Mapping insertFacts(Mapping inputMapping, Ontology ontology, DBMetadata dbMetadata, ExecutorRegistry
            executorRegistry, boolean isOntologyAnnotationQueryingEnabled);
}
