package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface OntopQueryAnsweringConfiguration extends OntopOBDAConfiguration, OntopOptimizationConfiguration {

    @Override
    OntopQueryAnsweringSettings getSettings();

    Optional<IRIDictionary> getIRIDictionary();


    interface OntopQueryAnsweringBuilderFragment<B extends Builder<B>> {
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

    interface Builder<B extends Builder<B>> extends OntopQueryAnsweringBuilderFragment<B>, OntopOBDAConfiguration.Builder<B>,
            OntopOptimizationConfiguration.Builder<B> {

        @Override
        OntopQueryAnsweringConfiguration build();
    }

}
