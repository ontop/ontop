package it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot.BootLabelsAndAliasesTest;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;

import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface MPBootTestsHelper {

    enum Method {MPBOOT, DIRECT};

    static Bootstrapper.BootstrappingResults bootstrapMapping(OntopSQLOWLAPIConfiguration initialConfiguration,
                                                              BootConf bootConf, String base_iri, Method method)

            throws OWLOntologyCreationException, MappingException, MappingBootstrappingException {

        Bootstrapper bootstrapper = (method == Method.DIRECT) ? Bootstrapper.defaultBootstrapper() : Bootstrapper.mpBootstrapper();

        // The bootstrappped mappings are appended to those already present in "initialConfiguration"
        return bootstrapper.bootstrap(initialConfiguration, base_iri, bootConf);
    }

    static OntopSQLOWLAPIConfiguration configure(String propertyPath, String owlPath, String obdaPath) {

        String propertyFilePath = BootLabelsAndAliasesTest.class.getResource(propertyPath).getPath();

        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlPath)
                .nativeOntopMappingFile(obdaPath)
                .propertyFile(propertyFilePath)
                .enableTestMode()
                .build();

        return configuration;
    }

    static void serializeMappingsAndOnto(SQLPPMapping mapping, OWLOntology onto, String bootOwlPath, String bootOBDAPath) throws IOException, OWLOntologyStorageException {

        File bootOwlFile = new File(bootOwlPath);
        File bootOBDAFile = new File(bootOBDAPath);

        OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
        writer.write(bootOBDAFile, mapping);

        onto.getOWLOntologyManager().saveOntology(onto, new OWLXMLDocumentFormat(), new FileDocumentTarget(bootOwlFile));
    }

    static List<String> getWorkloadQueries(String workloadFile) throws IOException {
        String json = Files.lines(Paths.get(workloadFile)).collect(Collectors.joining(" "));

        List<String> result = new ArrayList<>();

        JsonElement jsonElement = JsonParser.parseString(json);

        Gson g = new Gson();

        if (jsonElement.isJsonArray()) {

            JsonArray jsonArray = jsonElement.getAsJsonArray();

            for (JsonElement element : jsonArray) {
                String jsonStringElement = element.toString();
                WorkloadJsonEntry workloadJsonEntry = g.fromJson(jsonStringElement, WorkloadJsonEntry.class);
                result.add(workloadJsonEntry.getQuery());
            }
        }
        return result;
    }
}
