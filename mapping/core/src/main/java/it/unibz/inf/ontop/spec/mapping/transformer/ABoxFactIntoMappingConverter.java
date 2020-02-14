package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.ontology.OntologyABox;

public interface ABoxFactIntoMappingConverter {

    ImmutableList<MappingAssertion> convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled);
}
