package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.DataSourceMetadata;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;

import java.util.Collection;
import java.util.List;

/**
 * TODO: rename into Mapping2DatalogConverter
 */
public interface IMapping2DatalogConverter {

    ImmutableList<CQIE> constructDatalogProgram(Collection<OBDAMappingAxiom> mappingAxioms, DataSourceMetadata metadata);
}
