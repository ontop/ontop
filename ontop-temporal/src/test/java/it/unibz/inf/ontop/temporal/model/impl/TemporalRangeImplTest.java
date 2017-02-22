package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.TemporalRange;
import org.junit.Test;

import java.time.Duration;

public class TemporalRangeImplTest {

    @Test
    public void test(){

        TemporalRange r = new TemporalRangeImpl(false, true, Duration.parse("PT20.345S"), Duration.parse("PT1H1M"));

        System.out.printf(r.toString());

    }


}
