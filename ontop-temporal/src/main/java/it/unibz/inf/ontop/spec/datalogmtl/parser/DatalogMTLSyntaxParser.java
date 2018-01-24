package it.unibz.inf.ontop.spec.datalogmtl.parser;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

public interface DatalogMTLSyntaxParser {

    DatalogMTLProgram parse(String input);
}
