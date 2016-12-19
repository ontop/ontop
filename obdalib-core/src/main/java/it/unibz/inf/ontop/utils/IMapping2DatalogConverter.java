package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;

import java.util.Collection;

/**
 * TODO: rename into Mapping2DatalogConverter
 */
public interface IMapping2DatalogConverter {

    ImmutableList<CQIE> constructDatalogProgram(Collection<OBDAMappingAxiom> mappingAxioms, DBMetadata metadata);
}
