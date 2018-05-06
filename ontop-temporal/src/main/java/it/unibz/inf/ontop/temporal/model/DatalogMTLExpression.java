package it.unibz.inf.ontop.temporal.model;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

public interface DatalogMTLExpression {

    Iterable<? extends DatalogMTLExpression> getChildNodes();

    ImmutableList <VariableOrGroundTerm> getAllVariableOrGroundTerms();

}
