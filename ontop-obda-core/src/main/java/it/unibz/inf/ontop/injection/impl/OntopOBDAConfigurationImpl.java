package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopOBDAConfiguration;
import it.unibz.inf.ontop.injection.OntopOBDASettings;

import java.util.Properties;
import java.util.stream.Stream;


public class OntopOBDAConfigurationImpl extends OntopModelConfigurationImpl implements OntopOBDAConfiguration {

    private final OntopOBDASettings settings;

    OntopOBDAConfigurationImpl(OntopOBDASettings settings, OntopOBDAOptions options) {
        super(settings, options.modelOptions);
        this.settings = settings;
    }

    @Override
    public OntopOBDASettings getSettings() {
        return settings;
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.of(new OntopOBDAModule(this));
    }


    static class OntopOBDAOptions {

        final OntopModelConfigurationOptions modelOptions;

        private OntopOBDAOptions(OntopModelConfigurationOptions modelOptions) {
            this.modelOptions = modelOptions;
        }
    }


    static abstract class OntopOBDAConfigurationBuilderMixin<B extends OntopOBDAConfiguration.Builder>
            extends DefaultOntopModelBuilderFragment<B>
            implements OntopOBDAConfiguration.Builder<B> {

        final OntopOBDAOptions generateOBDAOptions() {
            return new OntopOBDAOptions(generateModelOptions());
        }
    }


    public static class BuilderImpl<B extends OntopOBDAConfiguration.Builder>
            extends OntopOBDAConfigurationBuilderMixin<B> {

        @Override
        public OntopOBDAConfiguration build() {
            Properties properties = generateProperties();
            OntopOBDASettings settings = new OntopOBDASettingsImpl(properties);

            OntopOBDAOptions options = generateOBDAOptions();

            return new OntopOBDAConfigurationImpl(settings, options);
        }

    }

}
