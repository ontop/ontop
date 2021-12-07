package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

public interface FactIntoMappingConverter {

    ImmutableList<MappingAssertion> convert(ImmutableSet<RDFFact> assertions);
}
