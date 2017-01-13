package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.io.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.owlapi.directmapping.DirectMappingEngine;
import it.unibz.inf.ontop.owlapi.directmapping.DirectMappingEngine.BootstrappingResults;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.util.Objects;

@Command(name = "bootstrap",
        description = "Bootstrap ontology and mapping from the database")
public class OntopBootstrap extends OntopMappingOntologyRelatedCommand {

    @Option(type = OptionType.COMMAND, name = {"-b", "--base-iri"}, title = "base IRI",
            description = "base uri of the generated mapping")
    protected String baseIRI;

    @Override
    public void run() {

        try {
            if (baseIRI.contains("#")) {
                System.err.println("Base IRI cannot contain the character '#'!");
                throw new IllegalArgumentException("Base IRI cannot contain the character '#'!");
            }

            Objects.requireNonNull(owlFile, "ontology file must not be null");

            QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
                    .jdbcUrl(jdbcURL)
                    .dbUser(jdbcUserName)
                    .dbPassword(jdbcPassword)
                    .jdbcDriver(jdbcDriverClass)
                    .ontologyFile(owlFile)
                    .nativeOntopMappingFile(mappingFile)
                    .build();

            BootstrappingResults results = DirectMappingEngine.bootstrap(configuration, baseIRI);

            File ontologyFile = new File(owlFile);
            File obdaFile = new File(mappingFile);

            OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer(results.getMapping());
            writer.save(obdaFile);

            OWLOntology onto = results.getOntology();
            onto.getOWLOntologyManager().saveOntology(onto, new FileDocumentTarget(ontologyFile));

        } catch (Exception e) {
            System.err.println("Error occurred during bootstrapping: "
                    + e.getMessage());
            System.err.println("Debugging information for developers: ");
            e.printStackTrace();
        }

    }
}
