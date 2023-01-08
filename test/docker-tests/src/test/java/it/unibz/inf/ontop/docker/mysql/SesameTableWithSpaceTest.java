package it.unibz.inf.ontop.docker.mysql;


import it.unibz.inf.ontop.docker.AbstractRDF4JVirtualModeTest;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;


public class SesameTableWithSpaceTest extends AbstractRDF4JVirtualModeTest {

	private static final String owlFile = "/mysql/northwind/1.4a.owl";
	private static final String ttlFile = "/mysql/northwind/mapping-northwind-1421066727259.ttl";
	private static final String propertyFile = "/mysql/northwind/northwind.properties";

	private static RepositoryConnection con;

	@BeforeClass
	public static void setUp() {
		Repository repo = createR2RMLReasoner(owlFile, ttlFile, propertyFile);
		con = repo.getConnection();
	}

	@AfterClass
	public static void tearDown() {
		if (con != null && con.isOpen()) {
			con.close();
		}
	}
	

	@Test
	public void test1() {
		//read next query
		String sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/> select * {?x a :OrderDetails}" ;

		int obtainedResult = extecuteQueryAndGetCount(con, sparqlQuery);
		assertEquals(2155, obtainedResult);
	}

}
