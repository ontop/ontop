/**
 * by Yuanbo Guo
 * Semantic Web and Agent Technology Lab, CSE Department, Lehigh University, USA
 * Copyright (C) 2004
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package it.unibz.krdb.obda.LUBM.rdf_generator;

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