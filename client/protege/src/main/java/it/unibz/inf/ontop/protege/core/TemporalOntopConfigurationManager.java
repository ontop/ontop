package it.unibz.inf.ontop.protege.core;


import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * TODO: find a better name
 */
public class TemporalOntopConfigurationManager extends OntopConfigurationManager {

    TemporalOntopConfigurationManager(@Nonnull TemporalOBDAModel obdaModel, @Nonnull DisposableProperties internalSettings) {
        super(obdaModel, internalSettings);
    }

    @Override
    public OntopTemporalSQLOWLAPIConfiguration buildOntopSQLOWLAPIConfiguration(OWLOntology currentOntology) {
        OntopTemporalSQLOWLAPIConfiguration.Builder builder = OntopTemporalSQLOWLAPIConfiguration.defaultBuilder()
                .enableTemporalMode()
                .properties(snapshotProperties())
                .ppMapping(obdaModel.generatePPMapping());
        Optional.ofNullable(implicitDBConstraintFile)
                .ifPresent(builder::basicImplicitConstraintFile);
        builder.ontology(currentOntology);
        return builder.build();
    }
}
