package it.unibz.inf.ontop.docker.mssql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/***
 * Tests that SQL Server returns the datetimes correctly
 */
public class AdventureWorksDatetimeTest extends AbstractVirtualModeTest {

	Logger log = LoggerFactory.getLogger(this.getClass());

	static final String owlFile = "/mssql/adventureWorks.owl";
	static final String obdaFile = "/mssql/adventureWorks.obda";
	static final String propertiesFile = "/mssql/adventureWorks.properties";

	private static OntopOWLReasoner REASONER;
	private static OntopOWLConnection CONNECTION;

	@BeforeClass
	public static void before() throws OWLOntologyCreationException {
		REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
		CONNECTION = REASONER.getConnection();
	}

	@Override
	protected OntopOWLStatement createStatement() throws OWLException {
		return CONNECTION.createStatement();
	}

	@AfterClass
	public static void after() throws OWLException {
		CONNECTION.close();
		REASONER.dispose();
	}

    /**
	 * NB: In the SQL server source, date are stored as DATETIME -> no time offset support
	 */
	@Test
	public void testDatetime() throws Exception {

		String query =  "PREFIX : <http://knova.ru/adventureWorks.owl#>\n" +
				"SELECT DISTINCT ?x ?y { ?y :SpecialOffer_ModifiedDate ?x }";
		String val = runQueryAndReturnStringOfLiteralX(query);
		assertEquals("\"2005-05-02T00:00:00\"^^xsd:dateTime", val);
	}





}

