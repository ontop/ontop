package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.injection.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.injection.impl.QuestCoreConfigurationImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Optional;
import java.util.Properties;

public class QuestConfigurationImpl extends QuestCoreConfigurationImpl implements QuestConfiguration {

    private final QuestOptions options;
    private final QuestPreferences preferences;

    protected QuestConfigurationImpl(QuestPreferences preferences, OBDAConfigurationOptions obdaOptions,
                                     QuestCoreOptions coreOptions, QuestOptions options) {
        super(preferences, obdaOptions, coreOptions);
        this.preferences = preferences;
        this.options = options;
    }

    @Override
    public QuestPreferences getPreferences() {
        return preferences;
    }

    @Override
    public Optional<OWLOntology> loadInputOntology() throws OWLOntologyCreationException {
        if (options.ontology.isPresent()) {
            return options.ontology;
        }

        Optional<File> optionalFile = options.ontologyFile
                .map(Optional::of)
                .orElseGet(() -> preferences.getOntologyFilePath()
                        .map(File::new));

        if (optionalFile.isPresent()) {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(optionalFile.get());
            return Optional.of(ontology);
        }
        else {
            return Optional.empty();
        }

    }

    public static class QuestOptions {

        private final Optional<OWLOntology> ontology;
        private final Optional<File> ontologyFile;

        public QuestOptions(Optional<OWLOntology> ontology, Optional<File> ontologyFile) {
            this.ontology = ontology;
            this.ontologyFile = ontologyFile;
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

        @Override
        public B ontologyFile(@Nonnull String owlFilename) {
            if (isOntologyDefined) {
                throw new IllegalArgumentException("Ontology already defined!");
            }
            isOntologyDefined = true;
            this.ontologyFile = Optional.of(new File(owlFilename));
            return (B) this;
        }

        @Override
        public B ontologyFile(@Nonnull File owlFile) {
            if (isOntologyDefined) {
                throw new IllegalArgumentException("Ontology already defined!");
            }
            isOntologyDefined = true;
            this.ontologyFile = Optional.of(owlFile);
            return (B) this;
        }

        @Override
        public B ontology(@Nonnull OWLOntology ontology) {
            if (isOntologyDefined) {
                throw new IllegalArgumentException("Ontology already defined!");
            }
            isOntologyDefined = true;
            this.ontology = Optional.of(ontology);
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
            Properties p = super.generateProperties();

            // Does not create new property entries

            return p;
        }

        /**
         * Default implementation for P == QuestPreferences
         */
        @Override
        protected P createOBDAProperties(Properties p) {
            return (P) new QuestPreferencesImpl(p, isR2rml());
        }

        /**
         * Default implementation for P == QuestPreferences
         */
        @Override
        protected C createConfiguration(P questPreferences) {
            return (C) new QuestConfigurationImpl(questPreferences, createOBDAConfigurationArguments(),
                    createQuestCoreArguments(), createQuestArguments());
        }

        protected final QuestOptions createQuestArguments() {
            return new QuestOptions(ontology, ontologyFile);
        }

    }
}
