package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;


public class OntopMappingConfigurationImpl extends OntopOBDAConfigurationImpl implements OntopMappingConfiguration {

    private final OntopMappingSettings settings;
    private final OntopMappingOptions options;

    OntopMappingConfigurationImpl(OntopMappingSettings settings, OntopMappingOptions options) {
        super(settings, options.obdaOptions);
        this.settings = settings;
        this.options = options;
    }

    @Override
    public Optional<ImplicitDBConstraintsReader> getImplicitDBConstraintsReader() {
        return options.implicitDBConstraintsReader;
    }

    @Override
    public OntopMappingSettings getSettings() {
        return settings;
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new OntopMappingModule(this)));
    }

    static class OntopMappingOptions {

        final OntopOBDAOptions obdaOptions;
        final Optional<ImplicitDBConstraintsReader> implicitDBConstraintsReader;

        private OntopMappingOptions(Optional<ImplicitDBConstraintsReader> implicitDBConstraintsReader,
                                    OntopOBDAOptions obdaOptions) {
            this.implicitDBConstraintsReader = implicitDBConstraintsReader;
            this.obdaOptions = obdaOptions;
        }
    }

    static class DefaultOntopMappingBuilderFragment<B extends OntopMappingConfiguration.Builder>
            implements OntopMappingBuilderFragment<B> {

        private final B builder;
        private Optional<ImplicitDBConstraintsReader> userConstraints = Optional.empty();
        private Optional<Boolean> obtainFullMetadata = Optional.empty();

        DefaultOntopMappingBuilderFragment(B builder) {
            this.builder = builder;
        }


        @Override
        public B dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints) {
            this.userConstraints = Optional.of(constraints);
            return builder;
        }

        @Override
        public B enableFullMetadataExtraction(boolean obtainFullMetadata) {
            this.obtainFullMetadata = Optional.of(obtainFullMetadata);
            return builder;
        }

        protected final OntopMappingOptions generateMappingOptions(OntopOBDAOptions obdaOptions) {
            return new OntopMappingOptions(userConstraints, obdaOptions);
        }

        Properties generateProperties() {
            Properties properties = new Properties();
            obtainFullMetadata.ifPresent(m -> properties.put(OntopMappingSettings.OBTAIN_FULL_METADATA, m));
            return properties;
        }

    }

    static abstract class OntopMappingBuilderMixin<B extends OntopMappingConfiguration.Builder>
        extends OntopOBDAConfigurationBuilderMixin<B>
        implements OntopMappingConfiguration.Builder<B> {

        private final DefaultOntopMappingBuilderFragment<B> mappingBuilderFragment;

        OntopMappingBuilderMixin() {
            this.mappingBuilderFragment = new DefaultOntopMappingBuilderFragment<>((B)this);
        }

        @Override
        public B dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints) {
            return mappingBuilderFragment.dbConstraintsReader(constraints);
        }

        @Override
        public B enableFullMetadataExtraction(boolean obtainFullMetadata) {
            return mappingBuilderFragment.enableFullMetadataExtraction(obtainFullMetadata);
        }

        final OntopMappingOptions generateMappingOptions() {
            return generateMappingOptions(generateOBDAOptions());
        }

        final OntopMappingOptions generateMappingOptions(OntopOBDAOptions obdaOptions) {
            return mappingBuilderFragment.generateMappingOptions(obdaOptions);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = new Properties();
            properties.putAll(super.generateProperties());
            properties.putAll(mappingBuilderFragment.generateProperties());

            return properties;
        }

    }

    public static class BuilderImpl<B extends OntopMappingConfiguration.Builder>
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
