package it.unibz.krdb.obda.owlrefplatform.owlapi3.example;








import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.sql.Attribute;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.TableDefinition;

import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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
	Quest qest = new Quest(OWLAPI3TranslatorUtility.translateImportsClosure(ontology), obdaModel, dbMetadata, preference);
	qest.setupRepository();
	
	/*
	 * Prepare the data connection for querying.
	 */
	
	QuestConnection conn =qest.getConnection();
	QuestOWLConnection connOWL = new QuestOWLConnection(conn);
	qst = connOWL.createStatement();
}

private TableDefinition defMeasTable(String name){
	TableDefinition tableDefinition = new TableDefinition(name);
	Attribute attribute = null;
	attribute = new Attribute("timestamp", java.sql.Types.TIMESTAMP, null, false, null);
	//It starts from 1 !!!
	tableDefinition.addAttribute(attribute);
	attribute = new Attribute("value", java.sql.Types.NUMERIC, null, false, null);
	tableDefinition.addAttribute(attribute);
	attribute = new Attribute("assembly", java.sql.Types.VARCHAR, null, false, null);
	tableDefinition.addAttribute(attribute);
	attribute = new Attribute("sensor", java.sql.Types.VARCHAR, null, false, null);
	tableDefinition.addAttribute(attribute);
	return tableDefinition;
}

private TableDefinition defMessTable(String name){
	TableDefinition tableDefinition = new TableDefinition(name);
	Attribute attribute = null;
	//It starts from 1 !!!
	attribute = new Attribute("timestamp", java.sql.Types.TIMESTAMP, null, false, null);
	tableDefinition.addAttribute(attribute);
	attribute = new Attribute("eventtext", java.sql.Types.VARCHAR, null, false, null);
	tableDefinition.addAttribute(attribute);
	attribute = new Attribute("assembly", java.sql.Types.VARCHAR, null, false, null);
	tableDefinition.addAttribute(attribute);
	return tableDefinition;
}

private TableDefinition defStaticTable(String name){
	TableDefinition tableDefinition = new TableDefinition(name);
	Attribute attribute = null;
	//It starts from 1 !!!
	attribute = new Attribute("domain", java.sql.Types.VARCHAR, null, false, null);
	tableDefinition.addAttribute(attribute);
	attribute = new Attribute("range", java.sql.Types.VARCHAR, null, false, null);
	tableDefinition.addAttribute(attribute);
	return tableDefinition;
}
private DBMetadata getMeta(){
	DBMetadata dbMetadata = new DBMetadata("dummy class");
	dbMetadata.add(defMeasTable("burner"));
	dbMetadata.add(defMessTable("events"));
	dbMetadata.add(defStaticTable("a_static"));
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