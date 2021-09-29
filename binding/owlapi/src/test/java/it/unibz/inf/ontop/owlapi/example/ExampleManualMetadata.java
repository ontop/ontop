package it.unibz.inf.ontop.owlapi.example;

import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.injection.CoreSingletons;
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

	private static final String owlfile = "src/test/resources/example/exampleSensor.owl";
	private static final String obdafile = "src/test/resources/example/UseCaseExampleMini.obda";
	private static final String propertyfile = "src/test/resources/example/UseCaseExampleMini.properties";

	private OWLStatement qst;
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

		queryEngine = engineFactory.create(configuration.loadSpecification());
		queryEngine.connect();

		/*
		 * Prepare the data connection for querying.
		 */

		OntopConnection conn = queryEngine.getConnection();
		OWLConnection connOWL = new DefaultOntopOWLConnection(conn, injector.getInstance(InputQueryFactory.class));
		qst = connOWL.createStatement();
	}

	private static void defMeasTable(OfflineMetadataProviderBuilder builder) {
		builder.createDatabaseRelation("burner",
				"timestamp", builder.getDBTypeFactory().getDBDateTimestampType(), false,
				"value", builder.getDBTypeFactory().getDBDoubleType(), false,
				"assembly", builder.getDBTypeFactory().getDBDoubleType(), false,
				"sensor", builder.getDBTypeFactory().getDBDoubleType(), false);
	}

	private static void defMessTable(OfflineMetadataProviderBuilder builder) {
		builder.createDatabaseRelation("events",
				"timestamp", builder.getDBTypeFactory().getDBDateTimestampType(), false,
				"eventtext", builder.getDBTypeFactory().getDBDoubleType(), false,
				"assembly", builder.getDBTypeFactory().getDBDoubleType(), false);
	}

	private static void defStaticTable(OfflineMetadataProviderBuilder builder) {
		builder.createDatabaseRelation("a_static",
				"domain", builder.getDBTypeFactory().getDBDoubleType(), false,
				"range", builder.getDBTypeFactory().getDBDoubleType(), false);
	}

	private static MetadataProvider getMeta(){
		OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder().build();
		OfflineMetadataProviderBuilder builder = new OfflineMetadataProviderBuilder(defaultConfiguration.getInjector()
				.getInstance(CoreSingletons.class));

		defMeasTable(builder);
		defMessTable(builder);
		defStaticTable(builder);
		return builder.build();
	}

	public void runQuery() throws Exception {
		setup();
		System.out.println("Good");
		queryEngine.close();
	}
}