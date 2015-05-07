package org.semanticweb.ontop.cli;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlapi3.bootstrapping.DirectMappingBootstrapper;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;

@Command(name = "bootstrap",
        description = "Bootstrap ontology and mapping from the database", hidden = true)
public class OntopBootstrap implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-t", "--ontology"}, title = "ontologyFile",
            description = "OWL ontology file")
    protected String owlFile;

    @Option(type = OptionType.COMMAND, name = {"-m", "--mapping"}, title = "mappingFile",
            description = "Mapping file in R2RML (.ttl) or in Ontop native format (.obda)", required = true)
    protected String mappingFile;

    @Option(type = OptionType.COMMAND, name = {"-u", "--username"}, title = "jdbcUserName",
            description = "user name for the jdbc connection (only for R2RML mapping)")
    protected String jdbcUserName;

    @Option(type = OptionType.COMMAND, name = {"-p", "--password"}, title = "jdbcPassword",
            description = "password for the jdbc connection  (only for R2RML mapping)")
    protected String jdbcPassword;

    @Option(type = OptionType.COMMAND, name = {"-l", "--url"}, title = "jdbcUrl",
            description = "jdbcUrl for the jdbc connection  (only for R2RML mapping)")
    protected String jdbcUrl;

    @Option(type = OptionType.COMMAND, name = {"-d", "--driver-class"}, title = "jdbcUrl",
            description = "class name of the jdbc Driver (only for R2RML mapping)")
    protected String jdbcDriverClass;

    @Option(type = OptionType.COMMAND, name = {"-b", "--base-uri"}, title = "baseURI",
            description = "base uri of the generated mapping")
    protected String baseUri;

    @Override
    public void run() {

        try {
            if (baseUri.contains("#")) {
                System.out
                        .println("Base uri cannot contain the character '#'!");
            } else {
                if (owlFile != null) {
                    File owl = new File(owlFile);
                    File obda = new File(mappingFile);
                    DirectMappingBootstrapper dm = new DirectMappingBootstrapper(
                            baseUri, jdbcUrl, jdbcUserName, jdbcPassword, jdbcDriverClass);
                    OBDAModel model = dm.getModel();
                    OWLOntology onto = dm.getOntology();
                    ModelIOManager mng = new ModelIOManager(model);
                    mng.save(obda);
                    onto.getOWLOntologyManager().saveOntology(onto,
                            new FileDocumentTarget(owl));
                } else {
                    System.out.println("Output file not found!");
                }
            }
        } catch (Exception e) {
            System.out.println("Error occured during bootstrapping: "
                    + e.getMessage());
            System.out.println("Debugging information for developers: ");
            e.printStackTrace();
        }

    }
}
