package org.semanticweb.ontop.owlrefplatform.owlapi3.example;








import com.google.inject.Guice;
import com.google.inject.Injector;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.owlrefplatform.core.*;
import org.semanticweb.ontop.owlrefplatform.injection.QuestComponentFactory;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.owlapi3.OWLAPI3TranslatorUtility;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.TableDefinition;
import org.semanticweb.ontop.sql.api.Attribute;

import java.io.File;
import java.io.FileReader;
import java.util.*;

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
private OWLAPI3TranslatorUtility translator = new OWLAPI3TranslatorUtility();
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

    /**
     * Factory initialization
     */
    Injector injector = Guice.createInjector(new OBDACoreModule(new OBDAProperties()));
    NativeQueryLanguageComponentFactory factory = injector.getInstance(NativeQueryLanguageComponentFactory.class);

	/*
	 * Load the OBDA model from an external .obda file
	 */
    MappingParser mappingParser = factory.create(new FileReader(obdafile));
    OBDAModel obdaModel = mappingParser.getOBDAModel();
	
	/*
	 * Prepare the configuration for the Quest instance. The example below shows the setup for
	 * "Virtual ABox" mode
	 */
	Properties p = new Properties();
	p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
	QuestPreferences preference = new QuestPreferences();
	DBMetadata dbMetadata = getMeta();

    QuestComponentFactory componentFactory = injector.getInstance(QuestComponentFactory.class);
	IQuest quest = componentFactory.create(getOntologyFromOWLOntology(ontology),
            obdaModel, dbMetadata, preference);
	quest.setupRepository();
	
	/*
	 * Prepare the data connection for querying.
	 */
	
	IQuestConnection conn = (IQuestConnection) quest.getConnection();
	QuestOWLConnection connOWL = new QuestOWLConnection(conn);
	qst = connOWL.createStatement();
}

private TableDefinition defMeasTable(String name){
	TableDefinition tableDefinition = new TableDefinition(name);
	Attribute attribute = null;
	attribute = new Attribute("timestamp", java.sql.Types.TIMESTAMP, false, null);
	//It starts from 1 !!!
	tableDefinition.setAttribute(1, attribute);
	attribute = new Attribute("value", java.sql.Types.NUMERIC, false, null);
	tableDefinition.setAttribute(2, attribute);
	attribute = new Attribute("assembly", java.sql.Types.VARCHAR, false, null);
	tableDefinition.setAttribute(3, attribute);
	attribute = new Attribute("sensor", java.sql.Types.VARCHAR, false, null);
	tableDefinition.setAttribute(4, attribute);
	return tableDefinition;
}

private TableDefinition defMessTable(String name){
	TableDefinition tableDefinition = new TableDefinition(name);
	Attribute attribute = null;
	//It starts from 1 !!!
	attribute = new Attribute("timestamp", java.sql.Types.TIMESTAMP, false, null);
	tableDefinition.setAttribute(1, attribute);
	attribute = new Attribute("eventtext", java.sql.Types.VARCHAR, false, null);
	tableDefinition.setAttribute(2, attribute);
	attribute = new Attribute("assembly", java.sql.Types.VARCHAR, false, null);
	tableDefinition.setAttribute(3, attribute);
	return tableDefinition;
}

private TableDefinition defStaticTable(String name){
	TableDefinition tableDefinition = new TableDefinition(name);
	Attribute attribute = null;
	//It starts from 1 !!!
	attribute = new Attribute("domain", java.sql.Types.VARCHAR, false, null);
	tableDefinition.setAttribute(1, attribute);
	attribute = new Attribute("range", java.sql.Types.VARCHAR, false, null);
	tableDefinition.setAttribute(2, attribute);
	return tableDefinition;
}
private DBMetadata getMeta(){
	DBMetadata dbMetadata = new DBMetadata();
	dbMetadata.add(defMeasTable("burner"));
	dbMetadata.add(defMessTable("events"));
	dbMetadata.add(defStaticTable("a_static"));
	return dbMetadata;
}
private Ontology getOntologyFromOWLOntology(OWLOntology owlontology) throws Exception{
	//compute closure first (owlontology might contain include other source declarations)
	Set<OWLOntology> clousure = owlontology.getOWLOntologyManager().getImportsClosure(owlontology);
	return translator.mergeTranslateOntologies(clousure);
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