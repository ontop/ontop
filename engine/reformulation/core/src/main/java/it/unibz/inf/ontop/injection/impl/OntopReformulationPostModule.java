package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.answering.reformulation.rewriting.ExistentialQueryRewriter;
import it.unibz.inf.ontop.answering.reformulation.rewriting.QueryRewriter;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.DummyRewriter;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.injection.ReformulationFactory;
import it.unibz.inf.ontop.injection.TranslationFactory;

/**
 * POST-module: to be loaded after that all the dependencies of concrete implementations have been defined
 *
 */
public class OntopReformulationPostModule extends OntopAbstractModule {

    private final OntopReformulationSettings settings;

    protected OntopReformulationPostModule(OntopReformulationSettings settings) {
        super(settings);
        this.settings = settings;
    }

    @Override
    protected void configure() {
        if (settings.isExistentialReasoningEnabled()) {
            bindFromSettings(ExistentialQueryRewriter.class);
            bind(QueryRewriter.class).to(ExistentialQueryRewriter.class);
        }
        else {
            bind(QueryRewriter.class).to(DummyRewriter.class);
        }

        bindFromSettings(QueryCache.class);

        Module reformulationFactoryModule = buildFactory(
                ImmutableList.of(
                        NativeQueryGenerator.class),
                TranslationFactory.class);
        install(reformulationFactoryModule);

        Module translationFactoryModule = buildFactory(
                ImmutableList.of(QueryReformulator.class),
                ReformulationFactory.class);
        install(translationFactoryModule);
    }
}
