package it.unibz.inf.ontop.owlapi.example;

import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.impl.DefaultOntopOWLConnection;

/**
 * This class shows how to create an instance of quest giving the metadata manually
 * 
 * @author mrezk, Christian 
 *
 */
public class ExampleManualMetadata {
	final String owlfile = "src/test/resources/example/exampleSensor.owl";
	final String obdafile = "src/test/resources/example/UseCaseExampleMini.obda";
	final String propertyfile = "src/test/resources/example/UseCaseExampleMini.properties";
	private OWLStatement qst = null;
	private OntopQueryEngine queryEngine;

	/*
     * 	prepare ontop for rewriting and unfolding steps
     */
private void setup()  throws Exception {

	OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
			.nativeOntopMappingFile(obdafile)
			.ontologyFile(owlfile)
			.propertyFile(propertyfile)
			.dbMetadata(getMeta())
			.enableTestMode()
			.build();
	Injector injector = configuration.getInjector();
	OntopSystemFactory engineFactory = injector.getInstance(OntopSystemFactory.class);

	queryEngine = engineFactory.create(configuration.loadSpecification(),
			configuration.getExecutorRegistry());
	queryEngine.connect();
	
	/*
	 * Prepare the data connection for querying.
	 */
	
	OntopConnection conn = queryEngine.getConnection();
	OWLConnection connOWL = new DefaultOntopOWLConnection(conn, injector.getInstance(InputQueryFactory.class));
	qst = connOWL.createStatement();
}

private static void defMeasTable(RDBMetadata dbMetadata, String name) {
	QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
	DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(idfac.createRelationID(null, name));
	tableDefinition.addAttribute(idfac.createAttributeID("timestamp"), java.sql.Types.TIMESTAMP, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("value"), java.sql.Types.NUMERIC, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("assembly"), java.sql.Types.VARCHAR, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("sensor"), java.sql.Types.VARCHAR, null, false);
}

private static void defMessTable(RDBMetadata dbMetadata, String name) {
	QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
	DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(idfac.createRelationID(null, name));
	tableDefinition.addAttribute(idfac.createAttributeID("timestamp"), java.sql.Types.TIMESTAMP, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("eventtext"), java.sql.Types.VARCHAR, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("assembly"), java.sql.Types.VARCHAR, null, false);
}

private static void defStaticTable(RDBMetadata dbMetadata, String name) {
	QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
	DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(idfac.createRelationID(null, name));
	tableDefinition.addAttribute(idfac.createAttributeID("domain"), java.sql.Types.VARCHAR, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("range"), java.sql.Types.VARCHAR, null, false);
}
private static RDBMetadata getMeta(){
	OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder().build();
	Injector defaultInjector = defaultConfiguration.getInjector();


	RDBMetadata dbMetadata = defaultInjector.getInstance(DummyRDBMetadata.class);

	defMeasTable(dbMetadata, "burner");
	defMessTable(dbMetadata, "events");
	defStaticTable(dbMetadata, "a_static");
	return dbMetadata;
}

public void runQuery() throws Exception {
	setup();
	System.out.println("Good");
	queryEngine.close();
}

public static void main(String[] args) {
	try {
		ExampleManualMetadata example = new ExampleManualMetadata();
		example.runQuery();
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}