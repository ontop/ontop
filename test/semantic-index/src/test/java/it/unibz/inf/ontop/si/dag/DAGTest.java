package it.unibz.inf.ontop.si.dag;

/*
 * #%L
 * ontop-quest-owlapi
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


import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.Equivalences;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.impl.DatatypeImpl;
import it.unibz.inf.ontop.spec.ontology.impl.ClassifiedTBoxImpl;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorUtility;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DAGTest extends TestCase {

	public final static Logger log = LoggerFactory.getLogger(DAGTest.class);

	public static final String owlloc = "src/test/resources/test/semanticIndex_ontologies/";
	private Ontology onto;

	private static final String owl_exists = "::__exists__::";
	private static final String owl_inverse_exists = "::__inverse__exists__::";
	private static final String owl_inverse = "::__inverse__::";


	public List<List<Description>> get_results(String resname) {
		String resfile = owlloc + resname + ".si";
		File results = new File(resfile);
		Document doc = null;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(results);
		}
		catch (Exception e) {
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

				boolean inverse = false;
				boolean exists = false;
				Description description;

				if (uri.startsWith(owl_exists)) {
					uri = uri.substring(owl_exists.length());
					exists = true;
				}
				else if (uri.startsWith(owl_inverse_exists)) {
					uri = uri.substring(owl_inverse_exists.length());
					inverse = true;
					exists = true;
				}
				else if (uri.startsWith(owl_inverse)) {
					uri = uri.substring(owl_inverse.length());
					inverse = true;
				}

				if (type.equals("classes")) {
					if (exists) {
						if (onto.objectProperties().contains(uri)) {
							ObjectPropertyExpression prop = onto.objectProperties().get(uri);
							if (inverse)
								prop = prop.getInverse();
							description = prop.getDomain();
						}
						else {
							DataPropertyExpression prop = onto.dataProperties().get(uri);
							description = prop.getDomainRestriction(DatatypeImpl.rdfsLiteral);
						}
					}
					else
						description = onto.classes().get(uri);
				}
				else {
					if (onto.objectProperties().contains(uri)) {
						ObjectPropertyExpression prop = onto.objectProperties().get(uri);
						if (inverse)
							description = prop.getInverse();
						else
							description = prop;
					}
					else {
						description = onto.dataProperties().get(uri);
					}
				}

				Description _node = description;
				rv.add(_node);
			}
		}
		return rv;
	}

	private void test_dag_index_nodes(String testname) throws Exception {

		onto = OWLAPITranslatorUtility.loadOntologyFromFile(owlloc + testname + ".owl");
		ClassifiedTBox reasoner = ClassifiedTBoxImpl.create(onto);
		List<List<Description>> exp_idx = get_results(testname);

		List<Description> classes= new LinkedList<Description>();
		for(Equivalences<ClassExpression> node : reasoner.classes().dag()) {
			for(ClassExpression c: node)
				classes.add(c);
		}
		for(Equivalences<DataRangeExpression> node : reasoner.dataRanges().dag()) {
			for(DataRangeExpression c: node)
				classes.add(c);
		}
		
		List<Description> roles= new LinkedList<Description>();
		for (Equivalences<ObjectPropertyExpression> node : reasoner.objectProperties().dag()) {
			for (ObjectPropertyExpression r: node)
				roles.add(r);
		}
		for (Equivalences<DataPropertyExpression> node : reasoner.dataProperties().dag()) {
			for (DataPropertyExpression r: node) {
				roles.add(r);
				roles.add(r); // ROMAN: hacky way of double-counting data properties (which have no inverses)
			}
		}
		
		System.out.println(classes);
		System.out.println(roles);
		assertEquals(exp_idx.get(0).size(), classes.size());
		assertEquals(exp_idx.get(1).size(), roles.size());

		for (Description node : exp_idx.get(0)) {
			classes.contains(node);
		}
		for (Description node : exp_idx.get(1)) {
			roles.contains(node);
		}
	}

	public void test_1_0_0() throws Exception {
		String testname = "test_1_0_0";
		test_dag_index_nodes(testname);
	}

	public void test_1_0_1() throws Exception {
		String testname = "test_1_0_1";
		test_dag_index_nodes(testname);
	}

	public void test_1_1_0() throws Exception {
		String testname = "test_1_1_0";
		test_dag_index_nodes(testname);
	}

	public void test_1_2_0() throws Exception {
		String testname = "test_1_2_0";
		test_dag_index_nodes(testname);
	}

	public void test_1_3_0() throws Exception {
		String testname = "test_1_3_0";
		test_dag_index_nodes(testname);
	}

	// public void test_1_4_0() throws Exception {
	// String testname = "test_1_4_0";
	// test_dag_index_nodes(testname);
	// }

	public void test_1_5_0() throws Exception {
		String testname = "test_1_5_0";
		test_dag_index_nodes(testname);
	}
}
