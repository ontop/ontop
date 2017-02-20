package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.Range;

import java.time.Duration;

import org.junit.Test;

public class RangeImplTest {

    @Test
    public void test(){

        Range r = new RangeImpl(false, true, Duration.parse("PT20.345S"), Duration.parse("PT1H1M"));

        System.out.printf(r.toString());

    }


}
