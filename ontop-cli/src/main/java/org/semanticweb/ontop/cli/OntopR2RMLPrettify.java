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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Command line tool for making R2RML file pretty using Jena
 */
public class OntopR2RMLPrettify {

    public static void main(String[] args) throws FileNotFoundException {

        if(args.length != 2){
            System.err.println("Usage: org.semanticweb.ontop.cli.R2RMLPrettify r2rml.ttl prettified_r2rml.ttl");
            System.err.println("");
            System.exit(0);
        }

        String r2rmlFile = args[0];

        String outputR2RMLFile = args[1];

        Model model = RDFDataMgr.loadModel(new File(r2rmlFile).toURI().toString(), Lang.TURTLE);

        OutputStream out = new FileOutputStream(outputR2RMLFile);

        RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY) ;

    }
}
