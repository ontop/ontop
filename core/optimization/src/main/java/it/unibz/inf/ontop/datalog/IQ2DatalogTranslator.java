package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.iq.IQ;

public interface IQ2DatalogTranslator {

   DatalogProgram translate(IQ query);
}
