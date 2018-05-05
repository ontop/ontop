package it.unibz.inf.ontop.spec.datalogmtl.parser;

import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

public interface DatalogMTLSyntaxParser {

    DatalogMTLProgram parse(String input);

    DatalogMTLProgram parse(java.io.File datalogFile);

    void save(DatalogMTLProgram datalogMTLProgram, java.io.File datalogFile);
}
