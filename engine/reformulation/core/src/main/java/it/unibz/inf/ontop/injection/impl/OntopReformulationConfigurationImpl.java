package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.ReformulationFactory;
import it.unibz.inf.ontop.injection.OntopReformulationConfiguration;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.spec.OBDASpecification;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;


public class OntopReformulationConfigurationImpl extends OntopKGQueryConfigurationImpl implements OntopReformulationConfiguration {
    private final OntopReformulationSettings settings;

    @Nullable
    private final SpecificationLoader specificationLoader;

    OntopReformulationConfigurationImpl(OntopReformulationSettings settings, OntopReformulationOptions options,
                                        SpecificationLoader specificationLoader) {
        super(settings, options.queryOptions);
        this.settings = settings;
        this.specificationLoader = specificationLoader;
    }

    OntopReformulationConfigurationImpl(OntopReformulationSettings settings, OntopReformulationOptions options,
        SpecificationLoader specificationLoader, Supplier<Injector> injectorSupplier) {
        super(settings, options.queryOptions, injectorSupplier);
        this.settings = settings;
        this.specificationLoader = specificationLoader;
    }

    /**
     * When the OBDASpecification object is given by the user.
     *
     * The configuration builder is in charge of MAKING SURE such an object is PROVIDED before calling this constructor.
     *
     */
    OntopReformulationConfigurationImpl(OntopReformulationSettings settings, OntopReformulationOptions options) {
        super(settings, options.queryOptions);
        this.settings = settings;
        this.specificationLoader = null;
    }
    
    OntopReformulationConfigurationImpl(OntopReformulationSettings settings, OntopReformulationOptions options, Supplier<Injector> injectorSupplier) {
        super(settings, options.queryOptions, injectorSupplier);
        this.settings = settings;
        this.specificationLoader = null;
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new OntopTranslationModule(this)));
    }

    @Override
    public OntopReformulationSettings getSettings() {
        return settings;
    }

    /**
     * This method should not be called when the specification loader is not provided
     * (--> an OBDA specification object is expected to be provided)
     */
    @Override
    protected final OBDASpecification loadOBDASpecification() throws OBDASpecificationException {
        if (specificationLoader == null)
            throw new MissingOBDASpecificationObjectException();

        return specificationLoader.load();
    }

    @Override
    public QueryReformulator loadQueryReformulator() throws OBDASpecificationException {
        ReformulationFactory reformulationFactory = getInjector().getInstance(ReformulationFactory.class);
        OBDASpecification obdaSpecification = loadSpecification();

        return reformulationFactory.create(obdaSpecification);
    }

    @Override
    public KGQueryFactory getKGQueryFactory() {
        return getInjector()
                .getInstance(KGQueryFactory.class);
    }

    static class OntopReformulationOptions {
        final OntopKGQueryOptions queryOptions;

        OntopReformulationOptions(OntopKGQueryOptions queryOptions) {
            this.queryOptions = queryOptions;
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static abstract class DefaultOntopReformulationBuilderFragment<B extends OntopReformulationConfiguration.Builder<B>>
            implements OntopReformulationBuilderFragment<B> {

        private Optional<Boolean> existentialReasoning = Optional.empty();

        protected abstract B self();

        @Override
        public B enableExistentialReasoning(boolean enable) {
            this.existentialReasoning = Optional.of(enable);
            return self();
        }

        protected Properties generateProperties() {
            Properties p = new Properties();
            existentialReasoning.ifPresent(r -> p.put(OntopReformulationSettings.EXISTENTIAL_REASONING, r));
            return p;
        }

        protected OntopReformulationOptions generateReformulationOptions(OntopKGQueryOptions queryOptions) {
            return new OntopReformulationOptions(queryOptions);
        }
    }

    protected static abstract class OntopReformulationBuilderMixin<B extends OntopReformulationConfiguration.Builder<B>>
            extends OntopKGQueryBuilderMixin<B>
            implements OntopReformulationConfiguration.Builder<B> {

        private final DefaultOntopReformulationBuilderFragment<B> localBuilderFragment;

        OntopReformulationBuilderMixin() {
            localBuilderFragment = new DefaultOntopReformulationBuilderFragment<>() {
                @Override
                protected B self() {
                    return OntopReformulationBuilderMixin.this.self();
                }
            };
        }

        @Override
        public B enableExistentialReasoning(boolean enable) {
            return localBuilderFragment.enableExistentialReasoning(enable);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(localBuilderFragment.generateProperties());
            return properties;
        }

        protected final OntopReformulationOptions generateReformulationOptions() {
            OntopKGQueryOptions queryOptions = generateKGQueryOptions();
            return localBuilderFragment.generateReformulationOptions(queryOptions);
        }
    }

    /**
     * Exception
     */
    private static class MissingOBDASpecificationObjectException extends OntopInternalBugException {

        private MissingOBDASpecificationObjectException() {
            super("Bug: this configuration does not received an OBDA specification object nor a specification loader" +
                    "and thus should have been constructed.\nPlease fix the configuration builder implementation.");
        }
    }

}
