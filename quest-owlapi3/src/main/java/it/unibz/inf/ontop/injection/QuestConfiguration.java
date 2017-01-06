package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.QuestConfigurationImpl;
import it.unibz.inf.ontop.model.OBDADataSource;
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
    QuestPreferences getProperties();

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

    interface Builder<B extends Builder> extends QuestCoreConfiguration.Builder<B> {

        B ontologyFile(@Nonnull String urlOrPath);

        B ontologyFile(@Nonnull URL url);

        B ontologyFile(@Nonnull File owlFile);

        B ontology(@Nonnull OWLOntology ontology);

        /**
         * Cannot be used together with a pre-defined mapping
         */
        B bootstrapMapping(OBDADataSource source, String baseIRI);

        QuestConfiguration build();
    }
}
