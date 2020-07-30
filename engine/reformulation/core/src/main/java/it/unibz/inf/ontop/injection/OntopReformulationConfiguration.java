package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.exception.OBDASpecificationException;


public interface OntopReformulationConfiguration extends OntopOBDAConfiguration, OntopOptimizationConfiguration {

    @Override
    OntopReformulationSettings getSettings();

    /**
     * To call ONLY when interested by query REFORMULATION, not FULL query ANSWERING
     * (no query evaluation).
     *
     */
    QueryReformulator loadQueryReformulator() throws OBDASpecificationException;

    InputQueryFactory getInputQueryFactory();


    interface OntopReformulationBuilderFragment<B extends Builder<B>> {

        B enableExistentialReasoning(boolean enable);
    }

    interface Builder<B extends Builder<B>> extends OntopReformulationBuilderFragment<B>, OntopOBDAConfiguration.Builder<B>,
            OntopOptimizationConfiguration.Builder<B> {

        @Override
        OntopReformulationConfiguration build();
    }

}
