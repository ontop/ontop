package it.unibz.inf.ontop.owlrefplatform.core.translator;

import it.unibz.inf.ontop.datalog.DatalogProgram;

public interface SameAsRewriter {
    DatalogProgram getSameAsRewriting(DatalogProgram pr);
}
