package it.unibz.inf.ontop.temporal.model;


import it.unibz.inf.ontop.model.term.Constant;

import java.util.List;

public interface GroundTemporalAtomicExpression extends TemporalAtomicExpression{

    public List<Constant> getTerms();

}
