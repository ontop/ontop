package it.unibz.krdb.obda.owlrefplatform.owlapi3.example;


import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPITranslatorUtility;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DBMetadataExtractor;
import it.unibz.krdb.sql.DatabaseRelationDefinition;
import it.unibz.krdb.sql.QuotedIDFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
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

/*
 * 	prepare ontop for rewriting and unfolding steps 
 */
private void setup()  throws Exception {
	/*
	 * Load the ontology from an external .owl file.
	 */
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

	/*
	 * Load the OBDA model from an external .obda file
	 */
	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	OBDAModel obdaModel = fac.getOBDAModel();
	ModelIOManager ioManager = new ModelIOManager(obdaModel);
	ioManager.load(obdafile);
	
	/*
	 * Prepare the configuration for the Quest instance. The example below shows the setup for
	 * "Virtual ABox" mode
	 */
	QuestPreferences preference = new QuestPreferences();
	preference.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
	DBMetadata dbMetadata = getMeta();
	Quest qest = new Quest(OWLAPITranslatorUtility.translateImportsClosure(ontology), obdaModel, dbMetadata, preference);
	qest.setupRepository();
	
	/*
	 * Prepare the data connection for querying.
	 */
	
	QuestConnection conn =qest.getConnection();
	QuestOWLConnection connOWL = new QuestOWLConnection(conn);
	qst = connOWL.createStatement();
}

private void defMeasTable(DBMetadata dbMetadata, String name) {
	QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
	DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(idfac.createRelationID(null, name));
	tableDefinition.addAttribute(idfac.createAttributeID("timestamp"), java.sql.Types.TIMESTAMP, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("value"), java.sql.Types.NUMERIC, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("assembly"), java.sql.Types.VARCHAR, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("sensor"), java.sql.Types.VARCHAR, null, false);
}

private void defMessTable(DBMetadata dbMetadata, String name) {
	QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
	DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(idfac.createRelationID(null, name));
	tableDefinition.addAttribute(idfac.createAttributeID("timestamp"), java.sql.Types.TIMESTAMP, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("eventtext"), java.sql.Types.VARCHAR, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("assembly"), java.sql.Types.VARCHAR, null, false);
}

private void defStaticTable(DBMetadata dbMetadata, String name) {
	QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
	DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(idfac.createRelationID(null, name));
	tableDefinition.addAttribute(idfac.createAttributeID("domain"), java.sql.Types.VARCHAR, null, false);
	tableDefinition.addAttribute(idfac.createAttributeID("range"), java.sql.Types.VARCHAR, null, false);
}
private DBMetadata getMeta(){
	DBMetadata dbMetadata = DBMetadataExtractor.createDummyMetadata();

	defMeasTable(dbMetadata, "burner");
	defMessTable(dbMetadata, "events");
	defStaticTable(dbMetadata, "a_static");
	return dbMetadata;
}

public void runQuery() throws Exception {
	setup();
	System.out.println("Good");
	
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