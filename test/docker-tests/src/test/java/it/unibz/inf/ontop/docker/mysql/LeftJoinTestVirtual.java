package it.unibz.inf.ontop.docker.mysql;

/*
 * #%L
 * ontop-test
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


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class LeftJoinTestVirtual extends AbstractVirtualModeTest {

	private static final String owlfile = "/mysql/person/person.owl";
	private static final String obdafile = "/mysql/person/person.obda";
	private static final String propertiesfile = "/mysql/person/person.properties";

	private static OntopOWLReasoner REASONER;
	private static OntopOWLConnection CONNECTION;

	@BeforeClass
	public static void before() throws OWLOntologyCreationException {
		REASONER = createReasoner(owlfile, obdafile, propertiesfile);
		CONNECTION = REASONER.getConnection();
	}

	@AfterClass
	public static void after() throws OWLException {
		CONNECTION.close();
		REASONER.dispose();
	}


	@Test
	public void testLeftJoin() throws Exception {
		String query1 = "PREFIX : <http://www.semanticweb.org/mindaugas/ontologies/2013/9/untitled-ontology-58#> SELECT * WHERE {?p a :Person . ?p :name ?name . ?p :age ?age }";
		String query2 = "PREFIX : <http://www.semanticweb.org/mindaugas/ontologies/2013/9/untitled-ontology-58#> SELECT * WHERE {?p a :Person . ?p :name ?name . OPTIONAL {?p :nick11 ?nick1} OPTIONAL {?p :nick22 ?nick2} }";
		String query3 = "PREFIX : <http://www.semanticweb.org/mindaugas/ontologies/2013/9/untitled-ontology-58#> SELECT * WHERE {?p a :Person . ?p :name ?name . OPTIONAL {?p :nick11 ?nick1} }";
		String query4 = "PREFIX : <http://www.semanticweb.org/mindaugas/ontologies/2013/9/untitled-ontology-58#> SELECT * WHERE {?p a :Person . ?p :name ?name . OPTIONAL {?p :nick1 ?nick1} OPTIONAL {?p :nick2 ?nick2} }";
		String query5 = "PREFIX : <http://www.semanticweb.org/mindaugas/ontologies/2013/9/untitled-ontology-58#> SELECT * WHERE {?p a :Person . ?p :name ?name . OPTIONAL {?p :age ?age} }";

		countResults( 3, query1);
		countResults( 4, query2);
		countResults(4, query3);
		countResults( 4, query4);
		countResults(4, query5);
	}

	@Override
	protected OntopOWLStatement createStatement() throws OWLException {
		return CONNECTION.createStatement();
	}
}
