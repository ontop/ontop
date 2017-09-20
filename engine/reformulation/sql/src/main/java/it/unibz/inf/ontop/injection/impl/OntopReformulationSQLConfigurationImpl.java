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
                                           OntopReformulationSQLOptions options,
                                           SpecificationLoader specificationLoader) {
        super(settings, options.qaOptions, specificationLoader);
        this.settings = settings;
        this.sqlConfiguration = new OntopSQLCoreConfigurationImpl(settings, options.sqlOptions);
    }

    /**
     * Assumes the OBDA specification to be already assigned
     */
    OntopReformulationSQLConfigurationImpl(OntopReformulationSQLSettings settings,
                                           OntopReformulationSQLOptions options) {
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
                Stream.of(new OntopReformulationSQLModule(this),
                        new OntopReformulationPostModule(getSettings())));
    }

    static class OntopReformulationSQLOptions {

        final OntopReformulationOptions qaOptions;
        final OntopSQLOptions sqlOptions;

        private OntopReformulationSQLOptions(OntopReformulationOptions qaOptions, OntopSQLOptions sqlOptions) {
            this.qaOptions = qaOptions;
            this.sqlOptions = sqlOptions;
        }
    }

    static class DefaultOntopReformulationSQLBuilderFragment<B extends OntopReformulationSQLConfiguration.Builder<B>>
        implements OntopReformulationSQLBuilderFragment<B> {

        private final B builder;

        DefaultOntopReformulationSQLBuilderFragment(B builder) {
            this.builder = builder;
        }

        OntopReformulationSQLOptions generateSQLReformulationOptions(OntopReformulationOptions qaOptions,
                                                                     OntopSQLOptions sqlOptions) {
            return new OntopReformulationSQLOptions(qaOptions, sqlOptions);
        }

        Properties generateProperties() {
            return new Properties();
        }
    }

}
