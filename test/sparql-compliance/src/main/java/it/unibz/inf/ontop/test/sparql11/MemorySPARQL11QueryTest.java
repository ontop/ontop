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
import it.unibz.inf.ontop.test.sparql.ManifestTestUtils;
import it.unibz.inf.ontop.test.sparql.MemoryOntopTestCase;
import org.eclipse.rdf4j.query.Dataset;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;


//Test of SPARQL 1.1 compliance

@RunWith(Parameterized.class)
public class MemorySPARQL11QueryTest extends MemoryOntopTestCase {

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


	private static ImmutableSet<String> IGNORE = ImmutableSet.of(

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

			/* BIND */
			//The sub-term: ADD(o,http://www.w3.org/2001/XMLSchema#integer(+1)) is not a VariableOrGroundTerm
			bindManifest + "bind03",
            //converision in SPARQL failed: Projection source of ProjectionElem "nova"
            // not found in Extension
			bindManifest + "bind04",
            //typing exception In ADD("null",http://www.w3.org/2001/XMLSchema#integer(+2)): Incompatible type inferred : expected: numeric term, actual: NULL
			bindManifest + "bind07",

			/* BINDINGS
			 */
			//Unexpected bindings
			bindingsManifest + "values7",
            //Missing bindings:
			bindingsManifest + "values6",
			bindingsManifest + "values8",
			bindingsManifest + "values5",

			/* FUNCTIONS*/

			//bnode not supported in SPARQL transformation
			functionsManifest + "bnode01",
			functionsManifest + "bnode02",

			//problem with the numbers format e.g. 1.0 instead of 1
			functionsManifest + "ceil01",

			//coalesce not supported in SPARQL transformation
			functionsManifest + "coalesce01",

			//wrong equality with lang
			functionsManifest + "concat02",

			//encoding is simply removed
			functionsManifest + "encode01",

			//problem with the numbers format
			functionsManifest + "floor01",

			//extract hours return 0
			functionsManifest + "hours",

			//not supported in SPARQL transformation
			functionsManifest + "if01",
			functionsManifest + "if02",
			functionsManifest + "in01",
			functionsManifest + "in02",
			functionsManifest + "iri01",

			//missing info about lang
			functionsManifest + "lcase01",

			//not supported in H2 transformation
			functionsManifest + "md5-01",
			functionsManifest + "md5-02",

			//extract minutes return 0
			functionsManifest + "minutes",

			// No data or composite atom in List
			functionsManifest + "notin01",
			functionsManifest + "notin02",
			functionsManifest + "now01",

			//Incompatible type inferred : expected: numeric term, actual: LITERAL
			functionsManifest + "plus-1",
			functionsManifest + "plus-2",

			// No data or composite atom in List
			functionsManifest + "rand01",

			//missing info about lang
			functionsManifest + "replace01",

			//problem with the numbers format
			functionsManifest + "round01",

			//extract seconds return 0
			functionsManifest + "seconds",

			//SHA1 is not supported in H2
			functionsManifest + "sha1-01",
			functionsManifest + "sha1-02",

			//SHA512 is not supported in H2
			functionsManifest + "sha512-01",
			functionsManifest + "sha512-02",

			//The type should already be for a non-variable - non-expression term e.g "e"^^xsd:string
			functionsManifest + "strafter01a",
			functionsManifest + "strafter02",
			functionsManifest + "strbefore01a",
			functionsManifest + "strbefore02",

			//not supported in SPARQL transformation
			functionsManifest + "strdt01",
			functionsManifest + "strdt02",
			functionsManifest + "strdt03",
			functionsManifest + "strlang01",
			functionsManifest + "strlang02",
			functionsManifest + "strlang03",

			//No data or composite atom in List
			functionsManifest + "struuid01",

			//The type should already be for a non-variable - non-expression term e.g "e"^^xsd:string
			functionsManifest + "substring01",
			functionsManifest + "substring02",

			//not supported in SPARQL transformation
			functionsManifest + "timezone",

			//TZ is not supported in H2
			functionsManifest + "tz",

			// missing language tag
			functionsManifest + "ucase01",

			//No data or composite atom in List
			functionsManifest + "uuid01",

			/* CONSTRUCT not supported yet*/
			//Projection cannot be cast to Reduced in rdf4j
			constructManifest + "constructwhere01",
			constructManifest + "constructwhere02",
			constructManifest + "constructwhere03",
			//problem importing dataset
			constructManifest + "constructwhere04",

			/* CSV */
			// Sorting by IRI is not supported by the SI
			csvTscResManifest + "tsv01",
			// Sorting by IRI is not supported by the SI
			csvTscResManifest + "tsv02",
			//different format for number and not supported custom datatype
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
//			negationManifest + "subset-by-exclusion-minus-1",
			negationManifest + "temporal-proximity-by-exclusion-nex-1",
			negationManifest + "subset-01",
			negationManifest + "subset-02",
			negationManifest + "set-equals-1",
			negationManifest + "subset-03",
			negationManifest + "exists-01",
			negationManifest + "exists-02",
//			negationManifest + "full-minuend",
//			negationManifest + "partial-minuend",

			/* EXISTS
			not supported yet */
			existsManifest + "exists01",
			existsManifest + "exists02",
			existsManifest + "exists03",
			existsManifest + "exists04",
			existsManifest + "exists05",

			/* PROJECT */
			//Unexpected function in the query: SPARQL_DATATYPE
			projectExpressionManifest + "projexp07",
			projectExpressionManifest + "projexp05",

			//Data conversion error converting "foobar"
			projectExpressionManifest + "projexp02",

			/* PROPERTY PATH*/
			// Not supported: ArbitraryLengthPath
			propertyPathManifest + "pp02",

			//wrong result, unexpected binding
			propertyPathManifest + "pp06",

			// Not supported: ArbitraryLengthPath
			propertyPathManifest + "pp12",
			propertyPathManifest + "pp14",
			propertyPathManifest + "pp16",
			propertyPathManifest + "pp21",
			propertyPathManifest + "pp23",
			propertyPathManifest + "pp25",

			//Not supported: ZeroLengthPath
			propertyPathManifest + "pp28a",

			// Not supported: ArbitraryLengthPath
			propertyPathManifest + "pp34",
			propertyPathManifest + "pp35",
			propertyPathManifest + "pp36",
			propertyPathManifest + "pp37",

			/* SERVICE
			not supported yet */
			serviceManifest + "service1",

			//no loading of the dataset
			serviceManifest + "service2",
			serviceManifest + "service3",

			serviceManifest + "service4a",
			serviceManifest + "service5",
			//no loading of the dataset
			serviceManifest + "service6",
			serviceManifest + "service7",


			/* SUBQUERY
			*/
			//Unexpected bindings
			subqueryManifest + "subquery02",
			subqueryManifest + "subquery04",

			//Problem SPARQL translation: Projection source of ProjectionElem "g"
			// not found in StatementPattern FROM NAMED CONTEXT
			subqueryManifest + "subquery07",

			//fucntion is not supported
			subqueryManifest + "subquery08",
			subqueryManifest + "subquery10",

			//wrong SQL translation Column "SUB_QVIEW.O" missing
			subqueryManifest + "subquery11",

			//unbound variable: Var
			subqueryManifest + "subquery12",

			//wrong SQL translation Column "SUB_QVIEW.O" missing
			subqueryManifest + "subquery13",

			//missing results
			subqueryManifest + "subquery14"
	);

	public MemorySPARQL11QueryTest(String testIRI, String name, String queryFileURL, String resultFileURL, Dataset dataSet,
								   boolean laxCardinality, boolean checkOrder, ImmutableSet<String> ignoredTests) {
		super(testIRI, name, queryFileURL, resultFileURL, dataSet, laxCardinality, checkOrder, ignoredTests);
	}

	@Parameterized.Parameters(name="{1}")
	public static Collection<Object[]> parameters() throws Exception {
		return ManifestTestUtils.parametersFromSuperManifest(
				"/testcases-dawg-sparql-1.1/manifest-all.ttl",
				IGNORE);
	}
}
