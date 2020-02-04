package it.unibz.inf.ontop.spec.mapping.transformer;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

public interface MappingSaturator {

    ImmutableMap<MappingAssertionIndex, IQ> saturate(ImmutableList<MappingAssertion> mapping, DBMetadata dbMetadata, ClassifiedTBox saturatedTBox);
}
