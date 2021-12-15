package it.unibz.inf.ontop.test.sparql11;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.test.sparql.ManifestTestUtils;
import it.unibz.inf.ontop.test.sparql.MemoryOntopTestCase;
import org.eclipse.rdf4j.query.Dataset;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;


/**
 * Test of SPARQL 1.1 compliance
 */
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

			/* AGGREGATES */
			// TODO: support xsd:double cast
			aggregatesManifest + "agg-err-02",

			// TODO: Find a right version of H2. Fail on 1.4.196
			aggregatesManifest + "agg-groupconcat-01",
			aggregatesManifest + "agg-groupconcat-02",
			aggregatesManifest + "agg-groupconcat-03",

			/* FUNCTIONS*/

			// the SI does not preserve the original timezone
			functionsManifest + "hours",
			// the SI does not preserve the original timezone
			functionsManifest + "day",

			//not supported in H2 transformation
			functionsManifest + "md5-01",
			functionsManifest + "md5-02",

			//The SI does not support IRIs as ORDER BY conditions
			functionsManifest + "plus-1",
			//The SI does not support IRIs as ORDER BY conditions
			functionsManifest + "plus-2",

			//SHA1 is not supported in H2
			functionsManifest + "sha1-01",
			functionsManifest + "sha1-02",

			//SHA512 is not supported in H2
			functionsManifest + "sha512-01",
			functionsManifest + "sha512-02",

			//not supported in SPARQL transformation
			functionsManifest + "strdt01",
			functionsManifest + "strdt02",
			functionsManifest + "strdt03",
			functionsManifest + "strlang01",
			functionsManifest + "strlang02",
			functionsManifest + "strlang03",

			//not supported in SPARQL transformation
			functionsManifest + "timezone",

			//TZ is not supported in H2
			functionsManifest + "tz",

			//problem importing dataset
			constructManifest + "constructwhere04",

			/* CSV */
			// Sorting by IRI is not supported by the SI
			csvTscResManifest + "tsv01",
			// Sorting by IRI is not supported by the SI
			csvTscResManifest + "tsv02",
			//different format for number and not supported custom datatype
			csvTscResManifest + "tsv03",

			/* NEGATION
			not supported yet */
            negationManifest + "subset-by-exclusion-nex-1",
			negationManifest + "temporal-proximity-by-exclusion-nex-1",
			negationManifest + "subset-01",
			negationManifest + "subset-02",
			negationManifest + "set-equals-1",
			negationManifest + "subset-03",
			negationManifest + "exists-01",
			negationManifest + "exists-02",

			// DISABLED DUE TO ORDER OVER IRI
			negationManifest + "full-minuend",
			// DISABLED DUE TO ORDER OVER IRI
			negationManifest + "partial-minuend",

			/* EXISTS
			not supported yet */
			existsManifest + "exists01",
			existsManifest + "exists02",
			existsManifest + "exists03",
			existsManifest + "exists04",
			existsManifest + "exists05",

			/* PROPERTY PATH*/
			// Not supported: ArbitraryLengthPath
			propertyPathManifest + "pp02",

			//wrong result, unexpected binding
			propertyPathManifest + "pp06",

			// Quads are not yet supported by the SI
			propertyPathManifest + "pp07",

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
			// Quads are not yet supported by the SI
			subqueryManifest + "subquery01",
			// Quads are not yet supported by the SI
			subqueryManifest + "subquery02",
			// Quads are not yet supported by the SI
			subqueryManifest + "subquery03",
			// Quads are not yet supported by the SI
			subqueryManifest + "subquery04",
			// Quads are not yet supported by the SI
			subqueryManifest + "subquery05",
			// Quads are not yet supported by the SI
			subqueryManifest + "subquery07",
			// EXISTS is not supported yet
			subqueryManifest + "subquery10",

			//ORDER BY IRI (not supported by the SI)
			subqueryManifest + "subquery11",

			//ORDER BY IRI (not supported by the SI)
			subqueryManifest + "subquery13"
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
