package it.unibz.inf.ontop.temporal.queryanswering.impl;

import it.unibz.inf.ontop.model.DatalogProgram;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.impl.DatalogProgramImpl;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.temporal.model.impl.*;
import org.junit.Test;
import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

import java.time.Duration;
import java.util.ArrayList;

public class TemporalQueryReformulatorTest {

//    public Rule generateRule(Predicate head, ArrayList<Predicate>body){
//
//        return (Rule) new NormalizedRuleImpl(head, body);
//    }

    @Test
    public void hurricaneAffectedStateTest(){

        Predicate innerPredicate = DATA_FACTORY.getPredicate("HurricaneForceWind", new Predicate.COL_TYPE[7]);
        Predicate predicate= new TemporalPredicateImpl(new UnaryTemporalModifierImpl(UnaryTemporalOperator.BoxMinus,
                new RangeImpl(false, true, Duration.parse("PT0H"), Duration.parse("PT1H"))),
                innerPredicate);

        Predicate head = DATA_FACTORY.getPredicate("P", new Predicate.COL_TYPE[7]);

        ArrayList<Predicate>body = new ArrayList<Predicate>();
        body.add(predicate);

        NormalizedRule rule = new NormalizedRuleImpl(head, body);

        DatalogMTLProgram Pi = new DatalogMTLProgramImpl();
        Pi.appendRule(rule);

        Predicate queryPredicate = DATA_FACTORY.getPredicate("P", new Predicate.COL_TYPE[7]);

        TemporalQuery temporalQuery = new TemporalQueryImpl(queryPredicate);



    }
}
