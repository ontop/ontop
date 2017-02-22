package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.Term;

import java.util.List;

public interface DatalogMTLFactory {

    public TemporalAtomicExpression getTemporalAtomicExpression(Predicate predicate, List<Term> terms);

    public BoxPlusExpression getSinceExpression(TemporalRange range, TemporalExpression left, TemporalExpression right);

    public BoxPlusExpression getBoxPlusExpression(TemporalRange range, TemporalExpression expression);

    // ...

    public DatalogMTLRule getRule(TemporalExpression head, TemporalExpression body);

    public DatalogMTLProgram getProgram(List<DatalogMTLRule> rules);
}
