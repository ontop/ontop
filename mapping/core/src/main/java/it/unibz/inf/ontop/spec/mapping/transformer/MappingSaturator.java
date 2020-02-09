package it.unibz.inf.ontop.spec.mapping.transformer;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

public interface MappingSaturator {

    ImmutableList<MappingAssertion> saturate(ImmutableList<MappingAssertion> mapping, ClassifiedTBox saturatedTBox);
}
