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
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
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

        Ontology onto = OntologyFactoryImpl.getInstance().createOntology("");

        long tbox_count = 0;
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
                    tbox_count++;
                    Predicate ps = predicateFactory.getPredicate(subject, 2);
                    Predicate po = predicateFactory.getPredicate(object, 1);
                    ClassDescription rs = descFactory.createPropertySomeRestriction(ps, true);
                    ClassDescription co = descFactory.createClass(po);
                    onto.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(rs, co));
                } 
                else if ("rdfs:domain".equals(predicate)) {
                    tbox_count++;
                    Predicate ps = predicateFactory.getPredicate(subject, 2);
                    Predicate po = predicateFactory.getPredicate(object, 1);
                    ClassDescription rs = descFactory.createPropertySomeRestriction(ps, false);
                    ClassDescription co = descFactory.createClass(po);
                    onto.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(rs, co));
                } 
                else if ("rdf:type".equals(predicate)) {
                    // a rdf:type A |= A(a)
                    Predicate po = predicateFactory.getPredicate(object, 1);
                    ClassDescription co = descFactory.createClass(po);
                    onto.addConcept(po);
                }
                else if ("rdfs:subClassOf".equals(predicate)) {
                    tbox_count++;
                    Predicate ps = predicateFactory.getPredicate(subject, 1);
                    Predicate po = predicateFactory.getPredicate(object, 1);
                    ClassDescription cs = descFactory.createClass(ps);
                    ClassDescription co = descFactory.createClass(po);
                    onto.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(cs, co));
                } 
                else if ("rdfs:subPropertyOf".equals(predicate)) {
                    tbox_count++;
                    Predicate ps = predicateFactory.getPredicate(subject, 1);
                    Predicate po = predicateFactory.getPredicate(object, 1);
                    Property rs = descFactory.createProperty(ps);
                    Property ro = descFactory.createProperty(po);
                    onto.addAssertion(OntologyFactoryImpl.getInstance().createSubPropertyAxiom(rs, ro));
                } else {
//                    log.debug(predicate);
                }
            } else {
                log.debug("Not matched line {}", line);
            }
        }
        return onto;
    }
}
