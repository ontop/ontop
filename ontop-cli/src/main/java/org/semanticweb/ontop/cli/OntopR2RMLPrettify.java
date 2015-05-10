package org.semanticweb.ontop.cli;

/*
 * #%L
 * ontop-cli
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.hp.hpl.jena.rdf.model.Model;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
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
            description = "Input mapping file in Ontop native format (.obda)", required = true)
    protected String inputMappingFile;

    @Option(type = OptionType.COMMAND, name = {"-o", "--output"}, title = "pretty.ttl",
            description = "Input mapping file in Ontop native format (.obda)", required = true)
    protected String outputMappingFile;

    public static void main(String[] args) throws FileNotFoundException {

//        if(args.length != 2){
//            System.err.println("Usage: org.semanticweb.ontop.cli.R2RMLPrettify r2rml.ttl prettified_r2rml.ttl");
//            System.err.println("");
//            System.exit(0);
//        }
//
//        String r2rmlFile = args[0];
//
//        String outputR2RMLFile = args[1];
//
//        Model model = RDFDataMgr.loadModel(new File(r2rmlFile).toURI().toString(), Lang.TURTLE);
//
//        OutputStream out = new FileOutputStream(outputR2RMLFile);
//
//        RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY) ;

    }

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
