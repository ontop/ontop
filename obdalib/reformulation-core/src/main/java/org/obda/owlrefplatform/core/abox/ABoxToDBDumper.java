package org.obda.owlrefplatform.core.abox;

import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.exception.NoDatasourceSelectedException;
import inf.unibz.it.obda.gui.swing.utils.OBDAProgressListener;
import inf.unibz.it.sql.JDBCConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class dumps the abox of a given ontology into a data base.
 *
 * @author Manfred Gerstgrasser
 *
 */

// TODO ABoxDumper document in the class description the schemas created by this
// class (table, indexes, etc)
// TODO ABoxDumper document in the class description the mappings that are
// created by this class
// TODO ABoxDumper document in the schema when and how the data is inserted into
// the schema

public class ABoxToDBDumper implements OBDAProgressListener{

	private Connection							conn					= null;
//	private APIController						apic					= null;
	private List<ABoxDumpListener>				listener				= null;
	private DataSource							ds						= null;
	private int									indexcounter			= 1;
	
	private boolean								isCanceled = false;					
	private Statement 							statement = null;

//	private static ABoxToDBDumper				instance				= null;
	
	private Map<URIIdentyfier,String> 	mapper 		= null;
	
	private final Logger								log						= LoggerFactory.getLogger(ABoxToDBDumper.class);

	public ABoxToDBDumper(DataSource ds) {
		this.ds = ds;
		listener = new Vector<ABoxDumpListener>();
//		instance = this;
	}

//	public void setAPIController(APIController apic) {
//		this.apic = apic;
//	}

	/**
	 * Materializes the Abox of the given ontologies using the given sql
	 * connection.
	 *
	 * @param ontologies
	 *            the ontolgies
	 * @param c
	 *            the sql connection
	 * @param dsUri
	 *            the data source identifier, can be null if createMapping is
	 *            false
	 * @param createMappings
	 *            true if automatic mappings should be made
	 * @throws Exception
	 */
	private void materialize(Set<OWLOntology> ontologies, Connection c) throws AboxDumpException {
		try {
			log.debug("Materializing ABoxes into DB");
			isCanceled = false;
			
			Set<String>	createTableSQLs			=  new HashSet<String>();;
			HashMap<String, List<List<String>>>	inserts	= new HashMap<String, List<List<String>>>();
			Set<String>	createIndexSQL			= new HashSet<String>();
			
			mapper = new HashMap<URIIdentyfier,String>();
			
			int conceptCounter = 1;
			int datapropCounter = 1;
			int objectpropCounter = 1;
	
			conn = c;
	
			Iterator<OWLOntology> it = ontologies.iterator();
			while(it.hasNext()){	
				OWLOntology ontology = it.next();
				log.debug("Materializing ABox for ontology: {}", ontology.getURI().toString());
				Set<OWLEntity> entities = ontology.getSignature();
				Iterator<OWLEntity> entityIterator = entities.iterator();
					
				while (entityIterator.hasNext()) {
						/* For each entity */
					OWLEntity entity = entityIterator.next();
		
					if (entity instanceof OWLClass) {
						OWLClass clazz = (OWLClass) entity;
						if (!clazz.isOWLThing()) {
							URIIdentyfier id = new URIIdentyfier(entity.getURI(), URIType.CONCEPT);
							String tablename = "table_concept_" + conceptCounter++;
							mapper.put(id,tablename);
							/* Creating the table */
							createTable(tablename, 1, createIndexSQL,createTableSQLs);
						}
					} else if (entity instanceof OWLObjectProperty) {
						URIIdentyfier id = new URIIdentyfier(entity.getURI(), URIType.OBJECTPROPERTY);
						String tablename = "table_objectProperty_" + objectpropCounter++;
						mapper.put(id,tablename);
						/* Creating the table */
						createTable(tablename, 2,createIndexSQL, createTableSQLs);
					} else if (entity instanceof OWLDataProperty) {
						URIIdentyfier id = new URIIdentyfier(entity.getURI(), URIType.DATAPROPERTY);
						String tablename = "table_dataProperty_" + datapropCounter++;
						mapper.put(id,tablename);
						/* Creating the table */
						createTable(tablename, 2,createIndexSQL,createTableSQLs);
					}
				}
		
				/* Inserting individuals */
				log.debug("Preparing indivituals to insert");
				int tupleCounter = 0;
				Set<OWLIndividualAxiom> ind = ontology.getIndividualAxioms();
				Iterator<OWLIndividualAxiom> ind_it = ind.iterator();
				while (ind_it.hasNext()) {
					tupleCounter += 1;
					OWLIndividualAxiom ax = ind_it.next();
					if (ax instanceof OWLClassAssertionAxiom) {
						OWLClassAssertionAxiom caa = (OWLClassAssertionAxiom) ax;
						OWLDescription des = caa.getDescription();
						if (!des.isOWLThing()) {
							OWLIndividual i = caa.getIndividual();
							OWLClass clazz = (OWLClass) des;
							URIIdentyfier id = new URIIdentyfier(clazz.getURI(),URIType.CONCEPT);
							String tablename = mapper.get(id);
							if(tablename == null){
								throw new AboxDumpException("No table found for " +id.getUri().toString() + " and uri type " + id.getType());
							}
							String in = i.getURI().toString();
							add(tablename, in, inserts);
						}
					} else if (ax instanceof OWLDataPropertyAssertionAxiom) {
		
						OWLDataPropertyAssertionAxiom paa = (OWLDataPropertyAssertionAxiom) ax;
						OWLConstant obj = paa.getObject();
						OWLIndividual sub = paa.getSubject();
						OWLDataPropertyExpression prop = paa.getProperty();
						OWLDataProperty dp = (OWLDataProperty) prop;
						URIIdentyfier id = new URIIdentyfier(dp.getURI(),URIType.DATAPROPERTY);
						String tablename = mapper.get(id);
						if(tablename == null){
							throw new AboxDumpException("No table found for " +id.getUri().toString() + " and uri type " + id.getType());
						}
						add(tablename, sub.getURI().toString(),	obj.getLiteral(),inserts);
		
					} else if (ax instanceof OWLObjectPropertyAssertionAxiom) {
						OWLObjectPropertyAssertionAxiom ppa = (OWLObjectPropertyAssertionAxiom) ax;
						OWLIndividual sub = ppa.getSubject();
						OWLIndividual obj = ppa.getObject();
						OWLObjectPropertyExpression prop = ppa.getProperty();
						OWLObjectProperty op = (OWLObjectProperty) prop;
						URIIdentyfier id = new URIIdentyfier(op.getURI(),URIType.OBJECTPROPERTY);
						String tablename = mapper.get(id);
						if(tablename == null){
							throw new AboxDumpException("No table found for " +id.getUri().toString() + " and uri type " + id.getType());
						}
						add(tablename, sub.getURI().toString(), obj.getURI().toString(),inserts);
					}
				}
				log.debug("Tuples to be inserted: {}", tupleCounter);
			}
			materializeMapper();
			insertData(inserts);
			createIndexes(createIndexSQL);
		} catch (SQLException e) {
			if(isCanceled){
				log.debug(e.getMessage());
			}else{
				throw new AboxDumpException("Error while dumping Abox to data base. " + e.getMessage(), e);
			}
		} catch (Exception e) {
			if(isCanceled){
				log.debug(e.getMessage());
			}else{
				throw new AboxDumpException("Error while dumping Abox to data base. " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Dumps the abox into the data source with the given identifier.
	 *
	 * @param ontologies
	 *            the ontologies
	 * @param dsname
	 *            the data source identifier
	 * @param createMappings
	 *            true if automatic mappings should be created
	 * @throws Exception
	 */
	public void materialize(Set<OWLOntology> ontologies, boolean override) throws AboxDumpException {
		isCanceled = false;
//		if (apic == null) {
//			throw new NullPointerException("the api controller has not been set.Use ABoxToDBDumper.setAPIController to set the controller");
//		}
//
//		ds = apic.getDatasourcesController().getDataSource(dsname);

		if(override){
			AboxFromDBLoader loader = new AboxFromDBLoader();
			try {
				loader.destroyDump(ds);
			} catch (AboxLoaderException e) {
				log.warn("Error while dropping old dump: it might not exists");
			}
		}
		try {
			conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(ds);
		} catch (NoDatasourceSelectedException e) {
			throw new AboxDumpException("Error while connecting to data base. " + e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			throw new AboxDumpException("Error while connecting to data base. " + e.getMessage(), e);
		} catch (SQLException e) {
			throw new AboxDumpException("Error while connecting to data base. " + e.getMessage(), e);
		}
		materialize(ontologies, conn);
	}

	private void materializeMapper() throws Exception{
		
		String createTable = "CREATE TABLE mapper (uri VARCHAR NOT NULL, type VARCHAR NOT NULL, tablename VARCHAR NOT NULL)";
		
		StringBuffer values = new StringBuffer();
		Iterator<URIIdentyfier> it = mapper.keySet().iterator();
		while(it.hasNext()){
			URIIdentyfier id = it.next();
			if(values.length() >0){
				values.append(",");
			}
			values.append("(");
			values.append("'");
			values.append(id.getUri());
			values.append("'");
			values.append(",");
			values.append("'");
			values.append(id.getType());
			values.append("'");
			values.append(",");
			values.append("'");
			values.append(mapper.get(id));
			values.append("'");
			values.append(")");
		}
		
		String insertStatement = "INSERT INTO mapper VALUES "+ values.toString();
		
		conn.commit();
		statement = conn.createStatement();
		statement.execute(createTable);
		statement.execute(insertStatement);
		statement.close();
	}
	
	/**
	 * Inserts the data into the abox. Note: its creates one SQL statement per
	 * table
	 *
	 * @throws SQLException
	 */
	private void insertData(HashMap<String, List<List<String>>>	inserts) throws SQLException {
		log.debug("Inserting data into DB. ");
		
		conn.commit();
		statement = conn.createStatement();
		Set<String> keys = inserts.keySet();// keys are table names
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String key = it.next();
			List<List<String>> listOfValues = inserts.get(key);
			StringBuffer sqlbody = new StringBuffer();
			Iterator<List<String>> vit = listOfValues.iterator();
			while (vit.hasNext()) {
				List<String> values = vit.next();
				StringBuffer sb = new StringBuffer();
				Iterator<String> it2 = values.iterator();
				while (it2.hasNext()) {
					String value = it2.next();
					value = value.replace("'", "");
					value = "'" + value + "'";
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(value);
				}
				String valuesAsString = sb.toString();
				if (sqlbody.length() > 0) {
					sqlbody.append(",");
				}
				sqlbody.append("(");
				sqlbody.append(valuesAsString);
				sqlbody.append(")");
			}

			StringBuffer sqlquery = new StringBuffer();
			sqlquery.append("INSERT INTO ");
			sqlquery.append(key);
			sqlquery.append(" VALUES ");
			sqlquery.append(sqlbody);

			log.debug("{}", sqlquery.toString());
			// System.out.println(sb.toString());
			statement.execute(sqlquery.toString());

		}
		log.debug("Done inserting data");
		statement.close();
	}

	/**
	 * adds a new value for a table with one column to the insert map
	 *
	 * @param tablename
	 *            the table name where the values should go
	 * @param value
	 *            the value as String
	 */
	private void add(String tablename, String value, HashMap<String, List<List<String>>>	inserts) {

		List<List<String>> list = inserts.get(tablename);
		if (list == null) {
			list = new Vector<List<String>>();
			// createTable(tablename, 1);
		}
		Vector<String> values = new Vector<String>();
		values.add(value);
		list.add(values);
		inserts.put(tablename, list);
	}

	/**
	 *
	 * adds a new value for a table with two columns to the insert map
	 *
	 * @param tablename
	 *            the table name where the values should go
	 * @param sub
	 *            the subject as string
	 * @param obj
	 *            the object as string
	 */
	private void add(String tablename, String sub, String obj,HashMap<String, List<List<String>>>	inserts) {

		List<List<String>> list = inserts.get(tablename);
		if (list == null) {
			list = new Vector<List<String>>();
		}
		Vector<String> v = new Vector<String>();
		v.add(sub);
		v.add(obj);
		list.add(v);
		inserts.put(tablename, list);
	}

	/**
	 * Creates a table with the given name an the given number of columns
	 *
	 * @param tablename
	 *            the table name
	 * @param columns
	 *            number of columns
	 * @throws AboxDumpException 
	 */
	private void createTable(String tablename, int columns, Set<String>	createIndexSQL, Set<String>	createTableSQLs	) throws AboxDumpException {

		// TODO Move create table to an independent method that creates the DDL
		// for the full database. Creation of the schema should be done in one
		// single call.
		log.debug("Creating table {} with {} columns", tablename, columns);

		StringBuffer col = new StringBuffer();
		for (int i = 0; i < columns; i++) {
			if (col.length() > 0) {
				col.append(", ");
			}
			col.append("term");
			col.append(i);
			col.append(" VARCHAR");

			String index = "CREATE INDEX index" + indexcounter++ + " ON " + tablename + "( term" + i + ")";
			createIndexSQL.add(index);
		}
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE TABLE ");
		sql.append(tablename);
		sql.append("(");
		sql.append(col.toString());
		sql.append(")");

		if (createTableSQLs.add(sql.toString())) {
			try {
				conn.commit();
				statement = conn.createStatement();
				log.debug("Executing SQL: {}", sql.toString());
				statement.execute(sql.toString());
				statement.close();
			} catch (SQLException e) {
				throw new AboxDumpException(e.getMessage());
			}
		}
	}


	/**
	 * creates SQL statements to create indexes over the created tables
	 *
	 * @throws Exception
	 */
	private void createIndexes(Set<String>	createIndexSQL) throws Exception {
		log.debug("Creating indexes");
		Iterator<String> it = createIndexSQL.iterator();
		conn.commit();
		statement = conn.createStatement();
		while (it.hasNext()) {
			String sql = it.next();
			log.debug("Executing update: {}", sql);
			statement.executeUpdate(sql);
		}
		statement.close();
		log.debug("Indexes created successfully");
	}

	/**
	 * Adds the given the listener
	 *
	 * @param l
	 *            the listener
	 */
	public void addListener(ABoxDumpListener l) {
		listener.add(l);
	}

	/**
	 * Removes the given the listener
	 *
	 * @param l
	 *            the listener
	 */
	public void removeListener(ABoxDumpListener l) {
		listener.remove(l);
	}

//	/**
//	 * Returns the given api controller
//	 *
//	 * @return the api controller
//	 */
//	public APIController getController() {
//		// TODO this shouldn't return a null pointer, the calling method should
//		// be prepared to recreive a null object
//		if (apic == null) {
//			throw new NullPointerException("the api controller has not been set.Use ABoxToDBDumper.setAPIController to set the controller");
//		}
//		return apic;
//	}

//	/**
//	 * Returns the current instance of the class
//	 *
//	 * @return
//	 */
//
//	public static ABoxToDBDumper getInstance() {
//		if (instance == null) {
//			instance = new ABoxToDBDumper();
//		}
//		return instance;
//	}	
	
	public Map<URIIdentyfier, String> getMapper(){
		return mapper;
	}

	@Override
	public void actionCanceled() {
		
		try {
			isCanceled = true;
			statement.cancel();
			statement.close();
			removeTables();
		} catch (SQLException e) {
			log.warn(e.getMessage());
		}
	}
	
	private void removeTables() throws SQLException{
		Collection<String> col = mapper.values();
		Iterator<String> it = col.iterator();
		statement = conn.createStatement();
		while(it.hasNext()){
			try {
				statement.executeUpdate("DROP TABLE " + it.next() );
			} catch (SQLException e) {
				log.debug(e.getMessage());
			}
		}
		try {
			statement.execute("DROP TABLE mapper");
		} catch (SQLException e) {
			log.debug(e.getMessage());
		}
	}	
}
