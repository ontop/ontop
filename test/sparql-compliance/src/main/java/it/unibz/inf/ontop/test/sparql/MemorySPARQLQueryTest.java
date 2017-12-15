package it.unibz.inf.ontop.test.sparql;

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
import junit.framework.Test;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.repository.Repository;
import org.junit.Ignore;

import java.util.Properties;
import java.util.Set;

// Test of SPARQL 1.0 compliance
@Ignore("We are testing the modified version MemorySPARQLOntopQueryTest")
public class MemorySPARQLQueryTest extends SPARQLQueryParent {

	/* List of UNSUPPORTED QUERIES */

	private static final String algebraManifest = "http://www.w3.org/2001/sw/DataAccess/tests/data-r2/algebra/manifest#";
	private static final String basicManifest = "http://www.w3.org/2001/sw/DataAccess/tests/data-r2/basic/manifest#";
	private static final String booleanManifest = "http://www.w3.org/2001/sw/DataAccess/tests/data-r2/boolean-effective-value/manifest#";
	private static final String castManifest = "http://www.w3.org/2001/sw/DataAccess/tests/data-r2/cast/manifest#";
	private static final String constructManifest = "http://www.w3.org/2001/sw/DataAccess/tests/data-r2/construct/manifest#";
    private static final String datasetManifest = "http://www.w3.org/2001/sw/DataAccess/tests/data-r2/dataset/manifest#";
	private static final String distinctManifest = "http://www.w3.org/2001/sw/DataAccess/tests/data-r2/distinct/manifest#";
	private static final String exprBuiltInManifest ="http://www.w3.org/2001/sw/DataAccess/tests/data-r2/expr-builtin/manifest#";
	private static final String exprEqualsManifest ="http://www.w3.org/2001/sw/DataAccess/tests/data-r2/expr-equals/manifest#";
    private static final String graphManifest ="http://www.w3.org/2001/sw/DataAccess/tests/data-r2/graph/manifest#";
	private static final String openWorldManifest ="http://www.w3.org/2001/sw/DataAccess/tests/data-r2/open-world/manifest#";
	private static final String regexManifest ="http://www.w3.org/2001/sw/DataAccess/tests/data-r2/regex/manifest#";
	private static final String sortManifest ="http://www.w3.org/2001/sw/DataAccess/tests/data-r2/sort/manifest#";
	private static final String typePromotionManifest ="http://www.w3.org/2001/sw/DataAccess/tests/data-r2/type-promotion/manifest#";

	private static Set<String> IGNORE = ImmutableSet.of(

			/* DATA-R2: ALGEBRA
			not well designed queries actually return correct results :
			:nested-opt-1
			:nested-opt-2
			:opt-filter-1
			:opt-filter-2 */

			//Unexpected exception: Unbounded variable: v2 Problem already appears in the filter datalog translation (missing variable v)
			//LeftJoin(http://example/q(URI1(0),w),http://example/p(URI1(0),v2),EQ("null",http://www.w3.org/2001/XMLSchema#integer(1)))
			algebraManifest + "filter-scope-1",

			//Empty results. WON'T FIX: GRAPH issue Problem already appears in the datalog  translation
			algebraManifest + "join-combo-2",

			//error, missing a result, null equalities
			algebraManifest + "join-combo-1",

			//Unexpected exception: Unbounded variable: v2 Problem already appears in the filter datalog translation (missing variable v) # ?v is not in scope so ?v2 never set
			//expected empty ripTesult
			algebraManifest + "opt-filter-3",

			/* DATA-R2: BASIC*/

			//error, empty query instead of solution. UNIX line end conventions is ignored
			basicManifest + "quotes-4",

			//missing result "." is not considered as part of the decimal (error is already in the sparql algebra)
			basicManifest + "term-6",

			//MalformedQueryException SPARQL Parser Encountered "."  "." is not considered as part of the decimal (error is already in the sparql algebra)
			basicManifest + "term-7",

			/* DATA-R2: BOOLEAN EFFECTIVE VALUE */
			//Cannot return the SQL type for: w
			booleanManifest + "dawg-bev-5",

			/* DATA-R2: CAST
			Cast with function call on the datatype is not yet supported e.g. FILTER(datatype(xsd:double(?v)) = xsd:double) . */

			castManifest + "cast-str",
			castManifest + "cast-flt",
			castManifest + "cast-dbl",
			castManifest + "cast-dec",
			castManifest + "cast-int",
			castManifest + "cast-dT",
			castManifest + "cast-bool",

			/* DATA-R2: CONSTRUCT Null pointer exception */

			constructManifest + "construct-3",
			constructManifest + "construct-4",

            /* DATA-R2: DATASET NullPointerException
            loading unsupported  */

            datasetManifest + "dawg-dataset-01",
            datasetManifest + "dawg-dataset-02",
            datasetManifest + "dawg-dataset-03",
            datasetManifest + "dawg-dataset-04",
            datasetManifest + "dawg-dataset-05",
            datasetManifest + "dawg-dataset-06",
            datasetManifest + "dawg-dataset-07",
            datasetManifest + "dawg-dataset-08",
            datasetManifest + "dawg-dataset-09",
            datasetManifest + "dawg-dataset-10",
            datasetManifest + "dawg-dataset-11",
            datasetManifest + "dawg-dataset-12",
            datasetManifest + "dawg-dataset-09b",
            datasetManifest + "dawg-dataset-10b",
            datasetManifest + "dawg-dataset-12b",

			/* DATA-R2: DISTINCT Missing and unexpected bindings  */
			distinctManifest + "no-distinct-9",
			distinctManifest + "distinct-9",

			/* DATA-R2: EXPR-BUILTIN   */

			//unknown datatype are not supported, so missing type
			exprBuiltInManifest + "dawg-isLiteral-1",

			//Illegal subject value: ""^^<http://www.w3.org/2001/XMLSchema#integer>
			exprBuiltInManifest + "dawg-langMatches-2",

			//missing and unexpected bindings:
			exprBuiltInManifest + "sameTerm-eq",

			//Missing bindings:
			exprBuiltInManifest + "sameTerm-not-eq",

			//missing and unexpected bindings:
			// The reason is because DBMS may modify the string representation
			// of the original data no support for custom datatype
			exprBuiltInManifest + "sameTerm-simple",

			//Missing bindings The reason is because DBMS may modify the string representation
			//  of the original data, i.e., "1"^^xsd:double --> 1.0
			exprBuiltInManifest + "dawg-str-1",

			//Illegal subject value: ""^^<http://www.w3.org/2001/XMLSchema#integer>
			exprBuiltInManifest + "dawg-str-2",

			//java.lang.NumberFormatException: For input string: "zzz" no support for custom datatype.
			exprBuiltInManifest + "dawg-str-3",

			//NumberFormatException: For input string: "" (it should not be considered as a number)
			exprBuiltInManifest + "dawg-str-4",

            //unknownType not supported, custom datatype
            exprBuiltInManifest + "dawg-datatype-2",
            exprBuiltInManifest + "dawg-lang-1",
            exprBuiltInManifest + "dawg-lang-2",

			/* DATA-R2: EXPR-EQUALS   */
			//never ends don't manage to get the result from the sql query
			exprEqualsManifest + "eq-2-1",

			//missing and unexpected bindings, no custom datatypes supported
			exprEqualsManifest + "eq-2-2",

			//missing bindings  equality between different
			//      #    numerical datatypes is not recognized.
			exprEqualsManifest + "eq-1",
			exprEqualsManifest + "eq-2",

			//Data conversion error converting "zzz" Bad datatype handling by the classic mode.
			exprEqualsManifest + "eq-4",

            /* DATA-R2: GRAPH   */
            //Unexpected bindings: it should not return results
            graphManifest + "dawg-graph-02",

            //Problem with graph transaltion in SPARQL Projection source of ProjectionElem "g"  not found in Join
            graphManifest + "dawg-graph-03",
            graphManifest + "dawg-graph-04",

            //Unexpected bindings: one result more from dataset2
            graphManifest + "dawg-graph-05",

            //Problem with graph transaltion in SPARQL Projection source of ProjectionElem "g"  not found in Join
            graphManifest + "dawg-graph-06",
            graphManifest + "dawg-graph-07",
            graphManifest + "dawg-graph-08",
            graphManifest + "dawg-graph-09",
            graphManifest + "dawg-graph-10",
            graphManifest + "dawg-graph-10b",
            graphManifest + "dawg-graph-11",

			/* DATA-R2: OPEN_WORLD   */
			//missing and unexpected bindings, different time in the unexpected datetime result
			openWorldManifest +"date-2",
			openWorldManifest +"date-3",

			//Missing bindings no result while searching for xsd:date datatype
			openWorldManifest +"date-4",

			//Data conversion error converting "v2" data conversion error in sql
			openWorldManifest +"open-cmp-01",
			openWorldManifest +"open-cmp-02",

			//Unexpected bindings: we return values that do not strictly match 001
			openWorldManifest +"open-eq-01",

			//Missing bindings: unsupported user-defined datatype
			openWorldManifest +"open-eq-02",
            openWorldManifest +"open-eq-05",

			//Unexpected bindings: should return empty result, we cannot know what is different from an unknown datatype
			openWorldManifest +"open-eq-06",

			//Missing bindings eaulity between variables
			openWorldManifest +"open-eq-07",

			//Missing bindings: problem handling language tags
			openWorldManifest +"open-eq-08",
			openWorldManifest +"open-eq-10",
			openWorldManifest +"open-eq-11",

			//Data conversion error converting "xyz"
			openWorldManifest +"open-eq-12",

			/* DATA-R2: REGEX
			Missing bindings #string operation over URI is not supported in SI mode*/
			regexManifest + "dawg-regex-004",

			/* DATA-R2: SORT
			 * Problem with SPARQL translation
			  * Error translating ORDER BY.
			   * The current implementation can only sort by variables. This query has a more complex expression*/
			sortManifest + "dawg-sort-numbers",
			sortManifest + "dawg-sort-builtin",
			sortManifest + "dawg-sort-function",


			/* DATA-R2: TYPE-PROMOTION
			 * all removed because of unsupported types */
			typePromotionManifest + "type-promotion-13",
			typePromotionManifest + "type-promotion-11",
			typePromotionManifest + "type-promotion-07",
			typePromotionManifest + "type-promotion-10",
			typePromotionManifest + "type-promotion-09",
			typePromotionManifest + "type-promotion-14",
			typePromotionManifest + "type-promotion-08",
			typePromotionManifest + "type-promotion-19",
			typePromotionManifest + "type-promotion-22",
			typePromotionManifest + "type-promotion-20",
			typePromotionManifest + "type-promotion-21",
			typePromotionManifest + "type-promotion-12",
			typePromotionManifest + "type-promotion-18",
			typePromotionManifest + "type-promotion-15",
			typePromotionManifest + "type-promotion-16",
			typePromotionManifest + "type-promotion-17"



	);

	public static Test suite() throws Exception{
		return suite(true);
	}

	public static Test suite(boolean ignoreFailures) throws Exception {
		return ManifestTestUtils.suite(new Factory() {

			public MemorySPARQLQueryTest createSPARQLQueryTest(
					String testURI, String name, String queryFileURL,
					String resultFileURL, Dataset dataSet,
					boolean laxCardinality, boolean checkOrder) {
				if(ignoreFailures && !IGNORE.contains(testURI)) {
					return new MemorySPARQLQueryTest(testURI, name,
							queryFileURL, resultFileURL, dataSet, laxCardinality,
							checkOrder);
				}
				return null;

			}
		}, "/testcases-dawg/data-r2/manifest-evaluation.ttl");
	}


	protected MemorySPARQLQueryTest(String testURI, String name,
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
