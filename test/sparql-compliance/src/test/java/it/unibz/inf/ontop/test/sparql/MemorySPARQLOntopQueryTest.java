package it.unibz.inf.ontop.test.sparql;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import junit.framework.Test;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.repository.Repository;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

/*Test of SPARQL 1.0 compliance
Some test have been modified  or are missing, respect to the original test case
- DATA-R2: ALGEBRA not well designed queries actually return correct results :
			:nested-opt-1
			:nested-opt-2
			:opt-filter-1
			:opt-filter-2

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

			// Quads are not yet supported by the SI
			optionalManifest + "dawg-optional-complex-2",
			// Quads are not yet supported by the SI
			optionalManifest + "dawg-optional-complex-3",

			// Quads are not yet supported by the SI
			algebraManifest + "join-combo-2",

			/* DATA-R2: BASIC*/

			//missing result "." is not considered as part of the decimal (error is already in the sparql algebra)
			basicManifest + "term-6",

			//MalformedQueryException SPARQL Parser Encountered "."  "." is not considered as part of the decimal (error is already in the sparql algebra)
			basicManifest + "term-7",

			/* DATA-R2: CAST
			Cast with function call on the datatype is not yet supported e.g. FILTER(datatype(xsd:double(?v)) = xsd:double) . */

			castManifest + "cast-str",
			castManifest + "cast-flt",
			castManifest + "cast-dbl",
			castManifest + "cast-dec",
			castManifest + "cast-int",
			castManifest + "cast-dT",
			castManifest + "cast-bool",

			/* DATA-R2: BUILT-IN */

			exprBuiltInManifest + "sameTerm-not-eq", // JdbcSQLException: Data conversion error converting "1.0e0"

			/* DATA-R2: EXPR-EQUALS   */

			exprEqualsManifest + "eq-2-1", // JdbcSQLException: Data conversion error converting "1.0e0"
			exprEqualsManifest + "eq-2-2", // JdbcSQLException: Data conversion error converting "1.0e0"

			/* DATA-R2: OPEN_WORLD   */
			//TODO: double-check
			openWorldManifest +"date-2", // JdbcSQLException: Cannot parse "DATE" constant "2006-08-23Z"
			// > for xsd:date is not part of SPARQL 1.1
			openWorldManifest +"date-3", // JdbcSQLException: Cannot parse "DATE" constant "2006-08-23Z"

			openWorldManifest +"open-eq-07", // JdbcSQLException: Data conversion error converting "xyz"
			openWorldManifest +"open-eq-08", // JdbcSQLException: Data conversion error converting "xyz"
			openWorldManifest +"open-eq-09", // JdbcSQLException: Data conversion error converting "xyz"
			openWorldManifest +"open-eq-10", // JdbcSQLException: Data conversion error converting "xyz"
			openWorldManifest +"open-eq-11", // JdbcSQLException: Data conversion error converting "xyz"
			openWorldManifest +"open-eq-12", // JdbcSQLException: Data conversion error converting "xyz"

			// H2 has some restrictions on the combination of ORDER BY and DISTINCT
			solutionSeqManifest + "limit-4",
			// H2 has some restrictions on the combination of ORDER BY and DISTINCT
			solutionSeqManifest + "offset-4",
			// H2 has some restrictions on the combination of ORDER BY and DISTINCT
			solutionSeqManifest + "slice-5",

			/* DATA-R2: SORT */
			// TODO: support the xsd:integer cast
			sortManifest + "dawg-sort-function",

			/* DATA-R2: TYPE-PROMOTION */
			// TODO: double-check why it is the case
			typePromotionManifest + "type-promotion-13",
			typePromotionManifest + "type-promotion-11",
			typePromotionManifest + "type-promotion-10",
			typePromotionManifest + "type-promotion-09",
			typePromotionManifest + "type-promotion-14",
			typePromotionManifest + "type-promotion-08",
			typePromotionManifest + "type-promotion-19",
			typePromotionManifest + "type-promotion-12",
			typePromotionManifest + "type-promotion-18",
			typePromotionManifest + "type-promotion-15",
			typePromotionManifest + "type-promotion-16",
			typePromotionManifest + "type-promotion-17",
			typePromotionManifest + "type-promotion-27"
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
