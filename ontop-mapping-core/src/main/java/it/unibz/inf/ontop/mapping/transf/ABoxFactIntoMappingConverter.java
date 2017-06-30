package it.unibz.inf.ontop.mapping.transf;

import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.UriTemplateMatcher;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;

public interface ABoxFactIntoMappingConverter {

    Mapping convert(Ontology ontology, ExecutorRegistry executorRegistry, boolean isOntologyAnnotationQueryingEnabled);
}
