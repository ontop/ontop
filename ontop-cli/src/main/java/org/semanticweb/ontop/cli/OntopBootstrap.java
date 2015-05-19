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
        description = "Bootstrap ontology and mapping from the database")
public class OntopBootstrap extends OntopMappingOntologyRelatedCommand {

    @Option(type = OptionType.COMMAND, name = {"-b", "--base-uri"}, title = "baseURI",
            description = "base uri of the generated mapping")
    protected String baseUri;

    @Override
    public void run() {

        try {
            if (baseUri.contains("#")) {
                System.err.println("Base uri cannot contain the character '#'!");
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
                    System.err.println("Output file not found!");
                }
            }
        } catch (Exception e) {
            System.err.println("Error occured during bootstrapping: "
                    + e.getMessage());
            System.err.println("Debugging information for developers: ");
            e.printStackTrace();
        }

    }
}
