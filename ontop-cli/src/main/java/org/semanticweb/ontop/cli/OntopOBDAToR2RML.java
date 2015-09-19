package org.semanticweb.ontop.cli;

import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.github.rvesse.airline.Command;
import com.github.rvesse.airline.Option;
import com.github.rvesse.airline.OptionType;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.io.InvalidDataSourceException;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.r2rml.R2RMLWriter;
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

        QuestPreferences preferences = new QuestPreferences();
        Injector injector = Guice.createInjector(new OBDACoreModule(preferences));
        NativeQueryLanguageComponentFactory factory = injector.getInstance(NativeQueryLanguageComponentFactory.class);

        MappingParser mappingParser = factory.create(new File(obdaURI));

        OBDAModel model;
        /**
         * load the mapping in native Ontop syntax
         */
        try {
            model = mappingParser.getOBDAModel();
        } catch (IOException | InvalidMappingException | DuplicateMappingException | InvalidDataSourceException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        URI srcURI = model.getSources().iterator().next().getSourceID();

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
