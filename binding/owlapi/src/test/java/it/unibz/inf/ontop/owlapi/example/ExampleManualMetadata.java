package it.unibz.inf.ontop.owlapi.example;

import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
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

private static void defMeasTable(BasicDBMetadata dbMetadata, DBTypeFactory dbTypeFactory, String name) {
	QuotedIDFactory idfac = dbMetadata.getDBParameters().getQuotedIDFactory();
	DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(new RelationDefinition.AttributeListBuilder(idfac.createRelationID(null, name))
		.addAttribute(idfac.createAttributeID("timestamp"), dbTypeFactory.getDBDateTimestampType(), false)
		.addAttribute(idfac.createAttributeID("value"), dbTypeFactory.getDBDoubleType(), false)
		.addAttribute(idfac.createAttributeID("assembly"), dbTypeFactory.getDBDoubleType(), false)
		.addAttribute(idfac.createAttributeID("sensor"), dbTypeFactory.getDBDoubleType(), false));
}

private static void defMessTable(BasicDBMetadata dbMetadata, DBTypeFactory dbTypeFactory, String name) {
	QuotedIDFactory idfac = dbMetadata.getDBParameters().getQuotedIDFactory();
	DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(new RelationDefinition.AttributeListBuilder(idfac.createRelationID(null, name))
		.addAttribute(idfac.createAttributeID("timestamp"), dbTypeFactory.getDBDateTimestampType(), false)
		.addAttribute(idfac.createAttributeID("eventtext"), dbTypeFactory.getDBDoubleType(), false)
		.addAttribute(idfac.createAttributeID("assembly"), dbTypeFactory.getDBDoubleType(), false));
}

private static void defStaticTable(BasicDBMetadata dbMetadata, DBTypeFactory dbTypeFactory, String name) {
	QuotedIDFactory idfac = dbMetadata.getDBParameters().getQuotedIDFactory();
	DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(new RelationDefinition.AttributeListBuilder(idfac.createRelationID(null, name))
		.addAttribute(idfac.createAttributeID("domain"), dbTypeFactory.getDBDoubleType(), false)
		.addAttribute(idfac.createAttributeID("range"), dbTypeFactory.getDBDoubleType(), false));
}
private static BasicDBMetadata getMeta(){
	OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder().build();
	Injector defaultInjector = defaultConfiguration.getInjector();

	BasicDBMetadata dbMetadata = defaultInjector.getInstance(BasicDBMetadata.class);
	DBTypeFactory dbTypeFactory = defaultConfiguration.getTypeFactory().getDBTypeFactory();

	defMeasTable(dbMetadata, dbTypeFactory,"burner");
	defMessTable(dbMetadata, dbTypeFactory,"events");
	defStaticTable(dbMetadata, dbTypeFactory,"a_static");
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