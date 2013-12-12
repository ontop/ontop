package it.unibz.krdb.obda.LUBM.rdf_generator;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

public class OwlWriter
        extends RdfWriter {
    /**
     * abbreviation of OWL namespace
     */
    private static final String T_OWL_NS = "owl";
    /**
     * prefix of the OWL namespace
     */
    private static final String T_OWL_PREFIX = T_OWL_NS + ":";

    /**
     * Constructor.
     *
     * @param generator The generator object.
     */
    public OwlWriter(Generator generator) {
        super(generator);
    }

    /**
     * Writes the header, including namespace declarations and ontology header.
     */
    void writeHeader() {
        String s;
        s = "xmlns:" + T_RDF_NS +
                "=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"";
        out.println(s);
        s = "xmlns:" + T_RDFS_NS + "=\"http://www.w3.org/2000/01/rdf-schema#\"";
        out.println(s);
        s = "xmlns:" + T_OWL_NS + "=\"http://www.w3.org/2002/07/owl#\"";
        out.println(s);
        s = "xmlns:" + T_ONTO_NS + "=\"" + generator.ontology + "#\">";
        out.println(s);
        out.println("\n");
        s = "<" + T_OWL_PREFIX + "Ontology " + T_RDF_ABOUT + "=\"\">";
        out.println(s);
        s = "<" + T_OWL_PREFIX + "imports " + T_RDF_RES + "=\"" +
                generator.ontology + "\" />";
        out.println(s);
        s = "</" + T_OWL_PREFIX + "Ontology>";
        out.println(s);
    }
}
