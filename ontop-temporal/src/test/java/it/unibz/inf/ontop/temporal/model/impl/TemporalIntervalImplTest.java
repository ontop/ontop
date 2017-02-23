package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DatalogMTLFactory;
import it.unibz.inf.ontop.temporal.model.TemporalInterval;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TemporalIntervalImplTest {

    @Test
    public void testInterval() {
        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();

        final TemporalInterval interval = f.createTemporalInterval(true, false,
                Instant.now().minus(Duration.of(10, ChronoUnit.SECONDS)),
                Instant.now());

        System.out.println(interval);
    }
}
