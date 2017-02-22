package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.model.Constant;
import it.unibz.inf.ontop.model.Term;

import java.util.List;

public interface GroundTemporalAtomicExpression extends TemporalAtomicExpression{

    public List<Constant> getTerms();

}
