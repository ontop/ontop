package it.unibz.inf.ontop.sesame;

/*
 * #%L
 * ontop-quest-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Guice;
import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDACoreModule;
import it.unibz.inf.ontop.injection.OBDAProperties;
import it.unibz.inf.ontop.io.QueryIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.RDBMSourceParameterConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.querymanager.QueryController;
import it.unibz.inf.ontop.querymanager.QueryControllerEntity;
import it.unibz.inf.ontop.querymanager.QueryControllerQuery;
import it.unibz.inf.ontop.sql.JDBCConnectionManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.util.Properties;


/***
 * Tests if QuestOWL can be initialized on top of an existing semantic index
 * created by the SemanticIndexManager.
 * 
 * @author mariano
 * 
 */
public class SemanticIndexCMD {

	String driver = "com.mysql.jdbc.Driver";
	String url = "jdbc:mysql://localhost/lubmex2050?sessionVariables=sql_mode='ANSI'";
	String username = "root";
	String password = "";

	String owlfile = "src/test/resources/test/lubm-ex-20-uni1/merge.owl";

	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OBDADataSource source;
	private final NativeQueryLanguageComponentFactory nativeQLFactory;

	Logger log = LoggerFactory.getLogger(this.getClass());

	public SemanticIndexCMD(String configFile) throws Exception {
		
		
		
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		source = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP1testx1"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "false");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Injector injector = Guice.createInjector(new OBDACoreModule(new QuestPreferences()));
		nativeQLFactory = injector.getInstance(NativeQueryLanguageComponentFactory.class);

	}

	public void setupRepo() throws Exception {
		log.info("Creating a new repository");
		Connection conn = null;
		try {
			conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);

			SemanticIndexManager simanager = new SemanticIndexManager(ontology, conn, nativeQLFactory);

			simanager.setupRepository(true);

		} catch (Exception e) {
			throw e;
		} finally {
			if (conn != null)
				conn.close();
		}
		log.info("Done.");
	}

	public void inserData() throws Exception {
		log.debug("Inserting data");
		Connection conn = null;
		
		ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));
		
		try {
			conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);

			SemanticIndexManager simanager = new SemanticIndexManager(ontology, conn, nativeQLFactory);

			simanager.restoreRepository();

			int inserts = simanager.insertData(ontology, 20000, 5000);
			
			simanager.updateMetadata();

			log.info("Done. Inserted {} triples.", inserts);
			
			
//			assertEquals(30033, inserts);
		} catch (Exception e) {
			throw e;
		} finally {
			if (conn != null)
				conn.close();
		}
		
		

	}

	public void test3InitializingQuest() throws Exception {
		Properties p = new Properties();
        p.setProperty(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
        p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
        p.setProperty(QuestPreferences.STORAGE_LOCATION, QuestConstants.JDBC);
        p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
        p.setProperty(OBDAProperties.JDBC_DRIVER, driver);
        p.setProperty(OBDAProperties.JDBC_URL, url);
        p.setProperty(OBDAProperties.DB_USER, username);
        p.setProperty(OBDAProperties.DB_PASSWORD, password);

		QuestOWLFactory fac = new QuestOWLFactory();
		QuestOWLConfiguration config = QuestOWLConfiguration.builder()
				.properties(p).build();

		QuestOWL quest = fac.createReasoner(ontology, config);

		QuestOWLConnection qconn = quest.getConnection();

		QuestOWLStatement st = qconn.createStatement();

		QueryController qc = new QueryController();
		QueryIOManager qman = new QueryIOManager(qc);
		qman.load("src/test/resources/test/treewitness/LUBM-ex-20.q");

		for (QueryControllerEntity e : qc.getElements()) {
			if (!(e instanceof QueryControllerQuery)) {
				continue;
			}
			QueryControllerQuery query = (QueryControllerQuery) e;
			log.debug("Executing query: {}", query.getID() );
			log.debug("Query: \n{}", query.getQuery());
			
			long start = System.nanoTime();
			QuestOWLResultSet res = st.executeTuple(query.getQuery());
			long end = System.nanoTime();
			
			double time = (end - start) / 1000; 
			
			int count = 0;
			while (res.nextRow()) {
				count += 1;
			}
			log.debug("Total result: {}", count );
			log.debug("Elapsed time: {} ms", time);
		}
	}

}
