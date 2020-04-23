package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface NoNullValueEnforcer {

    IQ transform(IQ originalQuery);

    IQTree transform(IQTree tree, VariableGenerator variableGenerator);
}
