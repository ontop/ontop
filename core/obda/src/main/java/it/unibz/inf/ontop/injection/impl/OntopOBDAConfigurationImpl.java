package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopOBDAConfiguration;
import it.unibz.inf.ontop.injection.OntopOBDASettings;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class OntopOBDAConfigurationImpl extends OntopModelConfigurationImpl implements OntopOBDAConfiguration {

    private final OntopOBDASettings settings;
    private final OntopOBDAOptions options;

    OntopOBDAConfigurationImpl(OntopOBDASettings settings, OntopOBDAOptions options) {
        super(settings, options.modelOptions);
        this.settings = settings;
        this.options = options;
    }

    OntopOBDAConfigurationImpl(OntopOBDASettings settings, OntopOBDAOptions options, Supplier<Injector> injectorSupplier) {
        super(settings, options.modelOptions, injectorSupplier);
        this.settings = settings;
        this.options = options;
    }

    @Override
    public OntopOBDASettings getSettings() {
        return settings;
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new OntopOBDAModule(this)));
    }


    static class OntopOBDAOptions {

        final OntopModelConfigurationOptions modelOptions;

        private OntopOBDAOptions(OntopModelConfigurationOptions modelOptions) {
            this.modelOptions = modelOptions;
        }
    }

    static class DefaultOntopOBDABuilderFragment<B extends OntopOBDAConfiguration.Builder<B>>
            implements OntopOBDABuilderFragment<B> {

        private final B builder;
        private Optional<Boolean> sameAsMappings = Optional.empty();

        DefaultOntopOBDABuilderFragment(B builder) {
            this.builder = builder;
        }

        @Override
        public B sameAsMappings(boolean sameAsMappings) {
            this.sameAsMappings = Optional.of(sameAsMappings);
            return builder;
        }

        Properties generateProperties() {
            Properties p = new Properties();
            sameAsMappings.ifPresent(b -> p.put(OntopOBDASettings.SAME_AS, b));

            return p;
        }

        final OntopOBDAOptions generateOBDAOptions(OntopModelConfigurationOptions modelOptions) {
            return new OntopOBDAOptions(modelOptions);
        }

    }

    static abstract class OntopOBDAConfigurationBuilderMixin<B extends OntopOBDAConfiguration.Builder<B>>
            extends DefaultOntopModelBuilderFragment<B>
            implements OntopOBDAConfiguration.Builder<B> {

        private final DefaultOntopOBDABuilderFragment<B> localBuilderFragment;

        OntopOBDAConfigurationBuilderMixin() {
            localBuilderFragment = new DefaultOntopOBDABuilderFragment<>((B) this);
        }

        final OntopOBDAOptions generateOBDAOptions() {
            return localBuilderFragment.generateOBDAOptions(generateModelOptions());
        }

        @Override
        public B sameAsMappings(boolean enable) {
            return localBuilderFragment.sameAsMappings(enable);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(localBuilderFragment.generateProperties());
            return properties;
        }
    }


    public static class BuilderImpl<B extends OntopOBDAConfiguration.Builder<B>>
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
