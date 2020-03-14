package it.unibz.inf.ontop.cli;


import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.restrictions.Required;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

@Command(name = "pretty-r2rml",
        description = "prettify R2RML file using Jena")
public class OntopR2RMLPrettify implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-i", "--input"}, title = "input.ttl",
            description = "Input mapping file in the turtle R2RML format (.ttl)")
    @Required
    protected String inputMappingFile;


    @Option(type = OptionType.COMMAND, name = {"-o", "--output"}, title = "pretty.ttl",
            description = "Output mapping file in the turtle R2RML format (.ttl)")
    @Required
    protected String outputMappingFile;

    @Override
    public void run() {
        try {
            Model model = RDFDataMgr.loadModel(new File(inputMappingFile).toURI().toString(), Lang.TURTLE);
            OutputStream out = new FileOutputStream(outputMappingFile);
            RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY) ;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
