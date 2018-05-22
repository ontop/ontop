package it.unibz.inf.ontop.datalog;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.Variable;

public interface IQ2DatalogTranslator {

   DatalogProgram translate(IQ query);

   DatalogProgram translate(IQTree iqTree, ImmutableList<Variable> childSignature);
}
