package it.unibz.inf.ontop.spec.trans;

import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;

public interface ABoxFactIntoMappingConverter {

    Mapping convert(Ontology ontology, ExecutorRegistry executorRegistry, boolean isOntologyAnnotationQueryingEnabled);
}
