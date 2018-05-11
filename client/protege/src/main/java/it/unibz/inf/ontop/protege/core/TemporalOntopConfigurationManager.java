package it.unibz.inf.ontop.protege.core;

import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.serializer.OntopNativeTemporalMappingSerializer;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class TemporalOntopConfigurationManager extends OntopConfigurationManager {
    private OBDAModel nativeObdaModel;

    TemporalOntopConfigurationManager(@Nonnull TemporalOBDAModel tobdaModel, @Nonnull OBDAModel obdaModel, @Nonnull DisposableProperties internalSettings) {
        super(tobdaModel, internalSettings);
        //TODO find a better way to get native mappings to the configuration
        this.nativeObdaModel = obdaModel;
    }

    @Override
    public OntopTemporalSQLOWLAPIConfiguration buildOntopSQLOWLAPIConfiguration(OWLOntology currentOntology) {
        File temporaryTemporalFile = null;
        try {
            //TODO change to temporal ppMapping passing to the configuration
            temporaryTemporalFile = File.createTempFile("temporal-mapping-for-configuration", ".tobda");
            new OntopNativeTemporalMappingSerializer(obdaModel.generatePPMapping()).save(temporaryTemporalFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        OntopTemporalSQLOWLAPIConfiguration.Builder builder = OntopTemporalSQLOWLAPIConfiguration.defaultBuilder()
                .enableTemporalMode()
                .properties(snapshotProperties())
                .ppMapping(nativeObdaModel.generatePPMapping())
                .nativeOntopTemporalMappingFile(temporaryTemporalFile)
                .nativeOntopTemporalRuleProgram(((TemporalOBDAModel) obdaModel).getDatalogMTLProgram());
        Optional.ofNullable(implicitDBConstraintFile)
                .ifPresent(builder::basicImplicitConstraintFile);
        builder.ontology(currentOntology);
        return builder.build();
    }
}
