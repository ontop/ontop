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

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.ontology.SomeValuesFrom;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;

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

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final OntologyFactory descFactory = OntologyFactoryImpl.getInstance();
   

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ontology onto = parse_tbox(dataFile);
        DAG dag = new DAG(onto);
    }

    private static Ontology parse_tbox(String filename) throws IOException, URISyntaxException {
        BufferedReader triples = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));

        String line;
        String subject;
        String object;
        String predicate;

        Pattern pattern = Pattern.compile("<(.+?)>\\s(.+?)\\s[<\"](.+?)[>\"]\\s\\.");
        Matcher matcher;

        Ontology onto = descFactory.createOntology();

        while ((line = triples.readLine()) != null) {
            if (line.startsWith("@")) {
                log.debug(line);
                continue;
            }
            matcher = pattern.matcher(line);

            boolean matchFound = matcher.find();

            if (matchFound) {
                subject = matcher.group(1);
                predicate = matcher.group(2);
                object = matcher.group(3);

                if ("rdfs:range".equals(predicate)) {
                    PropertyExpression psprop = descFactory.createProperty(subject).getInverse();
                    SomeValuesFrom rs = descFactory.createPropertySomeRestriction(psprop);
                    OClass co = descFactory.createClass(object);
                    onto.addAssertionWithCheck(descFactory.createSubClassAxiom(rs, co));
                } 
                else if ("rdfs:domain".equals(predicate)) {
                    PropertyExpression psprop = descFactory.createProperty(subject);
                    SomeValuesFrom rs = descFactory.createPropertySomeRestriction(psprop);
                    OClass co = descFactory.createClass(object);
                    onto.addAssertionWithCheck(descFactory.createSubClassAxiom(rs, co));
                } 
                else if ("rdf:type".equals(predicate)) {
                    // a rdf:type A |= A(a)
                    String co = object;
                    onto.getVocabulary().declareClass(co);
                }
                else if ("rdfs:subClassOf".equals(predicate)) {
                    OClass cs = descFactory.createClass(subject);
                    OClass co = descFactory.createClass(object);
                    onto.addAssertionWithCheck(descFactory.createSubClassAxiom(cs, co));
                } 
                else if ("rdfs:subPropertyOf".equals(predicate)) {
                    PropertyExpression rs = descFactory.createProperty(subject);
                    PropertyExpression ro = descFactory.createProperty(object);
                    onto.addAssertionWithCheck(descFactory.createSubPropertyAxiom(rs, ro));
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
