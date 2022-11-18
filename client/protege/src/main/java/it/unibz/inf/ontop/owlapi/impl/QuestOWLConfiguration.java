package it.unibz.inf.ontop.owlapi.impl;


import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import org.semanticweb.owlapi.reasoner.NullReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import javax.annotation.Nonnull;

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
