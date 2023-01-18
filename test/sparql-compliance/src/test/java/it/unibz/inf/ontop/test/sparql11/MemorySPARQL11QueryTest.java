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


	private static final ImmutableSet<String> IGNORE = ImmutableSet.of(

			/* AGGREGATES */
			aggregatesManifest + "agg-err-02", // SPARQL cast with function call on the datatype is not supported, e.g., COALESCE(xsd:double(?p),0)))

			/* FUNCTIONS*/
			functionsManifest + "hours", // TODO: incorrect answers when timezone is present (CAST ... AS TIMESTAMP)
			functionsManifest + "day", // TODO: incorrect answers when timezone is present (CAST ... AS TIMESTAMP)
			functionsManifest + "tz", // TZ is not supported in H2

			functionsManifest + "md5-01", // not supported in H2
			functionsManifest + "md5-02", // not supported in H2
			functionsManifest + "sha1-01", // not supported in H2
			functionsManifest + "sha1-02", // not supported in H2
			functionsManifest + "sha512-01", // not supported in H2
			functionsManifest + "sha512-02", // not supported in H2

			functionsManifest + "strdt01", // STRDT(?o,xsd:string) is not supported by the SPARQL-to-IQ translation
			functionsManifest + "strdt02", // STRDT(?o,xsd:string) is not supported by the SPARQL-to-IQ translation
			functionsManifest + "strdt03", // STRDT(?o,xsd:string) is not supported by the SPARQL-to-IQ translation

			functionsManifest + "strlang01", // STRLANG(?str,"en-US") is not supported by the SPARQL-to-IQ translation
			functionsManifest + "strlang02", // STRLANG(?str,"en-US") is not supported by the SPARQL-to-IQ translation
			functionsManifest + "strlang03", // STRLANG(?str,"en-US") is not supported by the SPARQL-to-IQ translation

			functionsManifest + "timezone", // TIMEZONE(?date) is not supported by the SPARQL-to-IQ translation

			/* CONSTRUCT */
			// TODO: throw an exception for the FROM clause
			constructManifest + "constructwhere04", // CONSTRUCT FROM <data.ttl> not supported by the SPARQL-to-IQ translation

			/* NEGATION */
            negationManifest + "subset-by-exclusion-nex-1", // EXISTS not supported by the SPARQL-to-IQ translation
			negationManifest + "temporal-proximity-by-exclusion-nex-1", // EXISTS not supported by the SPARQL-to-IQ translation
			negationManifest + "subset-01", // EXISTS not supported by the SPARQL-to-IQ translation
			negationManifest + "subset-02", // EXISTS not supported by the SPARQL-to-IQ translation
			negationManifest + "set-equals-1", // EXISTS not supported by the SPARQL-to-IQ translation
			negationManifest + "subset-03", // EXISTS not supported by the SPARQL-to-IQ translation
			negationManifest + "exists-01", // EXISTS not supported by the SPARQL-to-IQ translation
			negationManifest + "exists-02", // EXISTS not supported by the SPARQL-to-IQ translation

			/* EXISTS */
			existsManifest + "exists01", // EXISTS not supported by the SPARQL-to-IQ translation
			existsManifest + "exists02", // EXISTS not supported by the SPARQL-to-IQ translation
			existsManifest + "exists03", // EXISTS not supported by the SPARQL-to-IQ translation
			existsManifest + "exists04", // EXISTS not supported by the SPARQL-to-IQ translation
			existsManifest + "exists05", // EXISTS not supported by the SPARQL-to-IQ translation

			/* PROPERTY PATH */
			propertyPathManifest + "pp02",  // ArbitraryLengthPath not supported
			propertyPathManifest + "pp07", // quads are not supported by the SI
			propertyPathManifest + "pp12", // ArbitraryLengthPath not supported
			propertyPathManifest + "pp14", // ArbitraryLengthPath not supported
			propertyPathManifest + "pp16", // ArbitraryLengthPath not supported
			propertyPathManifest + "pp21", // ArbitraryLengthPath not supported
			propertyPathManifest + "pp23", // ArbitraryLengthPath not supported
			propertyPathManifest + "pp25", // ArbitraryLengthPath not supported
			propertyPathManifest + "pp28a", // ZeroLengthPath not supported
			propertyPathManifest + "pp34", // ArbitraryLengthPath not supported
			propertyPathManifest + "pp35", // ArbitraryLengthPath not supported
			propertyPathManifest + "pp36", // ArbitraryLengthPath not supported
			propertyPathManifest + "pp37", // ArbitraryLengthPath not supported

			/* SERVICE not supported */
			serviceManifest + "service1",
			serviceManifest + "service2",
			serviceManifest + "service3",
			serviceManifest + "service4a",
			serviceManifest + "service5",
			serviceManifest + "service6",
			serviceManifest + "service7",

			/* SUBQUERY */
			subqueryManifest + "subquery01", // quads are not supported by the SI
			subqueryManifest + "subquery02", // quads are not supported by the SI
			subqueryManifest + "subquery03", // quads are not supported by the SI
			subqueryManifest + "subquery04", // quads are not supported by the SI
			subqueryManifest + "subquery05", // quads are not supported by the SI
			subqueryManifest + "subquery07", // quads are not supported by the SI

			subqueryManifest + "subquery10" // EXISTS not supported by the SPARQL-to-IQ translation
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
