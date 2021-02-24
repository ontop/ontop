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


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.Equivalences;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.impl.DatatypeImpl;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.rdf.api.IRI;

import static it.unibz.inf.ontop.utils.SITestingTools.loadOntologyFromFileAndClassify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class DAGTest {

	public final static Logger log = LoggerFactory.getLogger(DAGTest.class);

	public static final String owlloc = "src/test/resources/test/semanticIndex_ontologies/";

	private static final String owl_exists = "::__exists__::";
	private static final String owl_inverse_exists = "::__inverse__exists__::";
	private static final String owl_inverse = "::__inverse__::";

	private static final RDF rdf = new SimpleRDF();

	private List<List<Description>> get_results(ClassifiedTBox reasoner, String resname) throws Exception {
		File results = new File(owlloc + resname + ".si");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(results);

		doc.getDocumentElement().normalize();
		List<Description> cls = get_dag_type(reasoner, doc, "classes");
		List<Description> roles = get_dag_type(reasoner, doc, "rolles"); // the spelling is essential

		return ImmutableList.of(cls, roles);
	}

	/**
	 * Extract particular type of DAG nodes from XML document
	 *
	 * @param doc  XML document containing encoded DAG nodes
	 * @param type type of DAGNodes to extract
	 * @return a list of DAGNodes
	 */
	private List<Description> get_dag_type(ClassifiedTBox reasoner, Document doc, String type) {
		List<Description> rv = new LinkedList<>();
		Node root = doc.getElementsByTagName(type).item(0);
		NodeList childNodes = root.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

				Element node = (Element) childNodes.item(i);
				String uri = node.getAttribute("uri");

				boolean inverse = false;
				boolean exists = false;

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

				IRI iri = rdf.createIRI(uri);
				final Description description;
				if (type.equals("classes")) {
					if (exists) {
						if (reasoner.objectProperties().contains(iri)) {
							ObjectPropertyExpression prop = reasoner.objectProperties().get(iri);
							description = inverse ? prop.getRange() : prop.getDomain();
						}
						else {
							DataPropertyExpression prop = reasoner.dataProperties().get(iri);
							description = prop.getDomainRestriction(DatatypeImpl.rdfsLiteral);
						}
					}
					else
						description = reasoner.classes().get(iri);
				}
				else {
					if (reasoner.objectProperties().contains(iri)) {
						ObjectPropertyExpression prop = reasoner.objectProperties().get(iri);
						description = inverse ? prop.getInverse() : prop;
					}
					else {
						description = reasoner.dataProperties().get(iri);
					}
				}

				rv.add(description);
			}
		}
		return rv;
	}

	private void test_dag_index_nodes(String testname) throws Exception {

		ClassifiedTBox reasoner = loadOntologyFromFileAndClassify(owlloc + testname + ".owl");
		List<List<Description>> exp_idx = get_results(reasoner, testname);

		List<Description> classes = new LinkedList<>();
		for (Equivalences<ClassExpression> node : reasoner.classesDAG()) {
			for (ClassExpression c : node)
				classes.add(c);
		}
		for (Equivalences<DataRangeExpression> node : reasoner.dataRangesDAG()) {
			for (DataRangeExpression c : node)
				classes.add(c);
		}
		
		List<Description> roles = new LinkedList<>();
		for (Equivalences<ObjectPropertyExpression> node : reasoner.objectPropertiesDAG()) {
			for (ObjectPropertyExpression r : node)
				roles.add(r);
		}
		for (Equivalences<DataPropertyExpression> node : reasoner.dataPropertiesDAG()) {
			for (DataPropertyExpression r : node) {
				roles.add(r);
				roles.add(r); // ROMAN: hacky way of double-counting data properties (which have no inverses)
			}
		}
		
		assertEquals(exp_idx.get(0).size(), classes.size());
		assertEquals(exp_idx.get(1).size(), roles.size());

		for (Description node : exp_idx.get(0)) {
			assertTrue(classes.contains(node));
		}
		for (Description node : exp_idx.get(1)) {
			assertTrue(roles.contains(node));
		}
	}

	@Test
	public void test_1_0_0() throws Exception {
		test_dag_index_nodes("test_1_0_0");
	}

	@Test
	public void test_1_0_1() throws Exception {
		test_dag_index_nodes("test_1_0_1");
	}

	@Test
	public void test_1_1_0() throws Exception {
		test_dag_index_nodes("test_1_1_0");
	}

	@Test
	public void test_1_2_0() throws Exception {
		test_dag_index_nodes("test_1_2_0");
	}

	@Test
	public void test_1_3_0() throws Exception {
		test_dag_index_nodes("test_1_3_0");
	}

	@Test
	public void test_1_5_0() throws Exception {
		test_dag_index_nodes("test_1_5_0");
	}
}
