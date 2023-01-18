package it.unibz.inf.ontop.docker.mssql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import static org.junit.Assert.assertEquals;

/***
 * Tests that jtds jdbc driver for SQL Server returns the datatypes correctly
 */
public class JTDSDatatypesTest extends AbstractVirtualModeTest {

	private static final String owlfile = "/mssql/datatype/datatypesjtds.owl";
	private static final String obdafile = "/mssql/datatype/datatypejtds.obda";
	private static final String propertiesfile = "/mssql/datatype/datatypejtds.properties";

	private static EngineConnection CONNECTION;

	@BeforeClass
	public static void before()  {
		CONNECTION = createReasoner(owlfile, obdafile, propertiesfile);
	}

	@Override
	protected OntopOWLStatement createStatement() throws OWLException {
		return CONNECTION.createStatement();
	}

	@AfterClass
	public static void after() throws Exception {
		CONNECTION.close();
	}


	/**
	 * Test use of datetime with jtds driver
	 *
	 * NB: no timezone stored in the DB (DATETIME column type)
	 *
	 */
	@Test
	public void testDatetime() throws Exception {

		String query =  "PREFIX : <http://knova.ru/adventureWorks.owl#>\n" +
				"SELECT DISTINCT ?s ?x { ?s :SpecialOffer_ModifiedDate ?x }";
		String val = runQueryAndReturnStringOfLiteralX(query);
		assertEquals("\"2005-05-02T00:00:00\"^^xsd:dateTime", val);
	}





}

