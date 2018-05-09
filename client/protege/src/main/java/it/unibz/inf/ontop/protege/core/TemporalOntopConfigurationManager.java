package it.unibz.inf.ontop.protege.core;

import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class TemporalOntopConfigurationManager extends OntopConfigurationManager {
    private final TemporalOBDAModel tobdaModel;

    TemporalOntopConfigurationManager(@Nonnull OBDAModel obdaModel, @Nonnull TemporalOBDAModel tobdaModel, @Nonnull DisposableProperties internalSettings) {
        super(obdaModel, internalSettings);
        this.tobdaModel = tobdaModel;
    }

    @Override
    public OntopTemporalSQLOWLAPIConfiguration buildOntopSQLOWLAPIConfiguration(OWLOntology currentOntology) {
        File temporaryTemporalFile = null;
        try {
            //TODO change to temporal ppMapping passing to the configuration
            temporaryTemporalFile = File.createTempFile("temporal-mapping-for-configuration", ".tobda");
            new OntopNativeMappingSerializer(tobdaModel.generatePPMapping()).save(temporaryTemporalFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        OntopTemporalSQLOWLAPIConfiguration.Builder builder = OntopTemporalSQLOWLAPIConfiguration.defaultBuilder()
                .enableTemporalMode()
                .properties(snapshotProperties())
                .ppMapping(obdaModel.generatePPMapping())
                .nativeOntopTemporalMappingFile(temporaryTemporalFile)
                .nativeOntopTemporalRuleProgram(tobdaModel.getDatalogMTLProgram());
        Optional.ofNullable(implicitDBConstraintFile)
                .ifPresent(builder::basicImplicitConstraintFile);
        builder.ontology(currentOntology);
        return builder.build();
    }
}
