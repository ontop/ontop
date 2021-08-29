package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.rewriting.ExistentialQueryRewriter;
import it.unibz.inf.ontop.answering.reformulation.rewriting.QueryRewriter;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.DummyRewriter;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.injection.ReformulationFactory;
import it.unibz.inf.ontop.injection.TranslationFactory;

/**
 * POST-module: to be loaded after that all the dependencies of concrete implementations have been defined
 *
 */
@SuppressWarnings("unchecked")
public class OntopReformulationPostModule extends OntopAbstractModule {

    private final OntopReformulationSettings settings;

    protected OntopReformulationPostModule(OntopReformulationSettings settings) {
        super(settings);
        this.settings = settings;
    }

    @Override
    protected void configure() {
        if (settings.isExistentialReasoningEnabled()) {
            bind(QueryRewriter.class).to(getImplementation(ExistentialQueryRewriter.class));
        }
        else {
            bind(QueryRewriter.class).to(DummyRewriter.class);
        }

        bindFromSettings(QueryCache.class);
        bindFromSettings(InputQueryTranslator.class);

        Module reformulationFactoryModule = buildFactory(
                ImmutableList.of(
                        QueryUnfolder.class,
                        NativeQueryGenerator.class),
                TranslationFactory.class);
        install(reformulationFactoryModule);

        Module translationFactoryModule = buildFactory(
                ImmutableList.of(QueryReformulator.class),
                ReformulationFactory.class);
        install(translationFactoryModule);
    }
}
