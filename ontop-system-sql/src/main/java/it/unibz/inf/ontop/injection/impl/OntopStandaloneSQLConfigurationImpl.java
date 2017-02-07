package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopQueryAnsweringConfigurationImpl.DefaultOntopQueryAnsweringBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopQueryAnsweringSQLConfigurationImpl.DefaultOntopQueryAnsweringSQLBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopQueryAnsweringSQLConfigurationImpl.OntopQueryAnsweringSQLOptions;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;


public class OntopStandaloneSQLConfigurationImpl extends OntopMappingSQLAllConfigurationImpl
        implements OntopStandaloneSQLConfiguration {

    private final OntopStandaloneSQLSettings settings;
    private final OntopQueryAnsweringConfigurationImpl qaConfiguration;

    OntopStandaloneSQLConfigurationImpl(OntopStandaloneSQLSettings settings, OntopStandaloneSQLOptions options) {
        super(settings, options.mappingOptions);
        this.settings = settings;
        qaConfiguration = new OntopQueryAnsweringSQLConfigurationImpl(settings, options.qaOptions);
    }

    @Override
    public OntopStandaloneSQLSettings getSettings() {
        return settings;
    }

    @Override
    public Optional<IRIDictionary> getIRIDictionary() {
        return qaConfiguration.getIRIDictionary();
    }

    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                qaConfiguration.buildGuiceModules());
    }



    static class OntopStandaloneSQLOptions {
        final OntopQueryAnsweringSQLOptions qaOptions;
        final OntopMappingSQLAllOptions mappingOptions;

        OntopStandaloneSQLOptions(OntopQueryAnsweringSQLOptions qaOptions, OntopMappingSQLAllOptions mappingOptions) {
            this.qaOptions = qaOptions;
            this.mappingOptions = mappingOptions;
        }
    }



    static abstract class OntopStandaloneSQLBuilderMixin<B extends OntopStandaloneSQLConfiguration.Builder<B>>
            extends OntopMappingSQLAllConfigurationImpl.OntopMappingSQLAllBuilderMixin<B>
            implements OntopStandaloneSQLConfiguration.Builder<B> {

        private final DefaultOntopQueryAnsweringSQLBuilderFragment<B> sqlQAFragmentBuilder;
        private final DefaultOntopQueryAnsweringBuilderFragment<B> qaFragmentBuilder;

        OntopStandaloneSQLBuilderMixin() {
            B builder = (B) this;
            this.sqlQAFragmentBuilder = new DefaultOntopQueryAnsweringSQLBuilderFragment<>(builder);
            this.qaFragmentBuilder = new DefaultOntopQueryAnsweringBuilderFragment<>(builder);
        }

        @Override
        public B enableIRISafeEncoding(boolean enable) {
            return qaFragmentBuilder.enableIRISafeEncoding(enable);
        }

        @Override
        public B enableExistentialReasoning(boolean enable) {
            return qaFragmentBuilder.enableExistentialReasoning(enable);
        }

        @Override
        public B iriDictionary(@Nonnull IRIDictionary iriDictionary) {
            return qaFragmentBuilder.iriDictionary(iriDictionary);
        }

        @Override
        protected Properties generateProperties() {
            Properties p = super.generateProperties();
            p.putAll(sqlQAFragmentBuilder.generateProperties());
            p.putAll(qaFragmentBuilder.generateProperties());
            return p;
        }

        final OntopStandaloneSQLOptions generateStandaloneSQLOptions() {
            OntopMappingSQLAllOptions sqlMappingOptions = generateMappingSQLAllOptions();
            OntopQueryAnsweringConfigurationImpl.OntopQueryAnsweringOptions qaOptions = this.qaFragmentBuilder.generateQAOptions(
                    sqlMappingOptions.mappingSQLOptions.mappingOptions.obdaOptions,
                    sqlMappingOptions.mappingSQLOptions.mappingOptions.optimizationOptions);

            return new OntopStandaloneSQLOptions(
                    sqlQAFragmentBuilder.generateQASQLOptions(qaOptions, sqlMappingOptions.mappingSQLOptions.sqlOptions),
                    sqlMappingOptions);
        }

    }

    public static final class BuilderImpl<B extends OntopStandaloneSQLConfiguration.Builder<B>>
            extends OntopStandaloneSQLBuilderMixin<B> {

        @Override
        public OntopStandaloneSQLConfiguration build() {
            OntopStandaloneSQLSettings settings = new OntopStandaloneSQLSettingsImpl(generateProperties(),
                    isR2rml());
            OntopStandaloneSQLOptions options = generateStandaloneSQLOptions();
            return new OntopStandaloneSQLConfigurationImpl(settings, options);
        }
    }



}
