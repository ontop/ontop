package it.unibz.inf.ontop.owlrefplatform.owlapi.example;

import com.google.inject.Injector;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlrefplatform.core.*;
import it.unibz.inf.ontop.injection.QuestComponentFactory;
import it.unibz.inf.ontop.injection.QuestCoreConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConnection;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLStatement;
import it.unibz.inf.ontop.reformulation.OBDAQueryProcessor;
import it.unibz.inf.ontop.sql.RDBMetadata;
import it.unibz.inf.ontop.sql.RDBMetadataExtractionTools;
import it.unibz.inf.ontop.sql.DatabaseRelationDefinition;
import it.unibz.inf.ontop.sql.QuotedIDFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.Optional;

/**
 * This class shows how to create an instance of quest giving the metadata manually 
 * 
 * @author mrezk, Christian 
 *
 */
public class ExampleManualMetadata {
final String owlfile = "src/main/resources/example/exampleSensor.owl";
final String obdafile = "src/main/resources/example/UseCaseExampleMini.obda";
private QuestOWLStatement qst = null;
	private DBConnector dbConnector;

	/*
     * 	prepare ontop for rewriting and unfolding steps
     */
private void setup()  throws Exception {
	/*
	 * Load the ontology from an external .owl file.
	 */
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

	QuestCoreConfiguration configuration = QuestCoreConfiguration.defaultBuilder()
			.nativeOntopMappingFile(obdafile)
			.dbMetadata(getMeta())
			.build();
	Injector injector = configuration.getInjector();
	QuestComponentFactory componentFactory = injector.getInstance(QuestComponentFactory.class);

	MappingParser mappingParser = injector.getInstance(MappingParser.class);
	OBDAModel obdaModel = mappingParser.parse(new File(obdafile));

	OBDAQueryProcessor queryProcessor = componentFactory.create(configuration.loadProvidedSpecification(),
			configuration.getExecutorRegistry());
	dbConnector = componentFactory.create(queryProcessor);
	dbConnector.connect();
	
	/*
	 * Prepare the data connection for querying.
	 */
	
	IQuestConnection conn =dbConnector.getConnection();
	QuestOWLConnection connOWL = new QuestOWLConnection(conn);
	qst = connOWL.createStatement();
}

private void defMeasTable(RDBMetadata dbMetadata, String name) {
	QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
	DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(idfac.createRelationID(null, name));
	tableDefinition.addAttribute(idfac.createAttributeID("timestamp"), java.sql.Types.TIMESTAMP, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("value"), java.sql.Types.NUMERIC, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("assembly"), java.sql.Types.VARCHAR, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("sensor"), java.sql.Types.VARCHAR, null, false);
}

private void defMessTable(RDBMetadata dbMetadata, String name) {
	QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
	DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(idfac.createRelationID(null, name));
	tableDefinition.addAttribute(idfac.createAttributeID("timestamp"), java.sql.Types.TIMESTAMP, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("eventtext"), java.sql.Types.VARCHAR, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("assembly"), java.sql.Types.VARCHAR, null, false);
}

private void defStaticTable(RDBMetadata dbMetadata, String name) {
	QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
	DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(idfac.createRelationID(null, name));
	tableDefinition.addAttribute(idfac.createAttributeID("domain"), java.sql.Types.VARCHAR, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("range"), java.sql.Types.VARCHAR, null, false);
}
private RDBMetadata getMeta(){
	RDBMetadata dbMetadata = RDBMetadataExtractionTools.createDummyMetadata();

	defMeasTable(dbMetadata, "burner");
	defMessTable(dbMetadata, "events");
	defStaticTable(dbMetadata, "a_static");
	return dbMetadata;
}

public void runQuery() throws Exception {
	setup();
	System.out.println("Good");
	dbConnector.close();
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