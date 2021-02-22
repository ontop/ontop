package it.unibz.inf.ontop.protege.core;


import it.unibz.inf.ontop.injection.OntopSystemOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.impl.QuestOWLConfiguration;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

import javax.annotation.Nonnull;

@SuppressWarnings("serial")
public class OntopProtegeOWLConfiguration extends QuestOWLConfiguration {


    private final OntopConfigurationManager ontopConfigurationManager;

    @Nonnull
    public OntopConfigurationManager getOntopConfigurationManager() {
        return ontopConfigurationManager;
    }


    public OntopProtegeOWLConfiguration(@Nonnull OntopSystemOWLAPIConfiguration ontopConfiguration,
                                        @Nonnull ReasonerProgressMonitor progressMonitor,
                                        @Nonnull OntopConfigurationManager configurationGenerator) {
        super(ontopConfiguration, progressMonitor);
        this.ontopConfigurationManager = configurationGenerator;
    }
}
