package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;
import it.unibz.inf.ontop.injection.OntopSystemSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;

import java.util.stream.Stream;


public class OntopSystemSQLConfigurationImpl extends OntopReformulationSQLConfigurationImpl
        implements OntopSystemSQLConfiguration {

    private final OntopSystemSQLSettings settings;

    OntopSystemSQLConfigurationImpl(OntopSystemSQLSettings settings, OntopSystemSQLOptions options,
                                    SpecificationLoader specificationLoader) {
        super(settings, options.sqlTranslationOptions, specificationLoader);
        this.settings = settings;
    }

    OntopSystemSQLConfigurationImpl(OntopSystemSQLSettings settings, OntopSystemSQLOptions options) {
        super(settings, options.sqlTranslationOptions);
        this.settings = settings;
    }

    @Override
    public OntopSystemSQLSettings getSettings() {
        return settings;
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(
                        new OntopSystemModule(settings),
                        new OntopSystemSQLModule(settings),
                        new OntopSystemPostModule(settings)));
    }

    static class OntopSystemSQLOptions {
        final OntopReformulationSQLOptions sqlTranslationOptions;

        /**
         * TODO: make it private when there will be a OntopSystemSQLBuilderFragment
         */
        OntopSystemSQLOptions(OntopReformulationSQLOptions sqlTranslationOptions) {
            this.sqlTranslationOptions = sqlTranslationOptions;
        }
    }

    static abstract class OntopSystemSQLBuilderMixin<B extends OntopSystemSQLConfiguration.Builder<B>>
            extends OntopReformulationSQLBuilderMixin<B>
            implements OntopSystemSQLConfiguration.Builder<B> {

        final OntopSystemSQLOptions generateSystemSQLOptions() {
            return new OntopSystemSQLOptions(generateSQLReformulationOptions());
        }
    }

    /**
     * Requires the OBDA specification to be already assigned
     */
    public static class BuilderImpl<B extends OntopSystemSQLConfiguration.Builder<B>>
            extends OntopSystemSQLBuilderMixin<B> {

        @Override
        public OntopSystemSQLConfiguration build() {
            if (!isOBDASpecificationAssigned())
                throw new InvalidOntopConfigurationException("An OBDA specification must be assigned " +
                        "to directly instantiate such a OntopReformulationSQLConfiguration");

            OntopSystemSQLSettings settings = new OntopSystemSQLSettingsImpl(generateProperties());
            OntopSystemSQLOptions options = generateSystemSQLOptions();
            return new OntopSystemSQLConfigurationImpl(settings, options);
        }
    }
}
