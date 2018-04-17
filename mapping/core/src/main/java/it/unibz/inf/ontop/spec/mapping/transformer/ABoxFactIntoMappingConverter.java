package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.OntologyABox;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;

public interface ABoxFactIntoMappingConverter {

    Mapping convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled, UriTemplateMatcher uriTemplateMatcher);
}
