package it.unibz.inf.ontop.spec.mapping.transformer;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

public interface MappingSaturator {

    MappingInTransformation saturate(ImmutableList<MappingAssertion> mapping, DBMetadata dbMetadata, ClassifiedTBox saturatedTBox);
}
