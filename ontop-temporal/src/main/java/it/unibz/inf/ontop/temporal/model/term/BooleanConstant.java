package it.unibz.inf.ontop.temporal.model.term;

import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

public interface BooleanConstant extends VariableOrGroundTerm {
    public boolean getBooleanValue();
}
