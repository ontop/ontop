package it.unibz.inf.ontop.temporal.model;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.Constant;

import java.util.List;

public interface GroundTemporalAtomicExpression extends TemporalAtomicExpression{

    public ImmutableList<Constant> getTerms();

}
