package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import com.google.common.base.Strings;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.r2rml.R2RMLWriter;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.*;

@Command(name = "to-r2rml",
        description = "Convert ontop native mapping format (.obda) to R2RML format")
public class OntopOBDAToR2RML implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-i", "--input"}, title = "mapping.obda",
            description = "Input mapping file in Ontop native format (.obda)")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    private String inputMappingFile;

    @Option(type = OptionType.COMMAND, name = {"-t", "--ontology"}, title = "ontology.owl",
            description = "OWL ontology file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    private String owlFile;

    @Option(type = OptionType.COMMAND, name = {"-o", "--output"}, title = "mapping.ttl",
            description = "Output mapping file in R2RML format (.ttl)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    private String outputMappingFile;

    @Override
    public void run() {

        if (Strings.isNullOrEmpty(outputMappingFile)) {
            outputMappingFile = inputMappingFile.substring(0, inputMappingFile.length() - ".obda".length())
                    .concat(".ttl");
        }

        File out = new File(outputMappingFile);

        OntopSQLOWLAPIConfiguration.Builder configBuilder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(inputMappingFile)
                .jdbcDriver("dummy")
                .jdbcUrl("dummy")
                .jdbcUser("")
                .jdbcPassword("");

        if (owlFile != null)
            configBuilder.ontologyFile(owlFile);

        OntopSQLOWLAPIConfiguration config = configBuilder.build();

        OBDAModel model;
        /**
         * load the mapping in native Ontop syntax
         */
        try {
            model = config.loadProvidedPPMapping();
        } catch ( InvalidMappingException | DuplicateMappingException | MappingIOException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        OWLOntology ontology;
        try {
            ontology = config.loadInputOntology()
                    .orElse(null);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        /**
         * render the mapping in the (ugly) Turtle syntax and save it to a string
         */
        R2RMLWriter writer = new R2RMLWriter(model, ontology,
                config.getInjector().getInstance(NativeQueryLanguageComponentFactory.class));

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
