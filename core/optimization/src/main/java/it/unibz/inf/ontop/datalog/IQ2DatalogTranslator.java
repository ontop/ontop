package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;

public interface IQ2DatalogTranslator {

   DatalogProgram translate(IQ query);

   DatalogProgram translate(IQTree iqTree);
}
