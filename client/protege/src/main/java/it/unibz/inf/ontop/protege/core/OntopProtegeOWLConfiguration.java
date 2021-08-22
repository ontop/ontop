package it.unibz.inf.ontop.protege.core;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import javax.annotation.Nonnull;

public class OntopProtegeOWLConfiguration extends SimpleConfiguration {

    private final OBDAModelManager obdaModelManager;

    public OntopProtegeOWLConfiguration(@Nonnull OBDAModelManager obdaModelManager,
                                        @Nonnull ReasonerProgressMonitor progressMonitor) {
        super(progressMonitor);
        this.obdaModelManager = obdaModelManager;
    }

    @Nonnull
    public OntopSQLOWLAPIConfiguration getOntopConfiguration(OWLOntology rootOntology) {
        return obdaModelManager.getOBDAModel(rootOntology).getOntopConfiguration();
    }
}
