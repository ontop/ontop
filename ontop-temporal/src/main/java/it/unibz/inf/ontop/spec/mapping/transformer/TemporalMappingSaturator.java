package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

public interface TemporalMappingSaturator extends MappingSaturator{

    Mapping saturate(Mapping mapping, DBMetadata dbMetadata, TemporalMapping temporalMapping, DBMetadata temporalDBMetadata, DatalogMTLProgram datalogMTLProgram);
}
