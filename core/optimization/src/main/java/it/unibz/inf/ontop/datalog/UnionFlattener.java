package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface UnionFlattener extends IQOptimizer {

    IQ optimize(IQ query);

    IQTree optimize(IQTree tree, VariableGenerator variableGenerator);
}
