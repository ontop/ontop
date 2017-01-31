package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.Ontology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

public class QuestConfigurationImpl extends QuestCoreConfigurationImpl implements QuestConfiguration {

    private final QuestOptions options;
    private final QuestSettings settings;

    protected QuestConfigurationImpl(QuestSettings settings, QuestOptions options) {
        super(settings, options.coreOptions);
        this.settings = settings;
        this.options = options;
    }

    @Override
    public QuestSettings getSettings() {
        return settings;
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

        if (options.ontologyFile.isPresent()) {
            return Optional.of(manager.loadOntologyFromOntologyDocument(options.ontologyFile.get()));
        }

        /**
         * URL
         */
        try {
            Optional<URL> optionalURL = options.ontologyURL;
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

    @Override
    public Optional<OBDASpecification> loadSpecification() throws IOException, OBDASpecificationException {
        return loadSpecification(this::loadOntology);
    }

    @Override
    public Optional<OBDAModel> loadPPMapping() throws IOException, InvalidMappingException, DuplicateMappingException {
        return loadPPMapping(this::loadOntology);
    }

    private Optional<Ontology> loadOntology() throws OntologyException {
        try {
            return loadInputOntology()
                    .map(o -> OWLAPITranslatorUtility.translateImportsClosure(o));
        } catch (OWLOntologyCreationException e) {
            throw new OntologyException(e.getMessage());
        }
    }

    public static class QuestOptions {

        private final Optional<OWLOntology> ontology;
        private final Optional<File> ontologyFile;
        private final Optional<URL> ontologyURL;
        public final QuestCoreOptions coreOptions;

        public QuestOptions(Optional<OWLOntology> ontology, Optional<File> ontologyFile, Optional<URL> ontologyURL,
                            QuestCoreOptions coreOptions) {
            this.ontology = ontology;
            this.ontologyFile = ontologyFile;
            this.ontologyURL = ontologyURL;
            this.coreOptions = coreOptions;
        }
    }

    protected static class DefaultQuestConfigurationBuilderFragment<B extends QuestConfiguration.Builder<B>>
        implements QuestConfigurationBuilderFragment<B> {

        private final B builder;

        private Optional<File> ontologyFile = Optional.empty();
        private Optional<OWLOntology> ontology = Optional.empty();
        private boolean isOntologyDefined = false;
        private Optional<URL> ontologyURL = Optional.empty() ;

        protected DefaultQuestConfigurationBuilderFragment(B builder) {
            this.builder = builder;
        }

        /**
         * For sub-classes ONLY!
         */
        protected DefaultQuestConfigurationBuilderFragment() {
            this.builder = (B) this;
        }

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
            return builder;
        }


        @Override
        public B ontologyFile(@Nonnull File owlFile) {
            if (isOntologyDefined) {
                throw new InvalidOntopConfigurationException("Ontology already defined!");
            }
            isOntologyDefined = true;
            this.ontologyFile = Optional.of(owlFile);
            return builder;
        }

        @Override
        public B ontology(@Nonnull OWLOntology ontology) {
            if (isOntologyDefined) {
                throw new InvalidOntopConfigurationException("Ontology already defined!");
            }
            isOntologyDefined = true;
            this.ontology = Optional.of(ontology);
            return builder;
        }

        protected final QuestOptions generateQuestOptions(QuestCoreOptions coreOptions) {
            return new QuestOptions(ontology, ontologyFile, ontologyURL, coreOptions);
        }

        protected Properties generateProperties() {
            Properties p = new Properties();
            // Does not create new property entries
            return p;
        }
    }

    protected abstract static class QuestConfigurationBuilderMixin<B extends QuestConfiguration.Builder<B>>
        extends QuestCoreConfigurationBuilderMixin<B>
        implements QuestConfiguration.Builder<B> {

        private final DefaultQuestConfigurationBuilderFragment<B> questBuilderFragment;

        protected QuestConfigurationBuilderMixin() {
            questBuilderFragment = new DefaultQuestConfigurationBuilderFragment<>((B) this);
        }

        @Override
        public B ontologyFile(@Nonnull String urlOrPath) {
            return questBuilderFragment.ontologyFile(urlOrPath);
        }

        @Override
        public B ontologyFile(@Nonnull URL url) {
            return questBuilderFragment.ontologyFile(url);
        }

        @Override
        public B ontologyFile(@Nonnull File owlFile) {
            return questBuilderFragment.ontologyFile(owlFile);
        }

        @Override
        public B ontology(@Nonnull OWLOntology ontology) {
            return questBuilderFragment.ontology(ontology);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(questBuilderFragment.generateProperties());
            return properties;
        }

        protected final QuestOptions generateQuestOptions() {
            return questBuilderFragment.generateQuestOptions(generateQuestCoreOptions());
        }
    }

    public static final class BuilderImpl<B extends QuestConfiguration.Builder<B>>
            extends QuestConfigurationBuilderMixin<B> {

        @Override
        public QuestConfiguration build() {
            Properties properties = generateProperties();
            QuestSettings settings = new QuestSettingsImpl(properties, isR2rml());

            return new QuestConfigurationImpl(settings, generateQuestOptions());
        }
    }
}
