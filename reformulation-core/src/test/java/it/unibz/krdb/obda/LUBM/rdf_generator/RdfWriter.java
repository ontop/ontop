package it.unibz.krdb.obda.LUBM.rdf_generator;

/*
 * #%L
 * ontop-reformulation-core
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public abstract class RdfWriter implements Writer {
    /**
     * abbreviation of univ-bench ontology namesapce
     */
    static final String T_ONTO_NS = "ub";
    /**
     * prefix of univ-bench ontology namespace
     */
    static final String T_ONTO_PREFIX = T_ONTO_NS + ":";
    /**
     * abbreviation of RDF namespace
     */
    static final String T_RDF_NS = "rdf";
    /**
     * prefix of RDF namespace
     */
    static final String T_RDF_PREFIX = T_RDF_NS + ":";
    /**
     * abbreviation of RDFS namespace
     */
    static final String T_RDFS_NS = "rdfs";
    /**
     * prefix of RDFS namespace
     */
    static final String T_RDFS_PREFIX = T_RDF_NS + ":";
    /**
     * string of "rdf:Id"
     */
    static final String T_RDF_ID = T_RDF_PREFIX + "ID";
    /**
     * string of "rdf:about"
     */
    static final String T_RDF_ABOUT = T_RDF_PREFIX + "about";
    /**
     * string of "rdf:resource
     */
    static final String T_RDF_RES = T_RDF_PREFIX + "resource";
    /**
     * white space string
     */
    static final String T_SPACE = " ";

    /**
     * output stream
     */
    PrintStream out = null;
    /**
     * the generator
     */
    Generator generator;

    /**
     * Constructor.
     *
     * @param generator The generator object.
     */
    public RdfWriter(Generator generator) {
        this.generator = generator;
    }

    /**
     * Implementation of Writer:start.
     */
    public void start() {
    }

    /**
     * Implementation of Writer:end.
     */
    public void end() {
    }

    /**
     * Implementation of Writer:startFile.
     */
    public void startFile(String fileName) {
        String s;
        try {
            out = new PrintStream(new FileOutputStream(fileName));
            s = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
            out.println(s);
            s = "<" + T_RDF_PREFIX + "RDF";
            out.println(s);
            writeHeader();
        } catch (IOException e) {
            System.out.println("Create file failure!");
        }
    }

    /**
     * Implementation of Writer:endFile.
     */
    public void endFile() {
        String s;
        s = "</" + T_RDF_PREFIX + "RDF>";
        out.println(s);
        out.close();
    }

    /**
     * Implementation of Writer:startSection.
     */
    public void startSection(int classType, String id) {
        generator.startSectionCB(classType);
        out.println();
        String s = "<" + T_ONTO_PREFIX + Generator.CLASS_TOKEN[classType] + T_SPACE +
                T_RDF_ABOUT + "=\"" + id + "\">";
        out.println(s);
    }

    /**
     * Implementation of Writer:startAboutSection.
     */
    public void startAboutSection(int classType, String id) {
        generator.startAboutSectionCB(classType);
        out.println();
        String s = "<" + T_ONTO_PREFIX + Generator.CLASS_TOKEN[classType] + T_SPACE +
                T_RDF_ABOUT + "=\"" + id + "\">";
        out.println(s);
    }

    /**
     * Implementation of Writer:endSection.
     */
    public void endSection(int classType) {
        String s = "</" + T_ONTO_PREFIX + Generator.CLASS_TOKEN[classType] + ">";
        out.println(s);
    }

    /**
     * Implementation of Writer:addProperty.
     */
    public void addProperty(int property, String value, boolean isResource) {
        generator.addPropertyCB(property);

        String s;
        if (isResource) {
            s = "   <" + T_ONTO_PREFIX + Generator.PROP_TOKEN[property] + T_SPACE +
                    T_RDF_RES + "=\"" + value + "\" />";
        } else { //literal
            s = "   <" + T_ONTO_PREFIX + Generator.PROP_TOKEN[property] + ">" + value +
                    "</" + T_ONTO_PREFIX + Generator.PROP_TOKEN[property] + ">";
        }

        out.println(s);
    }

    /**
     * Implementation of Writer:addProperty.
     */
    public void addProperty(int property, int valueClass, String valueId) {
        generator.addPropertyCB(property);
        generator.addValueClassCB(valueClass);

        String s;
        s = "   <" + T_ONTO_PREFIX + Generator.PROP_TOKEN[property] + ">\n" +
                "      <" + T_ONTO_PREFIX + Generator.CLASS_TOKEN[valueClass] + T_SPACE +
                T_RDF_ABOUT + "=\"" + valueId + "\" />" +
                "   </" + T_ONTO_PREFIX + Generator.PROP_TOKEN[property] + ">";

        out.println(s);
    }

    /**
     * Writes the header part.
     */
    abstract void writeHeader();
}
