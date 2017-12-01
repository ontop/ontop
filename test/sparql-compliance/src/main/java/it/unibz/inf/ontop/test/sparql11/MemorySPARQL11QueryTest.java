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
import it.unibz.inf.ontop.test.sparql.ManifestTestUtils;
import it.unibz.inf.ontop.test.sparql.SPARQLQueryParent;
import junit.framework.Test;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.repository.Repository;
import org.junit.Ignore;

import java.util.Properties;
import java.util.Set;


//Test of SPARQL 1.1 compliance
@Ignore("unsupported files still need to be added and checked")
public class MemorySPARQL11QueryTest extends SPARQLQueryParent {

	/* List of UNSUPPORTED QUERIES */

	private static final String aggregatesManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/aggregates/manifest#";
	private static final String bindManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/bind/manifest#";
	private static final String bindingsManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/bindings/manifest#";
	private static final String functionsManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/functions/manifest#";
	private static final String constructManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/construct/manifest#";
	private static final String csvTscResManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/csv-tsv-res/manifest#";
	private static final String groupingManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/grouping/manifest#";
	private static final String negationManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/negation/manifest#";
	private static final String existsManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/exists/manifest#";
	private static final String projectExpressionManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/project-expression/manifest#";
	private static final String propertyPathManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/property-path/manifest#";
	private static final String subqueryManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/subquery/manifest#";
	private static final String serviceManifest = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/service/manifest#";


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
			aggregatesManifest + "agg-empty-group",

			/* BIND
			not supported yet */
			bindManifest + "bind03",
			bindManifest + "bind04",
			bindManifest + "bind07",

			/* BINDINGS
			not supported yet */
			bindingsManifest + "values7",
			bindingsManifest + "values6",
			bindingsManifest + "values8",
			bindingsManifest + "values5",

			/* FUNCTIONS
			not supported yet */
			functionsManifest + "bnode01",
			functionsManifest + "bnode02",
			functionsManifest + "ceil01",
			functionsManifest + "coalesce01",
			functionsManifest + "concat02",
			functionsManifest + "encode01",
			functionsManifest + "floor01",
			functionsManifest + "hours",
			functionsManifest + "if01",
			functionsManifest + "if02",
			functionsManifest + "in01",
			functionsManifest + "in02",
			functionsManifest + "iri01",
			functionsManifest + "lcase01",
			functionsManifest + "md5-01",
			functionsManifest + "md5-02",
			functionsManifest + "minutes",
			functionsManifest + "notin01",
			functionsManifest + "notin02",
			functionsManifest + "now01",
			functionsManifest + "plus-1",
			functionsManifest + "plus-2",
			functionsManifest + "rand01",
			functionsManifest + "replace01",
			functionsManifest + "round01",
			functionsManifest + "seconds",
			functionsManifest + "sha1-01",
			functionsManifest + "sha1-02",
			functionsManifest + "sha512-01",
			functionsManifest + "sha512-02",
			functionsManifest + "strafter01a",
			functionsManifest + "strafter02",
			functionsManifest + "strbefore01a",
			functionsManifest + "strbefore02",
			functionsManifest + "strdt01",
			functionsManifest + "strdt02",
			functionsManifest + "strdt03",
			functionsManifest + "strlang01",
			functionsManifest + "strlang02",
			functionsManifest + "strlang03",
			functionsManifest + "struuid01",
			functionsManifest + "substring01",
			functionsManifest + "substring02",
			functionsManifest + "timezone",
			functionsManifest + "tz",
			functionsManifest + "ucase01",
			functionsManifest + "uuid01",

			/* CONSTRUCT
			not supported yet */
			constructManifest + "constructwhere01",
			constructManifest + "constructwhere02",
			constructManifest + "constructwhere03",
			constructManifest + "constructwhere04",

			/* CSV
			not supported yet */
			csvTscResManifest + "tsv03",

			/* GROUPING
			not supported yet */
			groupingManifest + "group01",
			groupingManifest + "group03",
			groupingManifest + "group04",
			groupingManifest + "group05",

			/* NEGATION
			not supported yet */
            negationManifest + "subset-by-exclusion-nex-1",
			negationManifest + "subset-by-exclusion-minus-1",
			negationManifest + "temporal-proximity-by-exclusion-nex-1",
			negationManifest + "subset-01",
			negationManifest + "subset-02",
			negationManifest + "set-equals-1",
			negationManifest + "subset-03",
			negationManifest + "exists-01",
			negationManifest + "exists-02",
			negationManifest + "full-minuend",
			negationManifest + "partial-minuend",

			/* EXISTS
			not supported yet */
			existsManifest + "exists01",
			existsManifest + "exists02",
			existsManifest + "exists03",
			existsManifest + "exists04",
			existsManifest + "exists05",

			/* PROJECT
			not supported yet */
			projectExpressionManifest + "projexp07",
			projectExpressionManifest + "projexp05",
			projectExpressionManifest + "projexp02",

			/* PROPERTY PATH
			not supported yet */
			propertyPathManifest + "pp02",
			propertyPathManifest + "pp06",
			propertyPathManifest + "pp12",
			propertyPathManifest + "pp14",
			propertyPathManifest + "pp16",
			propertyPathManifest + "pp21",
			propertyPathManifest + "pp23",
			propertyPathManifest + "pp25",
			propertyPathManifest + "pp28a",
			propertyPathManifest + "pp34",
			propertyPathManifest + "pp35",
			propertyPathManifest + "pp36",
			propertyPathManifest + "pp37",

			/* SERVICE
			not supported yet */
			serviceManifest + "service1",
			serviceManifest + "service2",
			serviceManifest + "service3",
			serviceManifest + "service4a",
			serviceManifest + "service5",
			serviceManifest + "service6",
			serviceManifest + "service7",


			/* SUBQUERY
			not supported yet */
			subqueryManifest + "subquery02",
			subqueryManifest + "subquery04",
			subqueryManifest + "subquery07",
			subqueryManifest + "subquery08",
			subqueryManifest + "subquery10",
			subqueryManifest + "subquery11",
			subqueryManifest + "subquery12",
			subqueryManifest + "subquery13",
			subqueryManifest + "subquery14"


	);

	public static Test suite() throws Exception {
		return ManifestTestUtils.suite(new Factory() {

			public MemorySPARQL11QueryTest createSPARQLQueryTest(
					String testURI, String name, String queryFileURL,
					String resultFileURL, Dataset dataSet,
					boolean laxCardinality, boolean checkOrder) {
				if(!IGNORE.contains(testURI)) {
				return new MemorySPARQL11QueryTest(testURI, name,
						queryFileURL, resultFileURL, dataSet, laxCardinality,
						checkOrder);
				}
				return null;
			}
		}, "/testcases-dawg-sparql-1.1/manifest-all.ttl");
	}

	protected MemorySPARQL11QueryTest(String testURI, String name,
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
