package org.semanticweb.ontop.cli;

import com.github.rvesse.airline.Command;
import com.github.rvesse.airline.Option;
import com.github.rvesse.airline.OptionType;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlapi3.bootstrapping.DirectMappingBootstrapper;
import it.unibz.krdb.obda.r2rml.R2RMLWriter;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.net.URI;
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
            File ontologyFile = new File(owlFile);
            File obdaFile = new File(mappingFile);
            DirectMappingBootstrapper dm = new DirectMappingBootstrapper(
                    baseIRI, jdbcURL, jdbcUserName, jdbcPassword, jdbcDriverClass);

            /**
             * exports ontology
             */
            OWLOntology onto = dm.getOntology();
            onto.getOWLOntologyManager().saveOntology(onto, new FileDocumentTarget(ontologyFile));

            /**
             * exports mappings
             */
            OBDAModel model = dm.getModel();

            if(this.mappingFile.endsWith(".obda")){
                ModelIOManager mng = new ModelIOManager(model);
                mng.save(obdaFile);
            } else if(this.mappingFile.endsWith(".ttl")){
                URI sourceID = model.getSources().iterator().next().getSourceID();
                R2RMLWriter writer = new R2RMLWriter(model, sourceID, onto);
                writer.write(obdaFile);
            } else {
                throw new IllegalArgumentException("the mappings file should end with .obda or .ttl");
            }




        } catch (Exception e) {
            System.err.println("Error occurred during bootstrapping: "
                    + e.getMessage());
            System.err.println("Debugging information for developers: ");
            e.printStackTrace();
        }

    }
}
