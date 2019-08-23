package it.unibz.inf.ontop.protege.core;


import it.unibz.inf.ontop.injection.OntopSystemOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.impl.QuestOWLConfiguration;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
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

    OWLAPITranslatorOWL2QL getOWLAPITranslator() {
        return getOntopConfiguration().getInjector().getInstance(OWLAPITranslatorOWL2QL.class);
    }
}
