package it.unibz.inf.ontop.owlapi.impl;


import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemOWLAPIConfiguration;
import org.semanticweb.owlapi.reasoner.NullReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import javax.annotation.Nonnull;

/**
 * See QuestPreferences.builder() for an high-level configuration builder.
 */
public class QuestOWLConfiguration extends SimpleConfiguration {

    private final OntopSystemConfiguration ontopConfiguration;

    @Nonnull
    public OntopSystemConfiguration getOntopConfiguration() {
        return ontopConfiguration;
    }

    QuestOWLConfiguration(@Nonnull OntopSystemConfiguration ontopConfiguration) {
        super(new NullReasonerProgressMonitor());
        this.ontopConfiguration = ontopConfiguration;
    }
}
