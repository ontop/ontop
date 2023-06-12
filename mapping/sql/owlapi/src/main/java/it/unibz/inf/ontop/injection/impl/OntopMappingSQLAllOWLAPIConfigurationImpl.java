package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllSettings;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.net.URL;
import java.util.Optional;

public class OntopMappingSQLAllOWLAPIConfigurationImpl extends OntopMappingSQLAllConfigurationImpl
        implements OntopMappingSQLAllOWLAPIConfiguration {

    private final OntopMappingOntologyConfigurationImpl mappingOWLConfiguration;

    OntopMappingSQLAllOWLAPIConfigurationImpl(OntopMappingSQLAllSettings settings,
                                              OntopMappingSQLAllOWLAPIOptions options) {
        super(settings, options.sqlOptions);
        mappingOWLConfiguration = new OntopMappingOntologyConfigurationImpl(settings, options.ontologyOptions);
    }

    @Override
    protected OBDASpecification loadOBDASpecification() throws OBDASpecificationException {
        return loadSpecification(mappingOWLConfiguration::loadOntology, mappingOWLConfiguration::loadInputFacts);
    }
    @Override
    public Optional<OWLOntology> loadInputOntology() throws OWLOntologyCreationException {
        return mappingOWLConfiguration.loadInputOntology();
    }

    @Override
    public Optional<ImmutableSet<RDFFact>> loadInputFacts() throws OBDASpecificationException {
        return mappingOWLConfiguration.loadInputFacts();
    }

    static class OntopMappingSQLAllOWLAPIOptions {

        final OntopMappingSQLAllOptions sqlOptions;
        final OntopMappingOntologyBuilders.OntopMappingOntologyOptions ontologyOptions;

        OntopMappingSQLAllOWLAPIOptions(OntopMappingSQLAllOptions sqlOptions, OntopMappingOntologyBuilders.OntopMappingOntologyOptions ontologyOptions) {
            this.sqlOptions = sqlOptions;
            this.ontologyOptions = ontologyOptions;
        }
    }

    protected static abstract class OntopMappingSQLAllOWLAPIBuilderMixin<B extends OntopMappingSQLAllOWLAPIConfiguration.Builder<B>>
            extends OntopMappingSQLAllBuilderMixin<B>
            implements OntopMappingSQLAllOWLAPIConfiguration.Builder<B> {

        private final OntopMappingOntologyBuilders.StandardMappingOntologyBuilderFragment<B> ontologyBuilderFragment;
        private boolean isOntologyDefined = false;

        OntopMappingSQLAllOWLAPIBuilderMixin() {
            ontologyBuilderFragment = new OntopMappingOntologyBuilders.StandardMappingOntologyBuilderFragment<>() {
                @Override
                protected B self() {
                    return OntopMappingSQLAllOWLAPIBuilderMixin.this.self();
                }

                @Override
                protected void declareOntologyDefined() {
                    OntopMappingSQLAllOWLAPIBuilderMixin.this.declareOntologyDefined();
                }
            };
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

        @Override
        public B factsFile(@Nonnull String urlOrPath) {
            return ontologyBuilderFragment.factsFile(urlOrPath);
        }

        @Override
        public B factFormat(@Nonnull String factFormat) {
            return ontologyBuilderFragment.factFormat(factFormat);
        }

        @Override
        public B factsBaseIRI(@Nonnull String factsBaseIRI) {
            return ontologyBuilderFragment.factsBaseIRI(factsBaseIRI);
        }

        @Override
        public B factsFile(@Nonnull URL url) {
            return ontologyBuilderFragment.factsFile(url);
        }

        @Override
        public B factsFile(@Nonnull File owlFile) {
            return ontologyBuilderFragment.factsFile(owlFile);
        }

        @Override
        public B factsReader(@Nonnull Reader reader) {
            return ontologyBuilderFragment.factsReader(reader);
        }

        protected final void declareOntologyDefined() {
            if (isOBDASpecificationAssigned())
                throw new InvalidOntopConfigurationException("The OBDA specification has already been assigned");
            if (isOntologyDefined) {
                throw new InvalidOntopConfigurationException("Ontology already defined!");
            }
            isOntologyDefined = true;
        }

        protected final OntopMappingSQLAllOWLAPIOptions generateSQLAllOWLAPIOptions() {
            OntopMappingSQLAllOptions sqlOptions = generateMappingSQLAllOptions();

            OntopMappingOntologyBuilders.OntopMappingOntologyOptions mappingOntologyOptions =
                    ontologyBuilderFragment.generateMappingOntologyOptions(
                    sqlOptions.mappingSQLOptions.mappingOptions);

            return new OntopMappingSQLAllOWLAPIOptions(sqlOptions, mappingOntologyOptions);
        }
    }

    public static class BuilderImpl extends OntopMappingSQLAllOWLAPIBuilderMixin<BuilderImpl> {

        @Override
        public OntopMappingSQLAllOWLAPIConfiguration build() {
            OntopMappingSQLAllSettings settings = new OntopMappingSQLAllSettingsImpl(generateProperties(), isR2rml());
            OntopMappingSQLAllOWLAPIOptions options = generateSQLAllOWLAPIOptions();
            return new OntopMappingSQLAllOWLAPIConfigurationImpl(settings, options);
        }

        @Override
        protected BuilderImpl self() {
            return this;
        }
    }
}
