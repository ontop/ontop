package it.unibz.inf.ontop.injection.impl;


import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.io.InvalidDataSourceException;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlapi.directmapping.DirectMappingEngine;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

public class QuestConfigurationImpl extends QuestCoreConfigurationImpl implements QuestConfiguration {

    private final QuestOptions options;
    private final QuestPreferences preferences;

    protected QuestConfigurationImpl(QuestPreferences preferences, OntopModelConfigurationOptions modelOptions,
                                     OBDAConfigurationOptions obdaOptions, QuestCoreOptions coreOptions,
                                     QuestOptions options) {
        super(preferences, modelOptions, obdaOptions, coreOptions);
        this.preferences = preferences;
        this.options = options;
    }

    @Override
    public QuestPreferences getProperties() {
        return preferences;
    }

    @Override
    public Optional<OWLOntology> loadInputOntology() throws OWLOntologyCreationException {
        if (options.ontology.isPresent()) {
            return options.ontology;
        }

        /**
         * File
         */
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        if (options.ontologyFile.isPresent()) {
            return Optional.of(manager.loadOntologyFromOntologyDocument(options.ontologyFile.get()));
        }

        /**
         * URL
         */
        try {
            Optional<URL> optionalURL = extractOntologyURL();
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

    private Optional<URL> extractOntologyURL() throws MalformedURLException {
        if (options.ontologyURL.isPresent()) {
            return options.ontologyURL;
        }
        Optional<String> optionalString = preferences.getOntologyURL();
        if (optionalString.isPresent()) {
            return Optional.of(new URL(optionalString.get()));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    protected boolean isMappingDefined() {
        return super.isMappingDefined() || options.sourceToBootstrap.isPresent();
    }

    /**
     * May bootstrap the ontology
     *
     * TODO: better handle exceptions
     */
    @Override
    protected Optional<OBDAModel> loadAlternativeMapping() throws InvalidDataSourceException {
        if (options.sourceToBootstrap.isPresent()) {

            Injector injector = getInjector();

            DirectMappingEngine dm = new DirectMappingEngine(options.bootstrappingBaseIRI.get(),
                    0,
                    injector.getInstance(NativeQueryLanguageComponentFactory.class),
                    injector.getInstance(OBDAFactoryWithException.class));
            try {
                return Optional.of(dm.extractMappings(options.sourceToBootstrap.get()));
            } catch (SQLException e) {
                throw new InvalidDataSourceException("Direct mapping boostrapping error: " + e.getMessage());
            }
        }
        else {
            return Optional.empty();
        }
    }

    public static class QuestOptions {

        private final Optional<OWLOntology> ontology;
        private final Optional<File> ontologyFile;
        private final Optional<URL> ontologyURL;
        private final Optional<OBDADataSource> sourceToBootstrap;
        private final Optional<String> bootstrappingBaseIRI;

        public QuestOptions(Optional<OWLOntology> ontology, Optional<File> ontologyFile, Optional<URL> ontologyURL,
                            Optional<OBDADataSource> sourceToBootstrap,
                            Optional<String> bootstrappingBaseIRI) {
            this.ontology = ontology;
            this.ontologyFile = ontologyFile;
            this.ontologyURL = ontologyURL;
            this.sourceToBootstrap = sourceToBootstrap;
            this.bootstrappingBaseIRI = bootstrappingBaseIRI;
        }
    }

    public static class BuilderImpl<B extends QuestConfiguration.Builder,
                                    P extends QuestPreferences,
                                    C extends QuestConfiguration>
            extends QuestCoreConfigurationImpl.BuilderImpl<B,P,C>
            implements QuestConfiguration.Builder<B> {


        private Optional<File> ontologyFile = Optional.empty();
        private Optional<OWLOntology> ontology = Optional.empty();
        private boolean isOntologyDefined = false;
        private Optional<URL> ontologyURL = Optional.empty() ;
        private Optional<OBDADataSource> sourceToBootstrap = Optional.empty();
        private Optional<String> boostrappingBaseIri = Optional.empty();

        @Override
        public B ontologyFile(@Nonnull String urlOrPath) {
            try {
                URL url = new URL(urlOrPath);
                /**
                 * If no protocol, treats it as a path
                 */
                String protocol = url.getProtocol();
                if (protocol == null) {
                    return ontologyFile(new File(urlOrPath));
                }
                else if (protocol.equals("file")) {
                    return ontologyFile(new File(url.getPath()));
                }
                else {
                    return ontologyFile(url);
                }
            } catch (MalformedURLException e) {
                return ontologyFile(new File(urlOrPath));
            }
        }

        @Override
        public B ontologyFile(@Nonnull URL url) {
                if (isOntologyDefined) {
                    throw new InvalidOntopConfigurationException("Ontology already defined!");
                }
                isOntologyDefined = true;
                this.ontologyURL = Optional.of(url);
                return (B) this;
        }


        @Override
        public B ontologyFile(@Nonnull File owlFile) {
            if (isOntologyDefined) {
                throw new InvalidOntopConfigurationException("Ontology already defined!");
            }
            isOntologyDefined = true;
            this.ontologyFile = Optional.of(owlFile);
            return (B) this;
        }

        @Override
        public B ontology(@Nonnull OWLOntology ontology) {
            if (isOntologyDefined) {
                throw new InvalidOntopConfigurationException("Ontology already defined!");
            }
            isOntologyDefined = true;
            this.ontology = Optional.of(ontology);
            return (B) this;
        }

        @Override
        public B bootstrapMapping(OBDADataSource source, String baseIRI) {
            if (isMappingDefined()) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefined();
            sourceToBootstrap = Optional.of(source);
            boostrappingBaseIri = Optional.of(baseIRI);
            return (B) this;
        }

        /**
         * TODO: explain
         * TODO: find a better term
         *
         * Can be overloaded (for extensions)
         */
        @Override
        protected Properties generateProperties() {
            Properties p = super.generateUserProperties();

            // Does not create new property entries

            return p;
        }

        /**
         * Default implementation for P == QuestPreferences
         */
        @Override
        protected P createOntopModelProperties(Properties p) {
            return (P) new QuestPreferencesImpl(p, isR2rml());
        }

        /**
         * Default implementation for P == QuestPreferences
         */
        @Override
        protected C createConfiguration(P questPreferences) {
            return (C) new QuestConfigurationImpl(questPreferences, generateOntopModelConfigurationOptions(),
                    createOBDAConfigurationOptions(), createQuestCoreOptions(), createQuestArguments());
        }

        protected final QuestOptions createQuestArguments() {
            return new QuestOptions(ontology, ontologyFile, ontologyURL, sourceToBootstrap, boostrappingBaseIri);
        }

    }
}
