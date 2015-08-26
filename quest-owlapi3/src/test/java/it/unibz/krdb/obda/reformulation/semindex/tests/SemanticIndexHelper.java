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


import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.DatatypeImpl;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.h2.jdbcx.JdbcDataSource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Helper class to load ontologies and compare computed values to expected results
 *
 * @author Sergejs Pugac
 */

// USED IN TWO TESTS ONLY

public class SemanticIndexHelper {
    public final static Logger log = LoggerFactory.getLogger(SemanticIndexHelper.class);

    public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
       
    public static final String owlloc = "src/test/resources/test/semanticIndex_ontologies/";
    private Ontology onto;
    
    public transient Connection conn;

    private final  OntologyFactory descFactory = OntologyFactoryImpl.getInstance();

    private String owl_exists = "::__exists__::";
    private String owl_inverse_exists = "::__inverse__exists__::";
    private String owl_inverse = "::__inverse__::";

    public SemanticIndexHelper() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:db1");
        try {
            conn = ds.getConnection();
        } catch (SQLException e) {
            log.error("Error creating test database");
            e.printStackTrace();
        }
    }

    public Ontology load_onto(String ontoname) throws Exception {
        String owlfile = owlloc + ontoname + ".owl";
        OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(new File(owlfile));
 
        Ontology ontology = OWLAPI3TranslatorUtility.translate(owlOntology);
        return ontology;
    }

    public TBoxReasoner load_dag(String ontoname) throws Exception {
    	onto = load_onto(ontoname);
    	return TBoxReasonerImpl.create(onto);
    }

    public List<List<Description>> get_results(String resname) {
        String resfile = owlloc + resname + ".si";
        File results = new File(resfile);
        Document doc = null;


        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(results);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        doc.getDocumentElement().normalize();
        List<Description> cls = get_dag_type(doc, "classes");
        List<Description> roles = get_dag_type(doc, "rolles");

        List<List<Description>> rv = new ArrayList<List<Description>>(2);
        rv.add(cls);
        rv.add(roles);
        return rv;
    }

    /**
     * Extract particular type of DAG nodes from XML document
     *
     * @param doc  XML document containing encoded DAG nodes
     * @param type type of DAGNodes to extract
     * @return a list of DAGNodes
     */
    private List<Description> get_dag_type(Document doc, String type) {
        List<Description> rv = new LinkedList<Description>();
        Node root = doc.getElementsByTagName(type).item(0);
        NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

                Element node = (Element) childNodes.item(i);
                String uri = node.getAttribute("uri");
                int idx = Integer.parseInt(node.getAttribute("index"));

                int arity = 1;
                boolean inverse = false;
                boolean exists = false;
                Predicate p;
                Description description;

                if (uri.startsWith(owl_exists)) {
                    uri = uri.substring(owl_exists.length());
                    arity = 2;
                    exists = true;

                } else if (uri.startsWith(owl_inverse_exists)) {
                    uri = uri.substring(owl_inverse_exists.length());
                    arity = 2;
                    inverse = true;
                    exists = true;
                } else if (uri.startsWith(owl_inverse)) {
                    uri = uri.substring(owl_inverse.length());
                    inverse = true;
                }

                if (type.equals("classes")) {
                    if (exists) {
                    	if (onto.getVocabulary().containsObjectProperty(uri)) {
                        	ObjectPropertyExpression prop = onto.getVocabulary().getObjectProperty(uri);
                        	if (inverse)
                        		prop = prop.getInverse();
                            description = prop.getDomain();
                    	}
                    	else {
                    		DataPropertyExpression prop = onto.getVocabulary().getDataProperty(uri);
                    		description = prop.getDomainRestriction(DatatypeImpl.rdfsLiteral);
                    	}
                    }
                    else
                        description = onto.getVocabulary().getClass(uri);
                } 
                else {
                	if (onto.getVocabulary().containsObjectProperty(uri)) {	
                    	ObjectPropertyExpression prop = onto.getVocabulary().getObjectProperty(uri);
                        if (inverse)
                        	description = prop.getInverse();
                        else
                        	description = prop;
                	}
                	else {
                		description = onto.getVocabulary().getDataProperty(uri);
                	}
                }


                Description _node = description;

//                _node.setIndex(idx);
//                _node.setRange(new SemanticIndexRange());
//
//                String[] range = node.getAttribute("range").split(",");
//                for (int j = 0; j < range.length; j++) {
//                   String[] interval = range[j].split(":");
//                   int start = Integer.parseInt(interval[0]);
//                    int end = Integer.parseInt(interval[1]);
//                    _node.getRange().addInterval(start, end);
//                }
                rv.add(_node);
            }
        }
        return rv;
    }

    public List<String[]> get_abox(String resname) throws Exception {
        String resfile = owlloc + resname + ".abox";
        List<String[]> rv = new LinkedList<String[]>();
      
        FileInputStream fstream = new FileInputStream(resfile);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        while ((strLine = br.readLine()) != null) {
            String[] tokens = strLine.split(" ");
            rv.add(tokens);
        }
        br.close();
        
        return rv;
    }

}
