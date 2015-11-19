package org.semanticweb.ontop.utils;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.OBDAMappingAxiom;

import java.util.List;

/**
 * TODO: rename into Mapping2DatalogConverter
 */
public interface IMapping2DatalogConverter {

    ImmutableList<CQIE> constructDatalogProgram(List<OBDAMappingAxiom> mappingAxioms);
}
