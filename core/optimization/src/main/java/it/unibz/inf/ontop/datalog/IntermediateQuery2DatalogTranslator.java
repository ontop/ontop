package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.iq.IntermediateQuery;

public interface IntermediateQuery2DatalogTranslator {

   DatalogProgram translate(IntermediateQuery query);
}
