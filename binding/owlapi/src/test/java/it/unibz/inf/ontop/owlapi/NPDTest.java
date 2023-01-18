package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NPDTest {
	
	private static final String url = "jdbc:h2:mem:npdv";
	private static final String username = "sa";
	private static final String password = "";

	@Ignore("Ontology URIs are now broken: impossible to download them")
	@Test
	public void test_load_NPD() throws Exception {
		
		File ontDir = new File("src/test/resources/npd-v2");
		String path = ontDir.getAbsolutePath() + "/";
		String prfx = "file://" + path;
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://sws.ifi.uio.no/vocab/version/20130919/npd-bfo"), IRI.create(prfx + "npd-bfo.owl")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://sws.ifi.uio.no/vocab/version/20130919/npd-db"), IRI.create(prfx + "npd-db")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://sws.ifi.uio.no/vocab/version/20130919/npd-facility"), IRI.create(prfx + "npd-facility.owl")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://sws.ifi.uio.no/vocab/version/20130919/npd-geology"), IRI.create(prfx + "npd-geology.owl")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://sws.ifi.uio.no/vocab/version/20130919/npd-geometry"), IRI.create(prfx + "npd-geometry.owl")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://sws.ifi.uio.no/vocab/version/20130919/npd-org"), IRI.create(prfx + "npd-org.owl")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://sws.ifi.uio.no/vocab/version/20130919/npd-prod"), IRI.create(prfx + "npd-prod.owl")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://sws.ifi.uio.no/vocab/version/20130919/npd-seismic"), IRI.create(prfx + "npd-seismic.owl")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://sws.ifi.uio.no/vocab/version/20130919/npd-well"), IRI.create(prfx + "npd-well.owl")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://sws.ifi.uio.no/vocab/version/20130919/npd"), IRI.create(prfx + "npd.owl")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://sws.ifi.uio.no/vocab/version/20130919/sql"), IRI.create(prfx + "sql.owl")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://sws.ifi.uio.no/vocab/version/20130919/npd-isc-2012"), IRI.create(prfx + "npd-isc-2012.owl")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://www.opengis.net/ont/geosparql"), IRI.create(prfx + "geosparql_vocab_all.xml")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://www.opengis.net/ont/gml"), IRI.create(prfx + "gml_32_geometries.xml")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://www.opengis.net/ont/sf"), IRI.create(prfx + "gml_32_geometries.xml")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://purl.org/dc/elements/1.1/"), IRI.create(prfx + "dc.xml")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://www.w3.org/2004/02/skos/core"), IRI.create(prfx + "skos.xml")));
		manager.addIRIMapper(new SimpleIRIMapper(
				IRI.create("http://www.ifomis.org/bfo/owl"), IRI.create(prfx + "bfo-1.1.owl")));
		 	
			
		OWLOntology owlOnto = manager.loadOntologyFromOntologyDocument(new File(path + "npd-v2.owl")); 
/*		
		Ontology onto = OWLAPI3TranslatorUtility.translateImportsClosure(owlOnto);
		// - 2 to account for top and bot
		System.out.println("Class names: " + (onto.getVocabulary().getClasses().size() - 2));
		System.out.println("Object Property names: " + (onto.getVocabulary().getObjectProperties().size() - 2));
		System.out.println("Data Property names: " + (onto.getVocabulary().getDataProperties().size() - 2));
*/

		setupDatabase();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(path + "npd.obda")
				.ontologyFile(path + "npd-v2.owl")
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableExistentialReasoning(true)
				.enableTestMode()
				.build();
		OntopOWLEngine reasoner = new SimpleOntopOWLEngine(config);
		
		//QuestOWL reasoner = factory.createReasoner(owlOnto, new SimpleConfiguration());
	

			String q12 = 	"PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>" +
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
					"PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>" +
					"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
					"PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>" +
					"SELECT DISTINCT ?unit ?well " +
					"WHERE { " +
					"[] npdv:wellboreStratumTopDepth     ?stratTop ; " +
				    " npdv:wellboreStratumBottomDepth  ?stratBottom ; " +
				    " npdv:stratumForWellbore          ?wellboreURI ; " +
				    " npdv:inLithostratigraphicUnit [ npdv:name ?unit ] . "+
				    "?wellboreURI npdv:name ?well . " +
				    " "+
				    "?core " + // "a npdv:WellboreCoreSample ; " +
					"npdv:coreForWellbore ?wellboreURI ." +
					"" +
					"{ ?core npdv:coreIntervalUOM \"m\"^^xsd:string ; " +
				    "	  npdv:coreIntervalTop     ?coreTopM ;" +
				    " 	  npdv:coreIntervalBottom  ?coreBottomM ;" +
				    "BIND(?coreTopM    AS ?coreTop) " +
				    "BIND(?coreBottomM AS ?coreBottom) " +
				    "}" +
				    "UNION " +
				    "{ ?core npdv:coreIntervalUOM \"ft\"^^xsd:string ; " +
				    "	  npdv:coreIntervalTop     ?coreTopFT ; " +
				    " 	  npdv:coreIntervalBottom  ?coreBottomFT ; " +
				    "BIND((?coreTopFT    * 0.3048) AS ?coreTop) " +
				    "BIND((?coreBottomFT * 0.3048) AS ?coreBottom) " +
				    "} " +
	                "" +
//				    "BIND(IF(?coreTop > ?stratTop, ?coreTop, ?stratTop) AS ?max) " + 
//				    "BIND(IF(?coreBottom < ?stratBottom, ?coreBottom, ?stratBottom) AS ?min) " +
//				    " " +
//				    "FILTER(?max < ?min) " +
				    "} ORDER BY ?unit ?well";

			OntopOWLConnection qconn =  reasoner.getConnection();
			OntopOWLStatement st = qconn.createStatement();
			
			st.getExecutableQuery(q12);
			st.close();
		
			//System.out.println(unfolding);
	}
	
	
	
	public void setupDatabase() throws SQLException, IOException {
		// String driver = "org.h2.Driver";

		Connection conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		int i = 0;
		
		FileReader reader = new FileReader("src/test/resources/npd-v2/npd-schema.sql");
		StringBuilder bf = new StringBuilder();
		try (BufferedReader in = new BufferedReader(reader)) {
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				bf.append(line).append("\n");
				if (line.startsWith("--")) {
					//System.out.println("EXECUTING " + i++ + ":\n" + bf.toString());
					st.executeUpdate(bf.toString());
					conn.commit();
					bf = new StringBuilder();
				}
			}
		}
		
		DatabaseMetaData md = conn.getMetaData();
		try (ResultSet rsTables = md.getTables(null, null, null, new String[] { "TABLE", "VIEW" })) {
			int tbl = 0;
			while (rsTables.next()) {
				final String tblName = rsTables.getString("TABLE_NAME");
				tbl++;
				//System.out.println("Table " + tbl + ": " + tblName);
			}
			assertEquals(tbl, 70);
		}
		
		List<String> pk = new LinkedList<>();
		try (ResultSet rsPrimaryKeys = md.getPrimaryKeys(null, null, "FIELD_DESCRIPTION")) {
			while (rsPrimaryKeys.next()) {
				String colName = rsPrimaryKeys.getString("COLUMN_NAME");
				String pkName = rsPrimaryKeys.getString("PK_NAME");
				if (pkName != null) {
					pk.add(colName);
				}
			}
		} 
		//System.out.println(pk);

		
		try (ResultSet rsIndexes = md.getIndexInfo(null, null, "WELLBORE_CORE", true, true)) {
			while (rsIndexes.next()) {
				String colName = rsIndexes.getString("COLUMN_NAME");
				String indName = rsIndexes.getString("INDEX_NAME");
				boolean nonUnique = rsIndexes.getBoolean("NON_UNIQUE");
				//System.out.println(indName + " " +colName + " " + nonUnique);
			}
		} 
		
		System.out.println("Database schema created successfully");
	}
	
}
