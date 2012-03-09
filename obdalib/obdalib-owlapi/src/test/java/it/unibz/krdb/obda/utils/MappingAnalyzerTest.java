package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class MappingAnalyzerTest extends TestCase {

	private OBDAModel obdaModel;

	public void testBooks() throws Exception {

		setup("src/test/resources/scenario/private/books-obdalin-pgsql.owl");

		URI sourceId = obdaModel.getSources().get(0).getSourceID();

		ArrayList<OBDAMappingAxiom> mappingList = obdaModel.getMappings(sourceId);
		DBMetadata metadata = JDBCConnectionManager.getJDBCConnectionManager().getMetaData(obdaModel.getSources().get(0));

		MappingAnalyzer analyzer = new MappingAnalyzer(mappingList, metadata);
		DatalogProgram dp = analyzer.constructDatalogProgram();

		System.out.println(dp.toString());
	}

	public void testStockExchange() throws Exception {

		setup("src/test/resources/scenario/private/stockexchange-obdalin-pgsql.owl");

		URI sourceId = obdaModel.getSources().get(0).getSourceID();

		ArrayList<OBDAMappingAxiom> mappingList = obdaModel.getMappings(sourceId);
		DBMetadata metadata = JDBCConnectionManager.getJDBCConnectionManager().getMetaData(obdaModel.getSources().get(0));

		MappingAnalyzer analyzer = new MappingAnalyzer(mappingList, metadata);
		DatalogProgram dp = analyzer.constructDatalogProgram();

		System.out.println(dp.toString());
	}

	public void testImdb() throws Exception {

		setup("src/test/resources/scenario/private/imdb-obdalin-pgsql.owl");

		URI sourceId = obdaModel.getSources().get(0).getSourceID();

		ArrayList<OBDAMappingAxiom> mappingList = obdaModel.getMappings(sourceId);
		DBMetadata metadata = JDBCConnectionManager.getJDBCConnectionManager().getMetaData(obdaModel.getSources().get(0));

		MappingAnalyzer analyzer = new MappingAnalyzer(mappingList, metadata);
		DatalogProgram dp = analyzer.constructDatalogProgram();

		System.out.println(dp.toString());
	}

	private void setup(String inputFile) throws Exception {

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		URI owlInput = new File(inputFile).toURI();
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI(owlInput);

		// Loading the OBDA data.
		OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();
		obdaModel = dataFactory.getOBDAModel();

		String obdaFile = inputFile.substring(0, inputFile.length() - 3) + "obda";
		URI obdaInput = new File(obdaFile).toURI();
		DataManager dataManager = new DataManager(obdaModel);

		dataManager.loadOBDADataFromURI(obdaInput, ontology.getURI(), obdaModel.getPrefixManager());
		for (OBDADataSource source : obdaModel.getSources()) {
			JDBCConnectionManager.getJDBCConnectionManager().createConnection(source); // new
																							// call
		}
	}
}
