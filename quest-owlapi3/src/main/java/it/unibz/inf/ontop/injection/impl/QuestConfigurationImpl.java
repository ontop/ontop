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
import java.util.function.Supplier;

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
        Optional<String> optionalString = settings.getOntologyURL();
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
        public final QuestCoreOptions coreOptions;

        public QuestOptions(Optional<OWLOntology> ontology, Optional<File> ontologyFile, Optional<URL> ontologyURL,
                            Optional<OBDADataSource> sourceToBootstrap,
                            Optional<String> bootstrappingBaseIRI,
                            QuestCoreOptions coreOptions) {
            this.ontology = ontology;
            this.ontologyFile = ontologyFile;
            this.ontologyURL = ontologyURL;
            this.sourceToBootstrap = sourceToBootstrap;
            this.bootstrappingBaseIRI = bootstrappingBaseIRI;
            this.coreOptions = coreOptions;
        }
    }

    protected static class DefaultQuestConfigurationBuilderFragment<B extends QuestConfiguration.Builder>
        implements QuestConfigurationBuilderFragment<B> {

        private final B builder;
        private final Runnable mappingDefinitionCB;
        private final Supplier<Boolean> isMappingDefinedCB;

        private Optional<File> ontologyFile = Optional.empty();
        private Optional<OWLOntology> ontology = Optional.empty();
        private boolean isOntologyDefined = false;
        private Optional<URL> ontologyURL = Optional.empty() ;
        private Optional<OBDADataSource> sourceToBootstrap = Optional.empty();
        private Optional<String> boostrappingBaseIri = Optional.empty();

        protected DefaultQuestConfigurationBuilderFragment(B builder, Runnable mappingDefinitionCB,
                                                           Supplier<Boolean> isMappingDefinedCB) {
            this.builder = builder;
            this.mappingDefinitionCB = mappingDefinitionCB;
            this.isMappingDefinedCB = isMappingDefinedCB;
        }

        /**
         * For sub-classes ONLY!
         */
        protected DefaultQuestConfigurationBuilderFragment(Runnable mappingDefinitionCB,
                                                           Supplier<Boolean> isMappingDefinedCB) {
            this.builder = (B) this;
            this.mappingDefinitionCB = mappingDefinitionCB;
            this.isMappingDefinedCB = isMappingDefinedCB;
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

        @Override
        public B bootstrapMapping(OBDADataSource source, String baseIRI) {
            if (isMappingDefinedCB.get()) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            mappingDefinitionCB.run();
            sourceToBootstrap = Optional.of(source);
            boostrappingBaseIri = Optional.of(baseIRI);
            return builder;
        }

        protected final QuestOptions generateQuestOptions(QuestCoreOptions coreOptions) {
            return new QuestOptions(ontology, ontologyFile, ontologyURL, sourceToBootstrap, boostrappingBaseIri, coreOptions);
        }

        protected Properties generateProperties() {
            Properties p = new Properties();
            // Does not create new property entries
            return p;
        }
    }

    protected abstract static class QuestConfigurationBuilderMixin<B extends QuestConfiguration.Builder>
        extends QuestCoreConfigurationBuilderMixin<B>
        implements QuestConfiguration.Builder<B> {

        private final DefaultQuestConfigurationBuilderFragment<B> questBuilderFragment;

        protected QuestConfigurationBuilderMixin() {
            questBuilderFragment = new DefaultQuestConfigurationBuilderFragment<>((B) this,
                    this::declareMappingDefined,
                    this::isMappingDefined);
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
        public B bootstrapMapping(OBDADataSource source, String baseIRI) {
            return questBuilderFragment.bootstrapMapping(source, baseIRI);
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

    public static final class BuilderImpl<B extends QuestConfiguration.Builder>
            extends QuestConfigurationBuilderMixin<B> {

        @Override
        public QuestConfiguration build() {
            Properties properties = generateProperties();
            QuestSettings settings = new QuestSettingsImpl(properties, isR2rml());

            return new QuestConfigurationImpl(settings, generateQuestOptions());
        }
    }
}
