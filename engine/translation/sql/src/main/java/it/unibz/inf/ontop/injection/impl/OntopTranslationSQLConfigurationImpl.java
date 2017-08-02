package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopTranslationSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopTranslationSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopSQLCoreConfigurationImpl.OntopSQLOptions;

import java.util.Properties;
import java.util.stream.Stream;

public class OntopTranslationSQLConfigurationImpl extends OntopTranslationConfigurationImpl
        implements OntopTranslationSQLConfiguration {

    private final OntopTranslationSQLSettings settings;
    private final OntopSQLCoreConfigurationImpl sqlConfiguration;

    OntopTranslationSQLConfigurationImpl(OntopTranslationSQLSettings settings,
                                         OntopTranslationSQLOptions options) {
        super(settings, options.qaOptions);
        this.settings = settings;
        this.sqlConfiguration = new OntopSQLCoreConfigurationImpl(settings, options.sqlOptions);
    }

    @Override
    public OntopTranslationSQLSettings getSettings() {
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

    static class DefaultOntopTranslationSQLBuilderFragment<B extends OntopTranslationSQLConfiguration.Builder<B>>
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
