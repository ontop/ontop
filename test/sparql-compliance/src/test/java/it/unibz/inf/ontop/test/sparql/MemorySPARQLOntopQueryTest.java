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

- DATA-R2: EXPR-BUILTIN modification in the result files
removed unknown datatype from
expr-builtin/result-isliteral-1

removed hierarchical language tag form
expr-builtin/result-langMatches-2.ttl

modified string representation and datatype
expr-builtin/result-sameTerm.ttl

modified string representation
expr-builtin/result-str-1.ttl
expr-builtin/result-str-2.ttl

removed custom datatype
expr-builtin/result-str-3.ttl

- DATA-R2: EXPR-EQUALS

removed equality between different numerical datatypes
expr-equals/data-eq.ttl
expr-equals/result-eq-1.ttl
expr-equals/result-eq-2.ttl

removed mismatch in data representation, equality and custom datatype
expr-equals/result-eq-2-1.ttl

removed custom datatype
expr-equals/result-eq2-2.ttl

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

	private static ImmutableSet<String> IGNORE = ImmutableSet.of(

			// Quads are not yet supported by the SI
			optionalManifest + "dawg-optional-complex-2",
			// Quads are not yet supported by the SI
			optionalManifest + "dawg-optional-complex-3",

			// Quads are not yet supported by the SI
			algebraManifest + "join-combo-2",
			//error, missing a result, null equalities. TODO: fix
			algebraManifest + "join-combo-1",

			/* DATA-R2: BASIC*/

			//error, empty query instead of solution. UNIX line end conventions is ignored
			basicManifest + "quotes-4",

			//missing result "." is not considered as part of the decimal (error is already in the sparql algebra)
			basicManifest + "term-6",

			//MalformedQueryException SPARQL Parser Encountered "."  "." is not considered as part of the decimal (error is already in the sparql algebra)
			basicManifest + "term-7",

			// "+5"^^xsd:integer is stored as a 5 integer by H2, which is in then rebuilt as "5"^^xsd:integer, which is not strictly equal to the initial one.
			// Ontop must not return a result, test not passed due to a fair design choice of the Semantic Index (canonicalization of numbers)
			basicManifest + "term-8",

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

			// Results look correct. Possible problem from RD4J (graph isomorphism). TODO: test more recent RDF4J.
			constructManifest + "construct-3",
			// Results look correct. Possible problem from RD4J (graph isomorphism). TODO: test more recent RDF4J.
			constructManifest + "construct-4",

			/* DATA-R2: DISTINCT */
			// NB: includes 3 tests. Incompatible with the SI (normalized lexical values)
			distinctManifest + "no-distinct-9",
			// NB: includes 3 tests. Incompatible with the SI (normalized lexical values + DISTINCT on IRI)
			distinctManifest + "distinct-9",

			// The DISTINCT blocks a CASE using the IRI dictionary function (SI limitation)
			distinctManifest + "distinct-3",

			//Incompatible with the SI mode: normalized lexical values + custom datatype
			exprBuiltInManifest + "sameTerm-eq",

			//Incompatible with the SI mode: normalized lexical values
			exprBuiltInManifest + "sameTerm-not-eq",

			//missing and unexpected bindings:
			// The reason is because DBMS may modify the string representation
			// of the original data no support for custom datatype
			exprBuiltInManifest + "sameTerm-simple",

			/* DATA-R2: EXPR-EQUALS   */

			//missing and unexpected bindings, no custom datatypes supported
			exprEqualsManifest + "eq-2-2",

			// SI is not supporting arbitrary datatypes and lexical terms are normalized
			exprEqualsManifest + "eq-2-1",

			// Lexical "values" of doubles are not preserved by the Semantic Index, so cannot match a non-canonical one
			exprEqualsManifest + "eq-graph-2",

			/* DATA-R2: OPEN_WORLD   */
			//TODO: double-check
			openWorldManifest +"date-2",
			// > for xsd:date is not part of SPARQL 1.1
			openWorldManifest +"date-3",

			//TODO: check with there is no xsd:date in the mapping
			openWorldManifest +"date-4",

			// Datatype unsupported by the SI
			openWorldManifest +"open-cmp-02",
			
			//Missing bindings: unsupported user-defined datatype
			openWorldManifest +"open-eq-02",

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

			// H2 has some restrictions on the combination of ORDER BY and DISTINCT
			solutionSeqManifest + "limit-4",
			// H2 has some restrictions on the combination of ORDER BY and DISTINCT
			solutionSeqManifest + "offset-4",
			// H2 has some restrictions on the combination of ORDER BY and DISTINCT
			solutionSeqManifest + "slice-5",

			/* DATA-R2: SORT */

			// TODO: support the xsd:integer cast
			sortManifest + "dawg-sort-function",
			// Sorted by an IRI, not supported by the SI
			sortManifest + "dawg-sort-3",
			// Sorted by an IRI, not supported by the SI
			sortManifest + "dawg-sort-6",
			// Sorted by an IRI, not supported by the SI
			sortManifest + "dawg-sort-8",


			/* DATA-R2: TYPE-PROMOTION
			 * all removed because of unsupported types */
			// TODO: double-check why it is the case
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
