package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopQueryAnsweringSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopQueryAnsweringSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopSQLCoreConfigurationImpl.OntopSQLOptions;

import java.util.Properties;
import java.util.stream.Stream;

public class OntopQueryAnsweringSQLConfigurationImpl extends OntopQueryAnsweringConfigurationImpl
        implements OntopQueryAnsweringSQLConfiguration {

    private final OntopQueryAnsweringSQLSettings settings;
    private final OntopSQLCoreConfigurationImpl sqlConfiguration;

    OntopQueryAnsweringSQLConfigurationImpl(OntopQueryAnsweringSQLSettings settings,
                                            OntopQueryAnsweringSQLOptions options) {
        super(settings, options.qaOptions);
        this.settings = settings;
        this.sqlConfiguration = new OntopSQLCoreConfigurationImpl(settings, options.sqlOptions);
    }

    @Override
    public OntopQueryAnsweringSQLSettings getSettings() {
        return settings;
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                Stream.concat(
                        super.buildGuiceModules(),
                        sqlConfiguration.buildGuiceModules()),
                Stream.of(new OntopQueryAnsweringSQLModule(this),
                        new OntopQueryAnsweringPostModule(getSettings())));
    }

    static class OntopQueryAnsweringSQLOptions {

        final OntopQueryAnsweringOptions qaOptions;
        final OntopSQLOptions sqlOptions;

        private OntopQueryAnsweringSQLOptions(OntopQueryAnsweringOptions qaOptions, OntopSQLOptions sqlOptions) {
            this.qaOptions = qaOptions;
            this.sqlOptions = sqlOptions;
        }
    }

    static class DefaultOntopQueryAnsweringSQLBuilderFragment<B extends OntopQueryAnsweringSQLConfiguration.Builder<B>>
        implements OntopQueryAnsweringSQLBuilderFragment<B> {

        private final B builder;

        DefaultOntopQueryAnsweringSQLBuilderFragment(B builder) {
            this.builder = builder;
        }

        OntopQueryAnsweringSQLOptions generateQASQLOptions(OntopQueryAnsweringOptions qaOptions,
                                                           OntopSQLOptions sqlOptions) {
            return new OntopQueryAnsweringSQLOptions(qaOptions, sqlOptions);
        }

        Properties generateProperties() {
            return new Properties();
        }
    }

}
