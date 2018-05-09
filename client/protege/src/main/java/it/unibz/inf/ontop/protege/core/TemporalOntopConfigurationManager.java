package it.unibz.inf.ontop.protege.core;

import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.serializer.OntopNativeTemporalMappingSerializer;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import java.io.*;
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

            printOutMappingFile(temporaryTemporalFile);

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
        // FileReader reads text files in the default encoding.
        FileReader fileReader =
                null;
        try {
            fileReader = new FileReader(temporaryTemporalFile);
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            String line = null;
            System.out.println("temporaryTemporalFile:\n");
            while((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }

            // Always close files.
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
