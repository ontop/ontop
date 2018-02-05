package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.spec.OBDASpecification;
import org.apache.commons.rdf.api.Graph;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class OntopTemporalSQLOWLAPIConfigurationImpl
        extends OntopSQLOWLAPIConfigurationImpl
        implements OntopTemporalSQLOWLAPIConfiguration {

    private final OntopTemporalMappingSQLAllConfigurationImpl temporalConfiguration;
    private final OntopTemporalSQLOWLAPIOptions options;

    private final OntopMappingOWLAPIConfigurationImpl mappingOWLConfiguration;

    OntopTemporalSQLOWLAPIConfigurationImpl(OntopStandaloneSQLSettings settings,
                                            OntopTemporalMappingSQLAllSettings temporalSettings,
                                            OntopTemporalSQLOWLAPIOptions options) {
        super(settings, options.owlOptions);
        this.options = options;
        temporalConfiguration = new OntopTemporalMappingSQLAllConfigurationImpl(temporalSettings, options.temporalOptions);
        mappingOWLConfiguration = new OntopMappingOWLAPIConfigurationImpl(settings, options.owlOptions.owlOptions);
    }

    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(super.buildGuiceModules(), Stream.concat(
                new OntopReformulationConfigurationImpl(getSettings(),
                        options.owlOptions.sqlOptions.systemOptions.sqlTranslationOptions.reformulationOptions).buildGuiceModules(),
                Stream.of(new OntopTemporalModule(temporalConfiguration)))
        );
    }

    @Override
    public OBDASpecification loadOBDASpecification() throws OBDASpecificationException {
        return temporalConfiguration.loadSpecification(mappingOWLConfiguration::loadOntology, options.ruleFile);
    }

    @Override
    public Optional<OWLOntology> loadInputOntology() throws OWLOntologyCreationException {
        return mappingOWLConfiguration.loadInputOntology();
    }

    static class OntopTemporalSQLOWLAPIOptions {
        final Optional<File> ruleFile;
        final Optional<Reader> ruleReader;
        final OntopSQLOWLAPIConfigurationImpl.OntopSQLOWLAPIOptions owlOptions;
        final OntopTemporalMappingSQLAllConfigurationImpl.OntopTemporalMappingSQLAllOptions temporalOptions;

        OntopTemporalSQLOWLAPIOptions(Optional<File> ruleFile, Optional<Reader> ruleReader, OntopSQLOWLAPIConfigurationImpl.OntopSQLOWLAPIOptions owlOptions, OntopTemporalMappingSQLAllConfigurationImpl.OntopTemporalMappingSQLAllOptions temporalOptions) {
            this.ruleFile = ruleFile;
            this.ruleReader = ruleReader;
            this.owlOptions = owlOptions;
            this.temporalOptions = temporalOptions;
        }
    }

    static class StandardOntopTemporalSQLOWLAPIBuilderFragment<B extends OntopTemporalSQLOWLAPIConfiguration.Builder<B>>
    implements OntopTemporalSQLOWLAPIBuilderFragment {


        private final B builder;

        Optional<File> ruleFile = Optional.empty();
        Optional<Reader> ruleReader = Optional.empty();

        boolean useRule = false;

        protected StandardOntopTemporalSQLOWLAPIBuilderFragment(B builder) {
            this.builder = builder;
        }

        @Override
        public B nativeOntopTemporalRuleFile(@Nonnull File ruleFile) {
            this.ruleFile = Optional.of(ruleFile);
            useRule = true;
            return builder;
        }

        @Override
        public B nativeOntopTemporalRuleFile(@Nonnull String ruleFilename) {
            try {
                URI fileURI = new URI(ruleFilename);
                String scheme = fileURI.getScheme();
                if (scheme == null) {
                    this.ruleFile = Optional.of(new File(fileURI.getPath()));
                } else if (scheme.equals("file")) {
                    this.ruleFile = Optional.of(new File(fileURI));
                } else {
                    throw new InvalidOntopConfigurationException("Currently only local files are supported" +
                            "as rule files");
                }
            } catch (URISyntaxException e) {
                throw new InvalidOntopConfigurationException("Invalid rule file path: " + e.getMessage());
            }
            useRule = true;
            return builder;
        }

        @Override
        public B nativeOntopTemporalRuleReader(@Nonnull Reader ruleReader) {
            this.ruleReader = Optional.of(ruleReader);
            useRule = true;
            return builder;
        }

    }

    static abstract class OntopTemporalSQLOWLAPIBuilderMixin<B extends OntopTemporalSQLOWLAPIConfiguration.Builder<B>>
            extends OntopSQLOWLAPIBuilderMixin<B>
            implements OntopTemporalSQLOWLAPIConfiguration.Builder<B> {

        private final OntopTemporalMappingSQLAllConfigurationImpl.StandardTemporalMappingSQLBuilderFragment<B> temporalMappingBuilderFragment;
        private final StandardOntopTemporalSQLOWLAPIBuilderFragment<B> localFragmentBuilder;

        OntopTemporalSQLOWLAPIBuilderMixin() {
            B builder = (B)this;
            localFragmentBuilder = new StandardOntopTemporalSQLOWLAPIBuilderFragment<>(builder);
            temporalMappingBuilderFragment = new OntopTemporalMappingSQLAllConfigurationImpl.StandardTemporalMappingSQLBuilderFragment<>(builder,
                    this::declareMappingDefined, this::declareImplicitConstraintSetDefined);

        }

        @Override
        public B nativeOntopTemporalRuleFile(@Nonnull File ruleFile) {
            return localFragmentBuilder.nativeOntopTemporalRuleFile(ruleFile);
        }

        @Override
        public B nativeOntopTemporalRuleFile(@Nonnull String ruleFilename) {
            return localFragmentBuilder.nativeOntopTemporalRuleFile(ruleFilename);
        }

        @Override
        public B nativeOntopTemporalRuleReader(@Nonnull Reader ruleReader) {
            return localFragmentBuilder.nativeOntopTemporalRuleReader(ruleReader);
        }

        @Override
        public B nativeOntopTemporalMappingFile(@Nonnull final File mappingFile) {
            return temporalMappingBuilderFragment.nativeOntopTemporalMappingFile(mappingFile);
        }

        @Override
        public B nativeOntopTemporalMappingFile(@Nonnull final String mappingFilename) {
            return temporalMappingBuilderFragment.nativeOntopTemporalMappingFile(mappingFilename);
        }

        @Override
        public B nativeOntopTemporalMappingReader(@Nonnull final Reader mappingReader) {
            return temporalMappingBuilderFragment.nativeOntopTemporalMappingReader(mappingReader);
        }

        @Override
        public B nativeOntopMappingFile(@Nonnull final String mappingFile) {
            return temporalMappingBuilderFragment.nativeOntopMappingFile(mappingFile);
        }

        @Override
        public B r2rmlMappingFile(@Nonnull final File mappingFile) {
            return temporalMappingBuilderFragment.r2rmlMappingFile(mappingFile);
        }

        @Override
        public B r2rmlMappingGraph(@Nonnull final Graph rdfGraph) {
            return temporalMappingBuilderFragment.r2rmlMappingGraph(rdfGraph);
        }

        boolean isTemporal() {
            return temporalMappingBuilderFragment.isTemporal();
        }

        boolean isR2rml(){
            return temporalMappingBuilderFragment.isR2rml();
        }

        final OntopTemporalMappingSQLAllConfigurationImpl.OntopTemporalMappingSQLAllOptions generateTemporalMappingSQLAllOptions() {
            return temporalMappingBuilderFragment.generateMappingSQLTemporalOptions(generateMappingSQLOptions());
        }

        final OntopTemporalSQLOWLAPIOptions generateTemporalSQLOWLAPIOptions() {
            return new OntopTemporalSQLOWLAPIOptions(localFragmentBuilder.ruleFile, localFragmentBuilder.ruleReader,
                    generateSQLOWLAPIOptions(), generateTemporalMappingSQLAllOptions());
        }

        @Override
        protected Properties generateProperties() {
            Properties p = super.generateProperties();
            p.putAll(temporalMappingBuilderFragment.generateProperties());
            return p;
        }

    }

    public static class BuilderImpl<B extends OntopTemporalSQLOWLAPIConfiguration.Builder<B>>
            extends OntopTemporalSQLOWLAPIBuilderMixin<B> {

        @Override
        public OntopTemporalSQLOWLAPIConfiguration build() {
            OntopTemporalMappingSQLAllSettingsImpl temporalSettings =
                    new OntopTemporalMappingSQLAllSettingsImpl(generateProperties(), isR2rml(), isTemporal());
            Properties standaloneProperties = generateProperties();
            standaloneProperties.put("it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator",
                    "it.unibz.inf.ontop.answering.reformulation.input.translation.impl.TemporalDatalogSparqlQueryTranslatorImpl");
            OntopStandaloneSQLSettingsImpl standaloneSQLSettings =
                    new OntopStandaloneSQLSettingsImpl(standaloneProperties, isR2rml());
            return new OntopTemporalSQLOWLAPIConfigurationImpl(standaloneSQLSettings,
                    temporalSettings, generateTemporalSQLOWLAPIOptions());
        }
    }

}