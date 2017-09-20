package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
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
}
