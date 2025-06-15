package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface UnionFlattener {

    IQTree transform(IQTree tree, VariableGenerator variableGenerator);
}
