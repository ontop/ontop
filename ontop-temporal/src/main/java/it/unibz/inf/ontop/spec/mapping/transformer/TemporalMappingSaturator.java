package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

public interface TemporalMappingSaturator extends MappingSaturator{

    Mapping saturate(Mapping mapping, DBMetadata dbMetadata, Mapping temporalMapping, DBMetadata temporalDBMetadata, DatalogMTLProgram datalogMTLProgram);
}
