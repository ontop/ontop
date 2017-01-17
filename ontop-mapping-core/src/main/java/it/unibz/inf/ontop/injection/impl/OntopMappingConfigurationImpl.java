package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;

import java.util.Properties;
import java.util.stream.Stream;


public class OntopMappingConfigurationImpl extends OntopOBDAConfigurationImpl implements OntopMappingConfiguration {

    private final OntopMappingSettings settings;

    OntopMappingConfigurationImpl(OntopMappingSettings settings, OntopMappingOptions options) {
        super(settings, options.obdaOptions);
        this.settings = settings;
    }

    @Override
    public OntopMappingSettings getSettings() {
        return settings;
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.of(new OntopMappingModule(this));
    }

    static class OntopMappingOptions {

        final OntopOBDAOptions obdaOptions;

        private OntopMappingOptions(OntopOBDAOptions obdaOptions) {
            this.obdaOptions = obdaOptions;
        }
    }

    static class DefaultOntopMappingBuilderFragment<B extends OntopMappingConfiguration.Builder>
            implements OntopMappingBuilderFragment<B> {

        private final B builder;

        DefaultOntopMappingBuilderFragment(B builder) {
            this.builder = builder;
        }

        protected final OntopMappingOptions generateMappingOptions(OntopOBDAOptions obdaOptions) {
            return new OntopMappingOptions(obdaOptions);
        }

        Properties generateProperties() {
            return new Properties();
        }

    }

    static abstract class OntopMappingBuilderMixin<B extends OntopMappingConfiguration.Builder>
        extends OntopOBDAConfigurationBuilderMixin<B>
        implements OntopMappingConfiguration.Builder<B> {

        private final DefaultOntopMappingBuilderFragment<B> mappingBuilderFragment;

        OntopMappingBuilderMixin() {
            this.mappingBuilderFragment = new DefaultOntopMappingBuilderFragment<>((B)this);
        }

        final OntopMappingOptions generateMappingOptions() {
            return generateMappingOptions(generateOBDAOptions());
        }

        final OntopMappingOptions generateMappingOptions(OntopOBDAOptions obdaOptions) {
            return new OntopMappingOptions(obdaOptions);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = new Properties();
            properties.putAll(super.generateProperties());
            properties.putAll(mappingBuilderFragment.generateProperties());

            return properties;
        }

    }

    public static class BuilderImpl<B extends OntopMappingConfiguration.Builder<B>>
            extends OntopMappingBuilderMixin<B> {

        @Override
        public OntopMappingConfiguration build() {
            Properties properties = generateProperties();
            OntopMappingSettings settings = new OntopMappingSettingsImpl(properties);

            OntopMappingOptions options = generateMappingOptions();

            return new OntopMappingConfigurationImpl(settings, options);
        }
    }

}
