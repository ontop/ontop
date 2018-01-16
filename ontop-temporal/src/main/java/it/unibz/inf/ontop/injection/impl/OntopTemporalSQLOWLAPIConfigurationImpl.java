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
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class OntopTemporalSQLOWLAPIConfigurationImpl
        extends OntopSQLOWLAPIConfigurationImpl
        implements OntopTemporalSQLOWLAPIConfiguration {

    public final OntopTemporalMappingSQLAllConfigurationImpl temporalConfiguration;
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
                Stream.of(new OntopTemporalModule(this)))
        );
    }

    @Override
    public OBDASpecification loadOBDASpecification() throws OBDASpecificationException {
        return temporalConfiguration.loadSpecification(()->mappingOWLConfiguration.loadOntology());
    }

    @Override
    public Optional<OWLOntology> loadInputOntology() throws OWLOntologyCreationException {
        return mappingOWLConfiguration.loadInputOntology();
    }

    static class OntopTemporalSQLOWLAPIOptions {
        final OntopSQLOWLAPIConfigurationImpl.OntopSQLOWLAPIOptions owlOptions;
        final OntopTemporalMappingSQLAllConfigurationImpl.OntopTemporalMappingSQLAllOptions temporalOptions;

        OntopTemporalSQLOWLAPIOptions(OntopSQLOWLAPIConfigurationImpl.OntopSQLOWLAPIOptions owlOptions, OntopTemporalMappingSQLAllConfigurationImpl.OntopTemporalMappingSQLAllOptions temporalOptions) {
            this.owlOptions = owlOptions;
            this.temporalOptions = temporalOptions;
        }
    }

    static abstract class OntopTemporalSQLOWLAPIBuilderMixin<B extends OntopTemporalSQLOWLAPIConfiguration.Builder<B>>
            extends OntopSQLOWLAPIBuilderMixin<B>
            implements OntopTemporalSQLOWLAPIConfiguration.Builder<B> {

        private final OntopTemporalMappingSQLAllConfigurationImpl.StandardTemporalMappingSQLBuilderFragment<B> temporalMappingBuilderFragment;

        OntopTemporalSQLOWLAPIBuilderMixin() {
            B builder = (B) this;
            temporalMappingBuilderFragment = new OntopTemporalMappingSQLAllConfigurationImpl.StandardTemporalMappingSQLBuilderFragment<>(builder,
                    this::declareMappingDefined, this::declareImplicitConstraintSetDefined);

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

        final OntopTemporalMappingSQLAllConfigurationImpl.OntopTemporalMappingSQLAllOptions generateTemporalMappingSQLAllOptions() {
            return temporalMappingBuilderFragment.generateMappingSQLTemporalOptions(generateMappingSQLOptions());
        }

        final OntopTemporalMappingSQLAllSettings getTemporalSettings(){
            return new OntopTemporalMappingSQLAllSettingsImpl(generateProperties(), isR2rml(), isTemporal());
        }

        final OntopTemporalSQLOWLAPIOptions generateTemporalSQLOWLAPIOptions() {
            return new OntopTemporalSQLOWLAPIOptions(generateSQLOWLAPIOptions(), generateTemporalMappingSQLAllOptions());
        }

    }

    public static class BuilderImpl<B extends OntopTemporalSQLOWLAPIConfiguration.Builder<B>>
            extends OntopTemporalSQLOWLAPIBuilderMixin<B> {

        @Override
        public OntopTemporalSQLOWLAPIConfiguration build() {

            return new OntopTemporalSQLOWLAPIConfigurationImpl(
                    new OntopStandaloneSQLSettingsImpl(generateProperties(), isR2rml()),
                    getTemporalSettings(),
                    generateTemporalSQLOWLAPIOptions());
        }
    }

}