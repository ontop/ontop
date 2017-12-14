package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.exception.OBDASpecificationException;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface OntopReformulationConfiguration extends OntopOBDAConfiguration, OntopOptimizationConfiguration {

    @Override
    OntopReformulationSettings getSettings();

    Optional<IRIDictionary> getIRIDictionary();

    /**
     * To call ONLY when interested by query REFORMULATION, not FULL query ANSWERING
     * (no query evaluation).
     *
     */
    QueryReformulator loadQueryReformulator() throws OBDASpecificationException;

    InputQueryFactory getInputQueryFactory();


    interface OntopReformulationBuilderFragment<B extends Builder<B>> {
        /**
         * In the case of SQL, inserts REPLACE functions in the generated query
         */
        B enableIRISafeEncoding(boolean enable);

        B enableExistentialReasoning(boolean enable);

        /**
         * Not for end-users!
         */
        B iriDictionary(@Nonnull IRIDictionary iriDictionary);
    }

    interface Builder<B extends Builder<B>> extends OntopReformulationBuilderFragment<B>, OntopOBDAConfiguration.Builder<B>,
            OntopOptimizationConfiguration.Builder<B> {

        @Override
        OntopReformulationConfiguration build();
    }

}
