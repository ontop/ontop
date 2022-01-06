package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.spec.mapping.bootstrap.DirectMappingBootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.DirectMappingBootstrapper.BootstrappingResults;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.util.Objects;

@Command(name = "bootstrap",
        description = "Bootstrap ontology and mapping from the database")
public class OntopBootstrap extends AbstractOntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-b", "--base-iri"}, title = "base IRI",
            description = "Base IRI of the generated mapping")
    @Required
    protected String baseIRI;

    @Option(type = OptionType.COMMAND, name = {"-t", "--ontology"}, title = "ontology file",
            description = "Output OWL ontology file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    @Required
    String owlFile;

    @Option(type = OptionType.COMMAND, name = {"-m", "--mapping"}, title = "mapping file",
            description = "Output mapping file in the Ontop native format (.obda)")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String mappingFile;

    @Override
    public void run() {

        try {
            if (baseIRI.contains("#")) {
                System.err.println("Base IRI cannot contain the character '#'!");
                throw new IllegalArgumentException("Base IRI cannot contain the character '#'!");
            }

            Objects.requireNonNull(owlFile, "ontology file must not be null");

            OntopSQLOWLAPIConfiguration.Builder<? extends OntopSQLOWLAPIConfiguration.Builder> builder = OntopSQLOWLAPIConfiguration.defaultBuilder();

            if (propertiesFile != null)
                builder.propertyFile(propertiesFile);

            if (dbPassword != null)
                builder.jdbcPassword(dbPassword);

            if (dbUrl != null)
                builder.jdbcUrl(dbUrl);

            if (dbUser != null)
                builder.jdbcUser(dbUser);

            if (dbDriver != null)
                builder.jdbcDriver(dbDriver);

            OntopSQLOWLAPIConfiguration configuration = builder.build();

            DirectMappingBootstrapper bootstrapper = DirectMappingBootstrapper.defaultBootstrapper();
            BootstrappingResults results = bootstrapper.bootstrap(configuration, baseIRI);

            File ontologyFile = new File(owlFile);
            File obdaFile = new File(mappingFile);

            OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
            writer.write(obdaFile, results.getPPMapping());

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
