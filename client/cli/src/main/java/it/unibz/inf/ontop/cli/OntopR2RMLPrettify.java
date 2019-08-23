package it.unibz.inf.ontop.cli;

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
            description = "Input mapping file in Ontop native format (.obda)")
    @Required
    protected String inputMappingFile;


    @Option(type = OptionType.COMMAND, name = {"-o", "--output"}, title = "pretty.ttl",
            description = "Input mapping file in Ontop native format (.obda)")
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
