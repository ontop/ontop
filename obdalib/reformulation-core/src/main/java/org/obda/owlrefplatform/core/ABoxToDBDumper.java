package org.obda.owlrefplatform.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.obda.query.domain.Atom;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.PredicateFactory;
import org.obda.query.domain.Query;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.BasicPredicateFactoryImpl;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.TermFactoryImpl;
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

public class ABoxToDBDumper {

	private Connection							conn					= null;
	private Set<String>							createTableSQLs			= null;
	private HashMap<String, List<List<String>>>	inserts					= null;
	private APIController						apic					= null;
	private List<ABoxDumpListener>				listener				= null;
	private DataSource							ds						= null;
	private Map<String, String>					ontologyMapper			= null;
	private Map<String, String>					classMapper				= null;
	private Map<String, String>					datapropertyMapper		= null;
	private Map<String, String>					objectporpertyMapper	= null;
	private int									ontocounter				= 1;
	private int									classcounter			= 1;
	private int									dpcounter				= 1;
	private int									opcounter				= 1;
	private Set<String>							createIndexSQL			= null;
	private int									indexcounter			= 1;
	private int									mapcounter				= 1;

	private final PredicateFactory				predicateFactory		= BasicPredicateFactoryImpl.getInstance();
	private final TermFactoryImpl				termFactory				= TermFactoryImpl.getInstance();

	private static ABoxToDBDumper				instance				= null;

	private final Logger								log						= LoggerFactory.getLogger(ABoxToDBDumper.class);

	public ABoxToDBDumper() {
		listener = new Vector<ABoxDumpListener>();
		instance = this;
	}

	public void setAPIController(APIController apic) {
		this.apic = apic;
	}

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
	public void materialize(Set<OWLOntology> ontologies, Connection c, URI dsUri, boolean createMappings) throws Exception {

		log.debug("Materializing ABoxes into DB");

		createTableSQLs = new HashSet<String>();
		inserts = new HashMap<String, List<List<String>>>();
		ontologyMapper = new HashMap<String, String>();
		classMapper = new HashMap<String, String>();
		datapropertyMapper = new HashMap<String, String>();
		objectporpertyMapper = new HashMap<String, String>();
		createIndexSQL = new HashSet<String>();
		ontocounter = 1;
		classcounter = 1;
		dpcounter = 1;
		opcounter = 1;

		conn = c;

		Iterator<OWLOntology> ontologyIterator = ontologies.iterator();

		while (ontologyIterator.hasNext()) {

			/*
			 * For each ontology
			 */
			OWLOntology onto = ontologyIterator.next();
			log.debug("Materializing ABox for ontology: {}", onto.getURI().toString());
			log.debug("Creating mapping: {}", createMappings);

			String ontoname = getOntologyAlias();
			ontologyMapper.put(getOnotlogyName(onto), ontoname);
			Set<OWLEntity> entities = onto.getSignature();
			Iterator<OWLEntity> entityIterator = entities.iterator();

			while (entityIterator.hasNext()) {
				/* For each entity */
				OWLEntity entity = entityIterator.next();

				if (entity instanceof OWLClass) {
					OWLClass clazz = (OWLClass) entity;
					if (!clazz.isOWLThing()) {
						String alias = getClassAlias();
						String tablename = ontoname + "_" + alias;
						classMapper.put(entity.toString(), alias);

						/* Creating the table */
						createTable(tablename, 1);

						if (createMappings && !clazz.isOWLThing()) {

							/* Creating the mapping */
							URI name = clazz.getURI();
							Term qt = termFactory.createVariable("x");
							List<Term> terms = new Vector<Term>();
							terms.add(qt);
							Predicate predicate = predicateFactory.createPredicate(name, terms.size());
							Atom bodyAtom = new AtomImpl(predicate, terms);
							List<Atom> body = new Vector<Atom>();
							body.add(bodyAtom); // the body
							predicate = predicateFactory.createPredicate(URI.create("q"), terms.size());
							Atom head = new AtomImpl(predicate, terms); // the
							// head
							Query cq = new CQIEImpl(head, body, false);
							String sql = "SELECT term0 as x FROM " + tablename;
							OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id" + mapcounter++);
							ax.setTargetQuery(cq);
							ax.setSourceQuery(new RDBMSSQLQuery(sql));
							apic.getMappingController().insertMapping(dsUri, ax);

							log.debug("Mapping created: {}", ax.toString());
						}
					}
				} else if (entity instanceof OWLObjectProperty) {
					String alias = getOPAlias();
					String tablename = ontoname + "_" + alias;
					objectporpertyMapper.put(entity.toString(), alias);

					createTable(tablename, 2);

					if (createMappings) {
						OWLObjectProperty oop = (OWLObjectProperty) entity;
						Term qt1 = termFactory.createVariable("x");
						Term qt2 = termFactory.createVariable("y");
						List<Term> terms = new Vector<Term>();
						terms.add(qt1);
						terms.add(qt2);
						Predicate predicate = predicateFactory.createPredicate(oop.getURI(), terms.size());
						Atom bodyAtom = new AtomImpl(predicate, terms);
						List<Atom> body = new Vector<Atom>();
						body.add(bodyAtom); // the body
						predicate = predicateFactory.createPredicate(URI.create("q"), terms.size());
						Atom head = new AtomImpl(predicate, terms); // the head
						Query cq = new CQIEImpl(head, body, false);
						String sql = "SELECT term0 as x, term1 as y FROM " + tablename;
						OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id" + mapcounter++);
						ax.setTargetQuery(cq);
						ax.setSourceQuery(new RDBMSSQLQuery(sql));

						log.debug("Mapping created: {}", ax.toString());

						apic.getMappingController().insertMapping(dsUri, ax);

					}

				} else if (entity instanceof OWLDataProperty) {
					String alias = getDPAlias();
					String tablename = ontoname + "_" + alias;
					datapropertyMapper.put(entity.toString(), alias);

					createTable(tablename, 2);

					if (createMappings) {
						OWLDataProperty oop = (OWLDataProperty) entity;
						Term qt1 = termFactory.createVariable("x");
						Term qt2 = termFactory.createVariable("y");
						List<Term> terms = new Vector<Term>();
						terms.add(qt1);
						terms.add(qt2);
						Predicate predicate = predicateFactory.createPredicate(oop.getURI(), terms.size());
						Atom bodyAtom = new AtomImpl(predicate, terms);
						List<Atom> body = new Vector<Atom>();
						body.add(bodyAtom); // the body
						predicate = predicateFactory.createPredicate(URI.create("q"), terms.size());
						Atom head = new AtomImpl(predicate, terms); // the head
						Query cq = new CQIEImpl(head, body, false);
						String sql = "SELECT term0 as x, term1 as y FROM " + tablename;
						OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id" + mapcounter++);
						ax.setTargetQuery(cq);
						ax.setSourceQuery(new RDBMSSQLQuery(sql));

						log.debug("Mapping created: {}", ax.toString());

						apic.getMappingController().insertMapping(dsUri, ax);
					}
				}
			}
		}

		/* Inserting the data */

		log.debug("Preparing the data to insert");
		int tupleCounter = 0;

		Iterator<OWLOntology> it = ontologies.iterator();
		while (it.hasNext()) {
			OWLOntology onto = it.next();
			String ontoname = ontologyMapper.get(getOnotlogyName(onto));
			Set<OWLIndividualAxiom> ind = onto.getIndividualAxioms();
			Iterator<OWLIndividualAxiom> ind_it = ind.iterator();
			while (ind_it.hasNext()) {

				tupleCounter += 1;

				OWLIndividualAxiom ax = ind_it.next();
				if (ax instanceof OWLClassAssertionAxiom) {

					OWLClassAssertionAxiom caa = (OWLClassAssertionAxiom) ax;
					OWLDescription des = caa.getDescription();
					if (!des.isOWLThing()) {
						OWLIndividual i = caa.getIndividual();
						String alias = classMapper.get(des.toString());
						String tablename = ontoname + "_" + alias;
						String in = onto.getURI().toString() + "#" + i.getURI().getFragment();
						add(tablename, in);
					}

				} else if (ax instanceof OWLDataPropertyAssertionAxiom) {

					OWLDataPropertyAssertionAxiom paa = (OWLDataPropertyAssertionAxiom) ax;
					OWLConstant obj = paa.getObject();
					OWLIndividual sub = paa.getSubject();
					OWLDataPropertyExpression prop = paa.getProperty();
					String alias = datapropertyMapper.get(prop.toString());
					String tablename = ontoname + "_" + alias;
					add(tablename, onto.getURI().toString() + "#" + sub.getURI().getFragment(), onto.getURI().toString() + "#"
							+ obj.getLiteral());

				} else if (ax instanceof OWLObjectPropertyAssertionAxiom) {
					OWLObjectPropertyAssertionAxiom ppa = (OWLObjectPropertyAssertionAxiom) ax;
					OWLIndividual sub = ppa.getSubject();
					OWLIndividual obj = ppa.getObject();
					OWLObjectPropertyExpression prop = ppa.getProperty();
					String alias = objectporpertyMapper.get(prop.toString());
					String tablename = ontoname + "_" + alias;
					add(tablename, onto.getURI().toString() + "#" + sub.getURI().getFragment(), onto.getURI().toString() + "#"
							+ obj.getURI().getFragment());
				}
			}
		}
		log.debug("Tuples to be inserted: {}", tupleCounter);


		insertData();
		createIndexes();
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
	public void materialize(Set<OWLOntology> ontologies, URI dsname, boolean createMappings) throws Exception {

		if (apic == null) {
			throw new NullPointerException("the api controller has not been set.Use ABoxToDBDumper.setAPIController to set the controller");
		}

		ds = apic.getDatasourcesController().getDataSource(dsname);

		try {
			createConnection();
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
		}

		materialize(ontologies, conn, dsname, createMappings);
	}

	/**
	 * Inserts the data into the abox. Note: its creates one SQL statement per
	 * table
	 *
	 * @throws SQLException
	 */
	private void insertData() throws SQLException {
		log.debug("Inserting data into DB. ");

		Statement st = conn.createStatement();
		Set<String> keys = inserts.keySet();// keys are table names
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String key = it.next();
			List<List<String>> values = inserts.get(key);
			StringBuffer v = new StringBuffer();
			Iterator<List<String>> vit = values.iterator();
			while (vit.hasNext()) {
				List<String> list = vit.next();
				StringBuffer sb = new StringBuffer();
				Iterator<String> it2 = list.iterator();
				while (it2.hasNext()) {
					String s = it2.next();
					s = s.replace("'", "");
					s = "'" + s + "'";
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(s);
				}
				String value = sb.toString();
				if (v.length() > 0) {
					v.append(",");
				}
				v.append("(");
				v.append(value);
				v.append(")");
			}

			StringBuffer sb = new StringBuffer();
			sb.append("INSERT INTO ");
			sb.append(key);
			sb.append(" VALUES ");
			sb.append(v);

			log.debug("{}", sb.toString());
			// System.out.println(sb.toString());
			st.execute(sb.toString());

		}
		log.debug("Done inserting data");
		st.close();
	}

	/**
	 * adds a new value for a table with one column to the insert map
	 *
	 * @param tablename
	 *            the table name where the values should go
	 * @param value
	 *            the value as String
	 */
	private void add(String tablename, String value) {

		List<List<String>> list = inserts.get(tablename);
		if (list == null) {
			list = new Vector<List<String>>();
			// createTable(tablename, 1);
		}
		Vector<String> v = new Vector<String>();
		v.add(value);
		list.add(v);
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
	private void add(String tablename, String sub, String obj) {

		List<List<String>> list = inserts.get(tablename);
		if (list == null) {
			list = new Vector<List<String>>();
			// createTable(tablename, 1);
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
	 */
	private void createTable(String tablename, int columns) {

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
				Statement st = conn.createStatement();
				log.debug("Executing SQL: {}", sql.toString());
				st.execute(sql.toString());
				st.close();
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * This method takes the ontology name out from its uri. Its not actually
	 * necessary to do but keeps the names shorter
	 *
	 * @param onto
	 *            the ontology uri
	 * @return the onotlogy name
	 */
	private String getOnotlogyName(OWLOntology onto) {
		String ontouri = onto.getURI().toString();
		int i = ontouri.lastIndexOf("/");
		return ontouri.substring(i + 1, ontouri.length() - 4);
	}

	/**
	 * creates a sql connection with the default data source
	 *
	 * @throws Exception
	 */
	private void createConnection() throws Exception {

		// TODO ABox Dump: This method will not catch exceptions properly. Why
		// is the default database schema called postgres?

		Class.forName(ds.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER));
		String usr = ds.getParameter(RDBMSsourceParameterConstants.DATABASE_USERNAME);
		String pwd = ds.getParameter(RDBMSsourceParameterConstants.DATABASE_PASSWORD);
		String url = ds.getParameter(RDBMSsourceParameterConstants.DATABASE_URL);
		conn = DriverManager.getConnection(url + "postgres", usr, pwd);
		log.debug("Creating a connection to the database {}", url + "postgres");
		try {
			conn.createStatement().executeUpdate("DROP DATABASE " + ds.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME));

		} catch (SQLException e) {
			log.debug(e.getMessage(), e);
		}
		conn.createStatement().executeUpdate("CREATE DATABASE " + ds.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME));
		conn.close();

		conn = DriverManager.getConnection(url + ds.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME), usr, pwd);
	}

	/**
	 * creates SQL statements to create indexes over the created tables
	 *
	 * @throws Exception
	 */
	private void createIndexes() throws Exception {
		log.debug("Creating indexes");
		Iterator<String> it = createIndexSQL.iterator();
		Statement st = conn.createStatement();
		while (it.hasNext()) {
			String sql = it.next();
			log.debug("Executing update: {}", sql);
			st.executeUpdate(sql);
		}
		st.close();
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

	/**
	 * Returns the given api controller
	 *
	 * @return the api controller
	 */
	public APIController getController() {
		// TODO this shouldn't return a null pointer, the calling method should
		// be prepared to recreive a null object
		if (apic == null) {
			throw new NullPointerException("the api controller has not been set.Use ABoxToDBDumper.setAPIController to set the controller");
		}
		return apic;
	}

	/**
	 * Returns the current instance of the class
	 *
	 * @return
	 */

	public static ABoxToDBDumper getInstance() {
		if (instance == null) {
			instance = new ABoxToDBDumper();
		}
		return instance;
	}

	// Note the following methods where needed to resolve the same name problem
	// e.g. between classes and properties

	/**
	 * creates an alias for each ontology
	 *
	 * @return the alias
	 */
	private String getOntologyAlias() {
		return "ontology" + ontocounter++;
	}

	/**
	 * creates an alias for each class
	 *
	 * @return the alias
	 */
	private String getClassAlias() {
		return "class" + classcounter++;
	}

	/**
	 * creates an alias for each object property
	 *
	 * @return the alias
	 */
	private String getOPAlias() {
		return "objectproperty" + opcounter++;
	}

	/**
	 * creates an alias for each data property
	 *
	 * @return the alias
	 */
	private String getDPAlias() {
		return "dataproperty" + dpcounter++;
	}

	/**
	 * Returns a map between original ontology names and alias
	 *
	 * @return a map
	 */
	public Map<String, String> getOntolgyMapper() {
		return ontologyMapper;
	}

	/**
	 * Returns a map between original class names and alias
	 *
	 * @return a map
	 */
	public Map<String, String> getClassMapper() {
		return classMapper;
	}

	/**
	 * Returns a map between original object property names and alias
	 *
	 * @return a map
	 */
	public Map<String, String> getObjectPropertyMapper() {
		return objectporpertyMapper;
	}

	/**
	 * Returns a map between original data property names and alias
	 *
	 * @return a map
	 */
	public Map<String, String> getDataPropertyMapper() {
		return datapropertyMapper;
	}

}
