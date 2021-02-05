package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.restrictions.Required;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

import java.io.*;

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
            Model m = Rio.parse(new FileInputStream(inputMappingFile), "", RDFFormat.TURTLE);
            m.setNamespace("rr", "http://www.w3.org/ns/r2rml#");

            WriterConfig settings = new WriterConfig();
            settings.set(BasicWriterSettings.PRETTY_PRINT, true);
            settings.set(BasicWriterSettings.INLINE_BLANK_NODES, true);
            FileOutputStream os = new FileOutputStream(outputMappingFile);
            Rio.write(m, os, RDFFormat.TURTLE, settings);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
