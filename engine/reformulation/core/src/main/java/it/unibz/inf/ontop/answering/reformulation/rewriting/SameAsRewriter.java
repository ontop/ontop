package it.unibz.inf.ontop.answering.reformulation.rewriting;

import it.unibz.inf.ontop.datalog.DatalogProgram;

/**
 * See TranslationFactory for creating a new instance.
 */
public interface SameAsRewriter {
    DatalogProgram getSameAsRewriting(DatalogProgram pr);
}
