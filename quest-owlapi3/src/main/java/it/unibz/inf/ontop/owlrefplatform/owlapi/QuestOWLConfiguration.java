package it.unibz.inf.ontop.owlrefplatform.owlapi;


import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemOWLAPIConfiguration;
import org.semanticweb.owlapi.reasoner.NullReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import javax.annotation.Nonnull;

/**
 * See QuestPreferences.builder() for an high-level configuration builder.
 */
@SuppressWarnings("serial")
public class QuestOWLConfiguration extends SimpleConfiguration {

    private final OntopSystemOWLAPIConfiguration ontopConfiguration;

    @Nonnull
    public OntopSystemConfiguration getOntopConfiguration() {
        return ontopConfiguration;
    }

    QuestOWLConfiguration(@Nonnull OntopSystemOWLAPIConfiguration ontopConfiguration) {
        super(new NullReasonerProgressMonitor());
        this.ontopConfiguration = ontopConfiguration;
    }

    /**
     * Constructor
     */
    public QuestOWLConfiguration(@Nonnull OntopSystemOWLAPIConfiguration ontopConfiguration,
                                 @Nonnull ReasonerProgressMonitor progressMonitor) {
        super(progressMonitor);
        this.ontopConfiguration = ontopConfiguration;
    }
}
