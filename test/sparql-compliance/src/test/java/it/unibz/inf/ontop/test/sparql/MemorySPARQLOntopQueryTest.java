package it.unibz.inf.ontop.test.sparql;

import com.google.common.collect.ImmutableSet;
import org.eclipse.rdf4j.query.Dataset;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

/*Test of SPARQL 1.0 compliance
Some test have been modified  or are missing, respect to the original test case

- DATA-R2: GRAPH folder is missing-
- DATA-R2: DATASET folder is missing

*/

@RunWith(Parameterized.class)
public class MemorySPARQLOntopQueryTest extends MemoryOntopTestCase {

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
	private static final String solutionSeqManifest ="http://www.w3.org/2001/sw/DataAccess/tests/data-r2/solution-seq/manifest#";
	private static final String sortManifest ="http://www.w3.org/2001/sw/DataAccess/tests/data-r2/sort/manifest#";
	private static final String typePromotionManifest ="http://www.w3.org/2001/sw/DataAccess/tests/data-r2/type-promotion/manifest#";
	private static final String optionalManifest = "http://www.w3.org/2001/sw/DataAccess/tests/data-r2/optional/manifest#";

	private static final ImmutableSet<String> IGNORE = ImmutableSet.of(

			optionalManifest + "dawg-optional-complex-2", // quads are not supported by the SI
			optionalManifest + "dawg-optional-complex-3", // quads are not supported by the SI

			algebraManifest + "join-combo-2", // quads are not supported by the SI

			// RDF4J SPARQL parser bug ("." after an integer is considered a triple separator rather than part of the decimal)
			basicManifest + "term-6",  // missing result
			basicManifest + "term-7", // org.eclipse.rdf4j.query.MalformedQueryException: Encountered "."

			// SPARQL cast with function call on the datatype is not supported, e.g., FILTER(datatype(xsd:double(?v)) = xsd:double)
			castManifest + "cast-str",
			castManifest + "cast-flt",
			castManifest + "cast-dbl",
			castManifest + "cast-dec",
			castManifest + "cast-int",
			castManifest + "cast-dT",
			castManifest + "cast-bool",

			exprBuiltInManifest + "sameTerm-eq", // JdbcSQLException: Data conversion error converting "zzz"
			exprBuiltInManifest + "sameTerm-not-eq", // JdbcSQLException: Data conversion error converting "1.0e0" (H2 issue: "1.0e0"^^xsd:#double in the data & result)

			exprEqualsManifest + "eq-2-1", // JdbcSQLException: Data conversion error converting "1.0e0" (H2 issue: "1.0e0"^^xsd:#double in the data & result)
			exprEqualsManifest + "eq-2-2", // JdbcSQLException: Data conversion error converting "1.0e0" (H2 issue: "1.0e0"^^xsd:#double in the data & result)

			openWorldManifest +"date-2", // JdbcSQLException: Cannot parse "DATE" constant "2006-08-23Z" (H2 issue: "2006-08-23Z"^^xsd:date in the data & result)
			openWorldManifest +"date-3", // JdbcSQLException: Cannot parse "DATE" constant "2006-08-23Z" (H2 issue: "2006-08-23Z"^^xsd:date in the data & result)
			openWorldManifest +"open-eq-07", // JdbcSQLException: Data conversion error converting "xyz" ("xyz"^^xsd:integer in the data & result)
			openWorldManifest +"open-eq-08", // JdbcSQLException: Data conversion error converting "xyz" ("xyz"^^xsd:integer in the data & result)
			openWorldManifest +"open-eq-09", // JdbcSQLException: Data conversion error converting "xyz" ("xyz"^^xsd:integer in the data ONLY)
			openWorldManifest +"open-eq-10", // JdbcSQLException: Data conversion error converting "xyz" ("xyz"^^xsd:integer in the data & result)
			openWorldManifest +"open-eq-11", // JdbcSQLException: Data conversion error converting "xyz" ("xyz"^^xsd:integer in the data & result)
			openWorldManifest +"open-eq-12", // JdbcSQLException: Data conversion error converting "xyz" ("xyz"^^xsd:integer in the data & result)

			sortManifest + "dawg-sort-function" // SPARQL cast with function call on the datatype is not supported, e.g., ORDER BY xsd:integer(?o)
	);

	public MemorySPARQLOntopQueryTest(String testIRI, String name, String queryFileURL, String resultFileURL,
									  Dataset dataSet, boolean laxCardinality, boolean checkOrder,
									  ImmutableSet<String> ignoredTests) {
		super(testIRI, name, queryFileURL, resultFileURL, dataSet, laxCardinality, checkOrder, ignoredTests);
	}

	@Parameterized.Parameters(name="{1}")
	public static Collection<Object[]> parameters() throws Exception {
		return ManifestTestUtils.parametersFromSuperManifest(
				"/testcases-dawg-quest/data-r2/manifest-evaluation.ttl",
				IGNORE);
	}
}
