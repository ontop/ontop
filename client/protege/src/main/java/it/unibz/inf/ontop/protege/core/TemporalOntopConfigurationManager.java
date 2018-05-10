package it.unibz.inf.ontop.protege.core;

import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.serializer.OntopNativeTemporalMappingSerializer;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
            new OntopNativeTemporalMappingSerializer(tobdaModel.generatePPMapping()).save(temporaryTemporalFile);

            //printOutMappingFile(temporaryTemporalFile);

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

    private void printOutMappingFile(File temporaryTemporalFile){
        FileReader fileReader;
        try {
            fileReader = new FileReader(temporaryTemporalFile);
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            String line;
            System.out.println("temporaryTemporalFile:\n");
            while((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
