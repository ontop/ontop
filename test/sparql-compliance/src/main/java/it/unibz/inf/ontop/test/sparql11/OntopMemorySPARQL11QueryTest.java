package it.unibz.inf.ontop.test.sparql11;

/*
 * #%L
 * ontop-sparql-compliance
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

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import it.unibz.inf.ontop.test.sparql.QuestManifestTestUtils;
import it.unibz.inf.ontop.test.sparql.SPARQLQueryParent;
import junit.framework.Test;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.repository.Repository;
import org.junit.Ignore;

import java.util.Properties;
import java.util.Set;


//Test of SPARQL 1.1 compliance
@Ignore("unsupported files still need to be added and checked")
public class OntopMemorySPARQL11QueryTest extends SPARQLQueryParent {

	/* List of UNSUPPORTED QUERIES */

	private static final String aggregatesManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/aggregates/manifest#";
	private static final String bindManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/bind/manifest#";
	private static final String bindingsManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/bindings/manifest#";
	private static final String functionsManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/functions/manifest#";
	private static final String constructManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/construct/manifest#";
	private static final String csvTscResManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/csv-tsv-res/manifest#";
	private static final String groupingManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/grouping/manifest#";
	private static final String existsManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/exists/manifest#";
	private static final String projectExpressionManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/project-expression/manifest#";
	private static final String propertyPathManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/property-path/manifest#";
	private static final String subqueryManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/subquery/manifest#";


	private static Set<String> IGNORE = ImmutableSet.of(

			/* AGGREGATES
			not supported yet */

			aggregatesManifest + "agg01",
			aggregatesManifest + "agg02",
			aggregatesManifest + "agg03",
			aggregatesManifest + "agg04",
			aggregatesManifest + "agg05",
			aggregatesManifest + "agg06",
			aggregatesManifest + "agg07",
			aggregatesManifest + "agg08",
			aggregatesManifest + "agg08b",
			aggregatesManifest + "agg09",
			aggregatesManifest + "agg10",
			aggregatesManifest + "agg11",
			aggregatesManifest + "agg12",
			aggregatesManifest + "agg-groupconcat-01",
			aggregatesManifest + "agg-groupconcat-02",
			aggregatesManifest + "agg-groupconcat-03",
			aggregatesManifest + "agg-sum-01",
			aggregatesManifest + "agg-sum-01-ontop",
			aggregatesManifest + "agg-sum-02",
			aggregatesManifest + "agg-sum-02-ontop",
			aggregatesManifest + "agg-avg-01",
			aggregatesManifest + "agg-avg-01-ontop",
			aggregatesManifest + "agg-avg-02",
			aggregatesManifest + "agg-min-01",
			aggregatesManifest + "agg-min-01-ontop",
			aggregatesManifest + "agg-min-02",
			aggregatesManifest + "agg-min-02-ontop",
			aggregatesManifest + "agg-max-01",
			aggregatesManifest + "agg-max-01-ontop",
			aggregatesManifest + "agg-max-02",
			aggregatesManifest + "agg-max-02-ontop",
			aggregatesManifest + "agg-sample-01",
			aggregatesManifest + "agg-err-01",
			aggregatesManifest + "agg-err-02",
			aggregatesManifest + "agg-sum-order-01",

			/* BIND
			not supported yet */
			bindManifest + "bind03",
			bindManifest + "bind04",
			bindManifest + "bind07",
			/* BINDINGS
			not supported yet */
			bindingsManifest + "",
			/* FUNCTIONS
			not supported yet */
			functionsManifest + "",
			/* CONSTRUCT
			not supported yet */
			constructManifest + "",
			/* CSV
			not supported yet */
			csvTscResManifest + "",
			/* GROUPING
			not supported yet */
			groupingManifest + "",
			/* EXISTS
			not supported yet */
			existsManifest + "",
			/* PROJECT
			not supported yet */
			projectExpressionManifest + "",
			/* PROPERTY PATH
			not supported yet */
			propertyPathManifest + "",
			/* SUBQUERY
			not supported yet */
			subqueryManifest + ""






	);

	public static Test suite() throws Exception {
		return QuestManifestTestUtils.suite(new Factory() {

			public OntopMemorySPARQL11QueryTest createSPARQLQueryTest(
					String testURI, String name, String queryFileURL,
					String resultFileURL, Dataset dataSet,
					boolean laxCardinality, boolean checkOrder) {
				if(!IGNORE.contains(testURI)) {
				return new OntopMemorySPARQL11QueryTest(testURI, name,
						queryFileURL, resultFileURL, dataSet, laxCardinality,
						checkOrder);
				}
				return null;
			}
		}, "/testcases-dawg-sparql-1.1/manifest-all.ttl");
	}

	protected OntopMemorySPARQL11QueryTest(String testURI, String name,
			String queryFileURL, String resultFileURL, Dataset dataSet,
			boolean laxCardinality, boolean checkOrder) {
		super(testURI, name, queryFileURL, resultFileURL, dataSet,
				laxCardinality, checkOrder);
	}

	@Override
	protected Repository newRepository() throws SemanticIndexException {
		try(OntopSemanticIndexLoader loader = OntopSemanticIndexLoader.loadRDFGraph(dataset, new Properties())) {
			Repository repository = OntopRepository.defaultRepository(loader.getConfiguration());
			repository.initialize();
			return repository;
		}
	}
}
