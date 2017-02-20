package it.unibz.inf.ontop.temporal.queryanswering.impl;

import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.impl.PredicateImpl;
import it.unibz.inf.ontop.temporal.model.Rule;
import it.unibz.inf.ontop.temporal.model.UnaryTemporalOperator;
import it.unibz.inf.ontop.temporal.model.impl.RangeImpl;
import it.unibz.inf.ontop.temporal.model.impl.it.unibz.inf.ontop.temporal.model.impl.NormalizedRuleImpl;
import it.unibz.inf.ontop.temporal.model.impl.it.unibz.inf.ontop.temporal.model.impl.TemporalPredicateImpl;
import it.unibz.inf.ontop.temporal.model.impl.it.unibz.inf.ontop.temporal.model.impl.UnaryTemporalModifierImpl;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;

public class TemporalQueryReformulatorTest {

    public Rule generateRule(Predicate head, ArrayList<Predicate>body){

        return (Rule) new NormalizedRuleImpl(head, body);
    }

    @Test
    public void hurricaneAffectedStateTest(){
//        Predicate innerPredicate = new PredicateImpl("HurricaneForceWind", 1,  new Predicate.COL_TYPE[7]);
//        Predicate predicate= new TemporalPredicateImpl(new UnaryTemporalModifierImpl(UnaryTemporalOperator.BoxMinus,
//                new RangeImpl(false, true, Duration.parse("PT0H"), Duration.parse("PT1H"))),
//                innerPredicate);


    }
}
