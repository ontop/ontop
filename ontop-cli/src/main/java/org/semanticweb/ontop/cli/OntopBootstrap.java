package org.semanticweb.ontop.cli;

import com.github.rvesse.airline.Command;
import com.github.rvesse.airline.Option;
import com.github.rvesse.airline.OptionType;
import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.owlapi3.bootstrapping.DirectMappingBootstrapper;
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
