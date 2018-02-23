package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopOBDAConfiguration;
import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.spec.OBDASpecification;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;


public abstract class OntopOBDAConfigurationImpl extends OntopModelConfigurationImpl implements OntopOBDAConfiguration {

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

    @Override
    public final OBDASpecification loadSpecification() throws OBDASpecificationException {
        if (options.optionalSpecification.isPresent())
            return options.optionalSpecification.get();

        return loadOBDASpecification();
    }

    protected abstract OBDASpecification loadOBDASpecification() throws OBDASpecificationException;

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new OntopOBDAModule(this)));
    }

    @Override
    public SpecificationFactory getSpecificationFactory() {
        return getInjector().getInstance(SpecificationFactory.class);
    }


    static class OntopOBDAOptions {

        final OntopModelConfigurationOptions modelOptions;
        final Optional<OBDASpecification> optionalSpecification;

        private OntopOBDAOptions(OntopModelConfigurationOptions modelOptions,
                                 Optional<OBDASpecification> optionalSpecification) {
            this.modelOptions = modelOptions;
            this.optionalSpecification = optionalSpecification;
        }
    }

    static class DefaultOntopOBDABuilderFragment<B extends OntopOBDAConfiguration.Builder<B>>
            implements OntopOBDABuilderFragment<B> {

        private final B builder;
        private final Runnable declareSpecificationCB;
        private Optional<Boolean> sameAsMappings = Optional.empty();
        private Optional<OBDASpecification> specification = Optional.empty();

        DefaultOntopOBDABuilderFragment(B builder, Runnable declareSpecificationCB) {
            this.builder = builder;
            this.declareSpecificationCB = declareSpecificationCB;
        }

        @Override
        public B obdaSpecification(OBDASpecification specification) {
            declareSpecificationCB.run();
            this.specification = Optional.of(specification);
            return builder;
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
            return new OntopOBDAOptions(modelOptions, specification);
        }

        /**
         * Returns true if the user assigned a OBDA specification object
         */
        boolean isOBDASpecificationAssigned() {
            return specification.isPresent();
        }

    }

    static abstract class OntopOBDAConfigurationBuilderMixin<B extends OntopOBDAConfiguration.Builder<B>>
            implements OntopOBDAConfiguration.Builder<B> {

        private final DefaultOntopOBDABuilderFragment<B> localBuilderFragment;
        private final DefaultOntopModelBuilderFragment<B> modelBuilderFragment;

        OntopOBDAConfigurationBuilderMixin() {
            localBuilderFragment = new DefaultOntopOBDABuilderFragment<>(
                    (B) this, this::declareOBDASpecificationAssigned);
            modelBuilderFragment = new DefaultOntopModelBuilderFragment<>((B) this);
        }

        @Override
        public B obdaSpecification(OBDASpecification specification) {
            return localBuilderFragment.obdaSpecification(specification);
        }

        @Override
        public B sameAsMappings(boolean enable) {
            return localBuilderFragment.sameAsMappings(enable);
        }

        final OntopOBDAOptions generateOBDAOptions() {
            return localBuilderFragment.generateOBDAOptions(modelBuilderFragment.generateModelOptions());
        }

        protected Properties generateProperties() {
            Properties properties = modelBuilderFragment.generateProperties();
            properties.putAll(localBuilderFragment.generateProperties());
            return properties;
        }

        /**
         * Hook: can be overloaded
         */
        void declareOBDASpecificationAssigned() {
        }

        /**
         * Returns true if the user assigned a OBDA specification object
         */
        boolean isOBDASpecificationAssigned() {
            return localBuilderFragment.isOBDASpecificationAssigned();
        }
    }

}
