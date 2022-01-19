package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllSettings;
import it.unibz.inf.ontop.injection.impl.OntopMappingOWLAPIConfigurationImpl.OntopMappingOWLAPIOptions;
import it.unibz.inf.ontop.spec.OBDASpecification;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.net.URL;
import java.util.Optional;

public class OntopMappingSQLAllOWLAPIConfigurationImpl extends OntopMappingSQLAllConfigurationImpl
        implements OntopMappingSQLAllOWLAPIConfiguration {

    private final OntopMappingOWLAPIConfigurationImpl mappingOWLConfiguration;

    OntopMappingSQLAllOWLAPIConfigurationImpl(OntopMappingSQLAllSettings settings,
                                              OntopMappingSQLAllOWLAPIOptions options) {
        super(settings, options.sqlOptions);
        mappingOWLConfiguration = new OntopMappingOWLAPIConfigurationImpl(settings, options.owlapiOptions);
    }

    @Override
    protected OBDASpecification loadOBDASpecification() throws OBDASpecificationException {
        return loadSpecification(mappingOWLConfiguration::loadOntology);
    }
    @Override
    public Optional<OWLOntology> loadInputOntology() throws OWLOntologyCreationException {
        return mappingOWLConfiguration.loadInputOntology();
    }

    static class OntopMappingSQLAllOWLAPIOptions {

        final OntopMappingSQLAllOptions sqlOptions;
        final OntopMappingOWLAPIOptions owlapiOptions;

        OntopMappingSQLAllOWLAPIOptions(OntopMappingSQLAllOptions sqlOptions, OntopMappingOWLAPIOptions owlapiOptions) {
            this.sqlOptions = sqlOptions;
            this.owlapiOptions = owlapiOptions;
        }
    }

    static abstract class OntopMappingSQLAllOWLAPIBuilderMixin<B extends OntopMappingSQLAllOWLAPIConfiguration.Builder<B>>
            extends OntopMappingSQLAllBuilderMixin<B>
            implements OntopMappingSQLAllOWLAPIConfiguration.Builder<B> {

        private final OntopMappingOWLAPIConfigurationImpl.StandardMappingOWLAPIBuilderFragment<B> owlBuilderFragment;
        private final OntopMappingOntologyBuilders.StandardMappingOntologyBuilderFragment<B> ontologyBuilderFragment;
        private boolean isOntologyDefined = false;

        OntopMappingSQLAllOWLAPIBuilderMixin() {
            B builder = (B) this;
            owlBuilderFragment = new OntopMappingOWLAPIConfigurationImpl.StandardMappingOWLAPIBuilderFragment<>(builder,
                    this::declareOntologyDefined
            );
            ontologyBuilderFragment = new OntopMappingOntologyBuilders.StandardMappingOntologyBuilderFragment<>(builder,
                    this::declareOntologyDefined
            );
        }

        @Override
        public B ontology(@Nonnull OWLOntology ontology) {
            return owlBuilderFragment.ontology(ontology);
        }

        @Override
        public B ontologyFile(@Nonnull String urlOrPath) {
            return ontologyBuilderFragment.ontologyFile(urlOrPath);
        }

        @Override
        public B ontologyFile(@Nonnull URL url) {
            return ontologyBuilderFragment.ontologyFile(url);
        }

        @Override
        public B ontologyFile(@Nonnull File owlFile) {
            return ontologyBuilderFragment.ontologyFile(owlFile);
        }

        @Override
        public B ontologyReader(@Nonnull Reader reader) {
            return ontologyBuilderFragment.ontologyReader(reader);
        }

        @Override
        public B xmlCatalogFile(@Nonnull String file) {
            return ontologyBuilderFragment.xmlCatalogFile(file);
        }

        void declareOntologyDefined() {
            if (isOBDASpecificationAssigned())
                throw new InvalidOntopConfigurationException("The OBDA specification has already been assigned");
            if (isOntologyDefined) {
                throw new InvalidOntopConfigurationException("Ontology already defined!");
            }
            isOntologyDefined = true;
        }

        final OntopMappingSQLAllOWLAPIOptions generateSQLAllOWLAPIOptions() {
            OntopMappingSQLAllOptions sqlOptions = generateMappingSQLAllOptions();

            OntopMappingOntologyBuilders.OntopMappingOntologyOptions mappingOntologyOptions =
                    ontologyBuilderFragment.generateMappingOntologyOptions(
                    sqlOptions.mappingSQLOptions.mappingOptions);

            OntopMappingOWLAPIOptions owlOptions = owlBuilderFragment.generateOntologyOWLAPIOptions(mappingOntologyOptions);
            return new OntopMappingSQLAllOWLAPIOptions(sqlOptions, owlOptions);
        }
    }

    public static class BuilderImpl<B extends OntopMappingSQLAllOWLAPIConfiguration.Builder<B>>
            extends OntopMappingSQLAllOWLAPIBuilderMixin<B> {

        @Override
        public OntopMappingSQLAllOWLAPIConfiguration build() {
            OntopMappingSQLAllSettings settings = new OntopMappingSQLAllSettingsImpl(generateProperties(), isR2rml());
            OntopMappingSQLAllOWLAPIOptions options = generateSQLAllOWLAPIOptions();
            return new OntopMappingSQLAllOWLAPIConfigurationImpl(settings, options);
        }
    }

}
