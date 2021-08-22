package it.unibz.inf.ontop.docker.mysql;

/*
 * #%L
 * ontop-quest-sesame
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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import junit.framework.TestCase;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class SesameVirtualTest extends TestCase {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String owlFile = "/mysql/example/exampleBooks.owl";
	private static final String obdaFile = "/mysql/example/exampleBooks.obda";
	private static final  String propertyFile = "/mysql/example/exampleBooks.properties";
	private final String owlFileName = this.getClass().getResource(owlFile).toString();
	private final String obdaFileName = this.getClass().getResource(obdaFile).toString();
	private final String propertyFileName = this.getClass().getResource(propertyFile).toString();

	public void test() {

		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFileName)
				.nativeOntopMappingFile(obdaFileName)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();

		Repository repo = OntopRepository.defaultRepository(configuration);

		repo.initialize();

		RepositoryConnection con = repo.getConnection();

		///query repo

		String queryString = "select * where {?x ?z ?y }";
		//"<http://www.semanticweb.org/tibagosi/ontologies/2012/11/Ontology1355819752067.owl#Book>}";
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult result = tupleQuery.evaluate();

		List<String> bindings = result.getBindingNames();
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			for (String b : bindings)
				log.debug("Binding: " + bindingSet.getBinding(b));
		}

		result.close();

		queryString = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o FILTER(?s = <http://meraka/moss/exampleBooks.owl#book/23/>)}";
		GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
		GraphQueryResult gresult = graphQuery.evaluate();
		while (gresult.hasNext()) {
			Statement s = gresult.next();
			System.out.println(s.toString());
		}

		System.out.println("Closing...");
		con.close();

		System.out.println("Done.");
	}


}

