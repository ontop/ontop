package it.unibz.inf.ontop.spec.datalogmtl.parser;

import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

public interface DatalogMTLNormalizer {

    DatalogMTLProgram normalize(DatalogMTLProgram program, Mapping staticMapping);
}
