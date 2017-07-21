package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.QueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.owlrefplatform.core.QueryCache;
import it.unibz.inf.ontop.owlrefplatform.core.reformulation.DummyRewriter;
import it.unibz.inf.ontop.owlrefplatform.core.reformulation.ExistentialQueryRewriter;
import it.unibz.inf.ontop.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SameAsRewriter;

/**
 * POST-module: to be loaded after that all the dependencies of concrete implementations have been defined
 *
 */
public class OntopTranslationPostModule extends OntopAbstractModule {

    private final OntopTranslationSettings settings;

    protected OntopTranslationPostModule(OntopTranslationSettings settings) {
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

        bindFromPreferences(QueryCache.class);

        Module reformulationFactoryModule = buildFactory(
                ImmutableList.of(
                        QueryUnfolder.class,
                        NativeQueryGenerator.class,
                        SameAsRewriter.class,
                        InputQueryTranslator.class),
                TranslationFactory.class);
        install(reformulationFactoryModule);

        Module translationFactoryModule = buildFactory(ImmutableList.of(
                QueryTranslator.class),
                OntopTranslationFactory.class);
        install(translationFactoryModule);
    }
}
