package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopSystemSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.spec.OBDASpecification;

import java.util.stream.Stream;


/**
 * "Abstract" class (loadSpecification() not implemented)
 */
public class OntopSystemSQLConfigurationImpl extends OntopTranslationSQLConfigurationImpl
        implements OntopSystemSQLConfiguration {

    private final OntopSystemSQLSettings settings;

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

    @Override
    public OBDASpecification loadSpecification() throws OBDASpecificationException {
        throw new IllegalStateException("loadSpecification() must be implemented by a sub-class");
    }

    static class OntopSystemSQLOptions {
        final OntopTranslationSQLOptions sqlTranslationOptions;

        /**
         * TODO: make it private when there will be a OntopSystemSQLBuilderFragment
         */
        OntopSystemSQLOptions(OntopTranslationSQLOptions sqlTranslationOptions) {
            this.sqlTranslationOptions = sqlTranslationOptions;
        }
    }
}
