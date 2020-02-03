package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.spec.ontology.OntologyABox;

public interface ABoxFactIntoMappingConverter {

    MappingInTransformation convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled);
}
