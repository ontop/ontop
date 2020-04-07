package it.unibz.inf.ontop.spec;


import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

/**
 * See SpecificationFactory for creating a new instance.
 */
public interface OBDASpecification {

    Mapping getSaturatedMapping();

    DBParameters getDBParameters();

    ClassifiedTBox getSaturatedTBox();
}
