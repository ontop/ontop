package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.OntopMappingOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.impl.OntopMappingOntologyBuilders.OntopMappingOntologyOptions;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

public class OntopMappingOWLAPIConfigurationImpl extends OntopMappingConfigurationImpl
        implements OntopMappingOWLAPIConfiguration {

    private final OntopMappingOWLAPIOptions options;

    protected OntopMappingOWLAPIConfigurationImpl(OntopMappingSettings settings, OntopMappingOWLAPIOptions options) {
        super(settings, options.mappingOntologyOptions.mappingOptions);
        this.options = options;
    }

    /**
     * TODO: cache the ontology
     */
    @Override
    public Optional<OWLOntology> loadInputOntology() throws OWLOntologyCreationException {
        if (options.ontology.isPresent()) {
            return options.ontology;
        }

        /**
         * File
         */
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        if (options.mappingOntologyOptions.ontologyFile.isPresent()) {
            return Optional.of(manager.loadOntologyFromOntologyDocument(options.mappingOntologyOptions.ontologyFile.get()));
        }

        /**
         * URL
         */
        try {
            Optional<URL> optionalURL = options.mappingOntologyOptions.ontologyURL;
            if (optionalURL.isPresent()) {
                return Optional.of(
                        manager.loadOntologyFromOntologyDocument(
                                optionalURL.get().openStream()));
            }

        } catch (MalformedURLException e ) {
            throw new OWLOntologyCreationException("Invalid URI: " + e.getMessage());
        } catch (IOException e) {
            throw new OWLOntologyCreationException(e.getMessage());
        }

        return Optional.empty();
    }

    Optional<Ontology> loadOntology() throws OntologyException {
        try {
            return loadInputOntology()
                    .map(o -> OWLAPITranslatorUtility.translateImportsClosure(o));
        } catch (OWLOntologyCreationException e) {
            throw new OntologyException(e.getMessage());
        }
    }

    static class OntopMappingOWLAPIOptions {

        private final Optional<OWLOntology> ontology;
        final OntopMappingOntologyOptions mappingOntologyOptions;

        OntopMappingOWLAPIOptions(Optional<OWLOntology> ontology, OntopMappingOntologyOptions mappingOntologyOptions) {
            this.ontology = ontology;
            this.mappingOntologyOptions = mappingOntologyOptions;
        }
    }

    protected static class StandardMappingOWLAPIBuilderFragment<B extends OntopMappingOWLAPIConfiguration.Builder<B>>
            implements OntopMappingOWLAPIBuilderFragment<B> {

        private final B builder;
        private final Runnable declareOntologyDefinedCB;
        private Optional<OWLOntology> ontology = Optional.empty();

        StandardMappingOWLAPIBuilderFragment(B builder, Runnable declareOntologyDefinedCB) {
            this.builder = builder;
            this.declareOntologyDefinedCB = declareOntologyDefinedCB;
        }

        @Override
        public B ontology(@Nonnull OWLOntology ontology) {
            declareOntologyDefinedCB.run();
            this.ontology = Optional.of(ontology);
            return builder;
        }

        protected final OntopMappingOWLAPIOptions generateOntologyOWLAPIOptions(
                OntopMappingOntologyOptions mappingOntologyOptions) {
            return new OntopMappingOWLAPIOptions(ontology, mappingOntologyOptions);
        }

        protected Properties generateProperties() {
            Properties p = new Properties();
            // Does not create new property entries
            return p;
        }
    }
}
