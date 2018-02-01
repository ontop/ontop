package it.unibz.inf.ontop.spec.datalogmtl;

import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.TOBDASpecInput;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

public interface DatalogMTLProgramExtractor {

    DatalogMTLProgram extract(TOBDASpecInput specInput, Mapping staticMapping);
}
