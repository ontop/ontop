package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;

public interface ReformulationFactory {

    QueryReformulator create(OBDASpecification obdaSpecification);
}