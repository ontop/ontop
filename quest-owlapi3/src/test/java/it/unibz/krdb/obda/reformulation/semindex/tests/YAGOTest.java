package it.unibz.krdb.obda.reformulation.semindex.tests;

/*
 * #%L
 * ontop-quest-owlapi3
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

import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class YAGOTest {
	
    private final static String dataFile = "yago2core_20110315.n3";
    private static final Logger log = LoggerFactory.getLogger(YAGOTest.class);

    private static final OntologyFactory descFactory = OntologyFactoryImpl.getInstance();
   

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ontology onto = parse_tbox(dataFile);
 //       DAG dag = new DAG(onto);
    }

    private static Ontology parse_tbox(String filename) throws IOException, URISyntaxException {
        BufferedReader triples = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));

        String line;

        Pattern pattern = Pattern.compile("<(.+?)>\\s(.+?)\\s[<\"](.+?)[>\"]\\s\\.");
 
        Ontology onto = descFactory.createOntology();

        while ((line = triples.readLine()) != null) {
            if (line.startsWith("@")) {
                log.debug(line);
                continue;
            }
            Matcher matcher = pattern.matcher(line);

            boolean matchFound = matcher.find();

            if (matchFound) {
            	String subject = matcher.group(1);
            	String predicate = matcher.group(2);
            	String object = matcher.group(3);

            	// TODO (ROMAN): not necessarily object properties?
            	
                if ("rdfs:range".equals(predicate)) {
                    ObjectPropertyExpression psprop = descFactory.createObjectProperty(subject).getInverse();
                    ObjectSomeValuesFrom rs = psprop.getDomain();
                    OClass co = descFactory.createClass(object);
                    onto.addSubClassOfAxiom(rs, co);
                } 
                else if ("rdfs:domain".equals(predicate)) {
                    ObjectPropertyExpression psprop = descFactory.createObjectProperty(subject);
                    ObjectSomeValuesFrom rs = psprop.getDomain();
                    OClass co = descFactory.createClass(object);
                    onto.addSubClassOfAxiom(rs, co);
                } 
                else if ("rdf:type".equals(predicate)) {
                    // a rdf:type A |= A(a)
                    String co = object;
                    onto.getVocabulary().declareClass(co);
                }
                else if ("rdfs:subClassOf".equals(predicate)) {
                    OClass cs = descFactory.createClass(subject);
                    OClass co = descFactory.createClass(object);
                    onto.addSubClassOfAxiom(cs, co);
                } 
                else if ("rdfs:subPropertyOf".equals(predicate)) {
                    ObjectPropertyExpression rs = descFactory.createObjectProperty(subject);
                    ObjectPropertyExpression ro = descFactory.createObjectProperty(object);
                    onto.addSubPropertyOfAxiom(rs, ro);
                } else {
//                    log.debug(predicate);
                }
            } else {
                log.debug("Not matched line {}", line);
            }
        }
        triples.close();
        return onto;
    }
}
