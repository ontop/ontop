package org.semanticweb.ontop.cli;

import com.google.common.base.Strings;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.github.rvesse.airline.Command;
import com.github.rvesse.airline.Option;
import com.github.rvesse.airline.OptionType;
import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.r2rml.R2RMLWriter;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.*;
import java.net.URI;

@Command(name = "to-r2rml",
        description = "Convert ontop native mapping format (.obda) to R2RML format")
public class OntopOBDAToR2RML implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-i", "--input"}, title = "mapping.obda",
            description = "Input mapping file in Ontop native format (.obda)", required = true)
    protected String inputMappingFile;

    @Option(type = OptionType.COMMAND, name = {"-t", "--ontology"}, title = "ontology.owl",
            description = "OWL ontology file")
    protected String owlFile;

    @Option(type = OptionType.COMMAND, name = {"-o", "--output"}, title = "mapping.ttl",
            description = "Output mapping file in R2RML format (.ttl)")
    protected String outputMappingFile;

    @Override
    public void run() {

        if (Strings.isNullOrEmpty(outputMappingFile)) {
            outputMappingFile = inputMappingFile.substring(0, inputMappingFile.length() - ".obda".length())
                    .concat(".ttl");
        }

        File out = new File(outputMappingFile);
        URI obdaURI = new File(inputMappingFile).toURI();
        // create model
        OBDAModel model = OBDADataFactoryImpl.getInstance().getOBDAModel();


        // obda mapping
        ModelIOManager modelIO = new ModelIOManager(model);

        /**
         * load the mapping in native Ontop syntax
         */
        try {
            modelIO.load(new File(obdaURI));
        } catch (IOException | InvalidMappingException e) {
            e.printStackTrace();
        }

        URI srcURI = model.getSources().get(0).getSourceID();

        OWLOntology ontology = null;
        if (owlFile != null) {

            // Loading the OWL file
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            try {
                ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }
        }

        /**
         * render the mapping in the (ugly) Turtle syntax and save it to a string
         */
        R2RMLWriter writer = new R2RMLWriter(model, srcURI, ontology);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            writer.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String mappingStr= null;

        try {
            mappingStr = outputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        /**
         * use Jena API to output a pretty version of the R2RML mappings
         */

        final Model rdfModel = ModelFactory.createDefaultModel();

         RDFDataMgr.read(rdfModel, new ByteArrayInputStream(mappingStr.getBytes()), Lang.TURTLE);


        //rdfModel.read(new ByteArrayInputStream(mappingStr.getBytes()), /* base */null);
        //OutputStream out = new FileOutputStream(outputR2RMLFile);

        try {
            RDFDataMgr.write(new FileOutputStream(out), rdfModel, RDFFormat.TURTLE_PRETTY) ;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("R2RML mapping file " + outputMappingFile + " written!");
    }
}
