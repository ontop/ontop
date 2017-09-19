package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopReformulationSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopSQLCoreConfigurationImpl.OntopSQLOptions;

import java.util.Properties;
import java.util.stream.Stream;

public class OntopReformulationSQLConfigurationImpl extends OntopReformulationConfigurationImpl
        implements OntopReformulationSQLConfiguration {

    private final OntopReformulationSQLSettings settings;
    private final OntopSQLCoreConfigurationImpl sqlConfiguration;

    OntopReformulationSQLConfigurationImpl(OntopReformulationSQLSettings settings,
                                           OntopTranslationSQLOptions options) {
        super(settings, options.qaOptions);
        this.settings = settings;
        this.sqlConfiguration = new OntopSQLCoreConfigurationImpl(settings, options.sqlOptions);
    }

    @Override
    public OntopReformulationSQLSettings getSettings() {
        return settings;
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                Stream.concat(
                        super.buildGuiceModules(),
                        sqlConfiguration.buildGuiceModules()),
                Stream.of(new OntopTranslationSQLModule(this),
                        new OntopTranslationPostModule(getSettings())));
    }

    static class OntopTranslationSQLOptions {

        final OntopTranslationOptions qaOptions;
        final OntopSQLOptions sqlOptions;

        private OntopTranslationSQLOptions(OntopTranslationOptions qaOptions, OntopSQLOptions sqlOptions) {
            this.qaOptions = qaOptions;
            this.sqlOptions = sqlOptions;
        }
    }

    static class DefaultOntopTranslationSQLBuilderFragment<B extends OntopReformulationSQLConfiguration.Builder<B>>
        implements OntopQueryAnsweringSQLBuilderFragment<B> {

        private final B builder;

        DefaultOntopTranslationSQLBuilderFragment(B builder) {
            this.builder = builder;
        }

        OntopTranslationSQLOptions generateSQLTranslationOptions(OntopTranslationOptions qaOptions,
                                                                 OntopSQLOptions sqlOptions) {
            return new OntopTranslationSQLOptions(qaOptions, sqlOptions);
        }

        Properties generateProperties() {
            return new Properties();
        }
    }

}
