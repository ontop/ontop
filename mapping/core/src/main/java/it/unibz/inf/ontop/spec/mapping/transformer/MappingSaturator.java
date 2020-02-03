package it.unibz.inf.ontop.spec.mapping.transformer;


import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

public interface MappingSaturator {

    MappingInTransformation saturate(MappingInTransformation mapping, DBMetadata dbMetadata, ClassifiedTBox saturatedTBox);
}
