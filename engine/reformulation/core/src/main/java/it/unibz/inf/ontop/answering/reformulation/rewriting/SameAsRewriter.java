package it.unibz.inf.ontop.answering.reformulation.rewriting;

import it.unibz.inf.ontop.datalog.DatalogProgram;

public interface SameAsRewriter {
    DatalogProgram getSameAsRewriting(DatalogProgram pr);
}
