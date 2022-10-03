package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.query.KGQueryFactory;
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

    KGQueryFactory getKGQueryFactory();


    interface OntopReformulationBuilderFragment<B extends Builder<B>> {

        B enableExistentialReasoning(boolean enable);
    }

    interface Builder<B extends Builder<B>> extends OntopReformulationBuilderFragment<B>, OntopOBDAConfiguration.Builder<B>,
            OntopOptimizationConfiguration.Builder<B> {

        @Override
        OntopReformulationConfiguration build();
    }

}
