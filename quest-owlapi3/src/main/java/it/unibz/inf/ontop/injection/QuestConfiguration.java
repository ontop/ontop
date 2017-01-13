package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.QuestConfigurationImpl;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URL;
import java.util.Optional;

/**
 *
 * TODO: explain
 *
 */
public interface QuestConfiguration extends QuestCoreConfiguration {

    @Override
    QuestSettings getSettings();

    Optional<OWLOntology> loadInputOntology() throws OWLOntologyCreationException;

    /**
     * Only call it if you are sure that an ontology has been provided
     */
    default OWLOntology loadProvidedInputOntology() throws OWLOntologyCreationException {
        return loadInputOntology()
                .orElseThrow(() -> new IllegalStateException("No ontology has been provided. " +
                        "Do not call this method unless you are sure of the ontology provision."));
    }

    static Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder>>>>>>>>>>>> defaultBuilder() {
        return new QuestConfigurationImpl.BuilderImpl<>();
    }

    interface QuestConfigurationBuilderFragment<B extends Builder> {

        B ontologyFile(@Nonnull String urlOrPath);

        B ontologyFile(@Nonnull URL url);

        B ontologyFile(@Nonnull File owlFile);

        B ontology(@Nonnull OWLOntology ontology);
    }

    interface Builder<B extends Builder> extends QuestConfigurationBuilderFragment<B>,
            QuestCoreConfiguration.Builder<B> {

        @Override
        QuestConfiguration build();
    }
}
