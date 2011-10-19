package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.LUBM.TBoxLoader;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlrefplatform.core.GraphGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGOperations;
import it.unibz.krdb.obda.owlrefplatform.core.dag.SemanticIndexRange;
import it.unibz.krdb.obda.owlrefplatform.core.dag.SemanticIndexRange.Interval;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OClass;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.PropertySomeRestrictionImpl;
import it.unibz.krdb.obda.owlrefplatform.exception.PunningException;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store ABox assertions in the DB
 * 
 * @author Sergejs Pugac
 */
public class RDBMSSIRepositoryManager implements RDBMSDataRepositoryManager {

	private final static Logger log = LoggerFactory.getLogger(RDBMSSIRepositoryManager.class);

	private Connection conn = null;

	private OBDADataSource db = null;

	public final static String index_table = "IDX";

	private final static String create_ddl = "CREATE TABLE " + index_table + " ( " + "URI VARCHAR(1000), " + "IDX INTEGER, "
			+ "IDX_FROM INTEGER, " + "IDX_TO INTEGER, " + "ENTITY_TYPE INTEGER" + ")";

	private final static String drop_dll = "DROP TABLE " + index_table + " IF EXISTS";

	private final static String insert_query = "INSERT INTO " + index_table
			+ "(URI, IDX, IDX_FROM, IDX_TO, ENTITY_TYPE) VALUES(?, ?, ?, ?, ?)";

	private final static String select_query = "SELECT * FROM " + index_table;

	public static final String class_table = "class";

	public static final String role_table = "role";

	public static final String class_table_create = "CREATE TABLE " + class_table + " ( " + "URI VARCHAR(1000)," + "IDX SMALLINT" + ")";

	public static final String role_table_create = "CREATE TABLE " + role_table + " ( " + "URI1 VARCHAR(1000), " + "URI2 VARCHAR(1000), "
			+ "IDX SMALLINT" + ")";

	public static final String class_table_drop = "DROP TABLE IF EXISTS " + class_table;

	public static final String role_table_drop = "DROP TABLE IF EXISTS " + role_table;

	public static final String class_insert = "INSERT INTO " + class_table + " (URI, IDX) VALUES (?, ?)";

	public static final String role_insert = "INSERT INTO " + role_table + " (URI1, URI2, IDX) VALUES (?, ?, ?)";

	public static final String indexclass1 = "CREATE INDEX idxclass1 ON " + class_table + "(URI)";

	public static final String indexclass2 = "CREATE INDEX idxclass2 ON " + class_table + "(IDX)";

	public static final String indexclass3 = "CREATE INDEX idxclass3 ON " + class_table + "(IDX, URI)";

	public static final String indexclass4 = "CREATE INDEX idxclass4 ON " + class_table + "(URI, IDX)";

	public static final String indexrole1 = "CREATE INDEX idxrole1 ON " + role_table + "(URI1)";

	public static final String indexrole2 = "CREATE INDEX idxrole2 ON " + role_table + "(IDX)";

	public static final String indexrole3 = "CREATE INDEX idxrole3 ON " + role_table + "(URI2)";

	public static final String indexrole4 = "CREATE INDEX idxrole4 ON " + role_table + "(URI1, URI2)";

	public static final String indexrole5 = "CREATE INDEX idxrole5 ON " + role_table + "(URI1, IDX)";

	public static final String indexrole6 = "CREATE INDEX idxrole6 ON " + role_table + "(URI2, URI1)";

	public static final String indexrole7 = "CREATE INDEX idxrole7 ON " + role_table + "(URI2, IDX)";

	public static final String indexrole8 = "CREATE INDEX idxrole8 ON " + role_table + "(IDX, URI1)";

	public static final String indexrole9 = "CREATE INDEX idxrole9 ON " + role_table + "(IDX, URI2)";

	public static final String indexrole10 = "CREATE INDEX idxrole10 ON " + role_table + "(IDX, URI1, URI2)";

	public static final String indexrole11 = "CREATE INDEX idxrole11 ON " + role_table + "(IDX, URI2, URI1)";

	public static final String indexrole12 = "CREATE INDEX idxrole12 ON " + role_table + "(URI1, URI2, IDX)";

	public static final String indexrole13 = "CREATE INDEX idxrole13 ON " + role_table + "(URI1, IDX, URI2)";

	public static final String indexrole14 = "CREATE INDEX idxrole14 ON " + role_table + "(URI2, URI1, IDX)";

	public static final String indexrole15 = "CREATE INDEX idxrole15 ON " + role_table + "(URI2, IDX, URI1)";

	public static final String analyze = "ANALYZE";

	public static final String select_mapping_class = "SELECT URI as X FROM " + class_table;

	public static final String select_mapping_class_role_left = "SELECT URI1 as X FROM " + role_table;

	public static final String select_mapping_class_role_right = "SELECT URI2 as X FROM " + role_table;

	public static final String select_mapping_role = "SELECT URI1 as X, URI2 as Y FROM " + role_table;

	public static final String select_mapping_role_inverse = "SELECT URI2 as X, URI1 as Y FROM " + role_table;

	public static final String whereSingleCondition = "(IDX = %d)";

	public static final String whereIntervalCondition = "(IDX >= %d AND IDX <= %d)";

	private final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();

	private final OntologyFactory descFactory = new OntologyFactoryImpl();

	private Properties config = null;

	private DAG dag;

	private DAG pureIsa;

	private DAG sigmaDag;

	private Ontology aboxDependencies;

	private Ontology ontology;

	final static int CLASS_TYPE = 1;
	final static int ROLE_TYPE = 2;

	private static final boolean mergeUniions = true;

	public RDBMSSIRepositoryManager(OBDADataSource ds) throws SQLException, PunningException {
		this(ds, null);
	}

	public RDBMSSIRepositoryManager(OBDADataSource ds, Set<Predicate> vocabulary) throws SQLException, PunningException {
		try {
			if (vocabulary != null) {
				setVocabulary(vocabulary);
			}
			setDatabase(ds);
		} catch (ClassNotFoundException e) {
			RuntimeException ex = new RuntimeException(e);
			e.fillInStackTrace();
			throw ex;
		}
	}

	@Override
	public void setConfig(Properties config) {
		this.config = config;
	}

	@Override 
	public void disconnect() {
		try {
			conn.close();
		} catch (Exception e) {
			
		}
	}
	
	@Override
	public Connection getConnection() {
		return conn;
	}
	

	@Override
	public void setDatabase(OBDADataSource ds) throws SQLException, ClassNotFoundException {
		this.db = db;
		
		String url = ds.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = ds.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = ds.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = ds.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);
		
		try {
			Class.forName(driver);
		}
		catch (ClassNotFoundException e1) {
			// Does nothing because the SQLException handles this problem also.
		}		
		conn = DriverManager.getConnection(url, username, password);		
	}

	@Override
	public void setTBox(Ontology ontology) {

		this.ontology = ontology;

		log.debug("Ontology: {}", ontology.toString());

		dag = DAGConstructor.getISADAG(ontology);

		// USE THE DAG GRAPHS FOR DEBUGGING
		//
		// try {
		// GraphGenerator.dumpISA(dag, "given");
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }

		dag.clean();

		pureIsa = DAGConstructor.filterPureISA(dag);
		aboxDependencies = DAGConstructor.getSigmaOntology(dag);

		pureIsa.clean();
		pureIsa.index();

		/***
		 * Copying the equivalences that might bet lost from the translation
		 */
		for (Description d : dag.equi_mappings.keySet()) {
			pureIsa.equi_mappings.put(d, dag.equi_mappings.get(d));
		}

//		try {
//			GraphGenerator.dumpISA(dag, "no-cycles");
//			GraphGenerator.dumpISA(pureIsa, "isa-indexed");
//
//		} catch (IOException e) {
//
//		}

	}

	@Override
	public String getType() {
		return TYPE_SI;
	}

	@Override
	public void getTablesDDL(OutputStream outstream) throws IOException {
		log.debug("Recreating ABox tables");

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));

		out.append(create_ddl);
		out.append(";\n");

		out.append(class_table_create);
		out.append(";\n");

		out.append(role_table_create);
		out.append(";\n");

		out.flush();
	}

	@Override
	public void getIndexDDL(OutputStream outstream) throws IOException {

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));

		out.append(indexclass1);
		out.append(";\n");
		out.append(indexclass2);
		out.append(";\n");
		out.append(indexclass3);
		out.append(";\n");
		out.append(indexclass4);
		out.append(";\n");
		out.append(indexrole1);
		out.append(";\n");
		out.append(indexrole2);
		out.append(";\n");
		out.append(indexrole3);
		out.append(";\n");
		out.append(indexrole4);
		out.append(";\n");
		out.append(indexrole5);
		out.append(";\n");
		out.append(indexrole6);
		out.append(";\n");
		out.append(indexrole7);
		out.append(";\n");
		out.append(indexrole8);
		out.append(";\n");

		out.append(indexrole9);
		out.append(";\n");
		out.append(indexrole10);
		out.append(";\n");
		out.append(indexrole11);
		out.append(";\n");
		out.append(indexrole12);
		out.append(";\n");

		out.append(indexrole13);
		out.append(";\n");
		out.append(indexrole14);
		out.append(";\n");
		out.append(indexrole15);
		out.append(";\n");

		out.flush();

	}

	@Override
	public void getSQLInserts(Iterator<Assertion> data, OutputStream outstream) throws IOException {

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));

		String role_insert_str = role_insert.replace("?", "%s");
		String cls_insert_str = class_insert.replace("?", "%s");

		int insertscount = 0;

		int batchCount = 0;

		while (data.hasNext()) {

			Assertion ax = data.next();

			insertscount += 1;
			batchCount += 1;

			if (ax instanceof DataPropertyAssertion) {

				DataPropertyAssertion attributeABoxAssertion = (DataPropertyAssertion) ax;
				String prop = attributeABoxAssertion.getAttribute().getName().toString();
				String uri = attributeABoxAssertion.getObject().getURI().toString();
				String lit = attributeABoxAssertion.getValue().getValue();

				Predicate propPred = predicateFactory.getDataPropertyPredicate(URI.create(prop));
				Property propDesc = descFactory.createProperty(propPred);
				DAGNode node = pureIsa.getRoleNode(propDesc);
				int idx = node.getIndex();

				out.append(String.format(role_insert_str, getQuotedString(uri), getQuotedString(lit), idx));

			} else if (ax instanceof ObjectPropertyAssertion) {

				ObjectPropertyAssertion roleABoxAssertion = (ObjectPropertyAssertion) ax;
				String prop = roleABoxAssertion.getRole().getName().toString();
				String uri1 = roleABoxAssertion.getFirstObject().getURI().toString();
				String uri2 = roleABoxAssertion.getSecondObject().getURI().toString();

				Predicate propPred = predicateFactory.getObjectPropertyPredicate(URI.create(prop));
				Property propDesc = descFactory.createProperty(propPred);

				if (dag.equi_mappings.containsKey(propDesc)) {
					Property desc = (Property) dag.equi_mappings.get(propDesc);
					if (desc.isInverse()) {
						String tmp = uri1;
						uri1 = uri2;
						uri2 = tmp;
					}
				}

				DAGNode node = pureIsa.getRoleNode(propDesc);
				int idx = node.getIndex();

				out.append(String.format(role_insert_str, getQuotedString(uri1), getQuotedString(uri2), idx));

			} else if (ax instanceof ClassAssertion) {

				String cls = ((ClassAssertion) ax).getConcept().getName().toString();
				// XXX: strange behaviour - owlapi generates an extra
				// assertion of the form ClassAssertion(Thing, i)
				// if (!cls.equals(DAG.thingStr)) {
				String uri = ((ClassAssertion) ax).getObject().getURI().toString();

				Predicate clsPred = ((ClassAssertion) ax).getConcept();
				ClassDescription clsDesc = descFactory.createClass(clsPred);
				DAGNode node = pureIsa.getClassNode(clsDesc);
				int idx = node.getIndex();

				out.append(String.format(cls_insert_str, getQuotedString(uri), idx));

				// }
			}
			out.append(";\n");
		}

		out.flush();
	}

	@Override
	public void getCSVInserts(Iterator<Assertion> data, OutputStream out) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void createDBSchema(boolean dropExisting) throws SQLException {

		log.debug("Recreating data tables");

		conn.setAutoCommit(false);
		Statement st = conn.createStatement();

		if (dropExisting) {
			try {
				dropDBSchema();
			} catch (SQLException e) {
				log.debug(e.getMessage(), e);
			}
		}

		st.addBatch(create_ddl);
		st.addBatch(class_table_create);
		st.addBatch(role_table_create);

		st.executeBatch();
		st.close();
		conn.commit();

	}

	@Override
	public void createIndexes() throws SQLException {
		log.debug("Creating indexes");

		conn.setAutoCommit(false);
		Statement st = conn.createStatement();

		st.addBatch(indexclass1);
		st.addBatch(indexclass2);
		st.addBatch(indexclass3);
		st.addBatch(indexclass4);
		st.addBatch(indexrole1);
		st.addBatch(indexrole2);
		st.addBatch(indexrole3);
		st.addBatch(indexrole4);
		st.addBatch(indexrole5);
		st.addBatch(indexrole6);
		st.addBatch(indexrole7);
		st.addBatch(indexrole8);

		st.addBatch(indexrole9);
		st.addBatch(indexrole10);
		st.addBatch(indexrole11);
		st.addBatch(indexrole12);

		st.addBatch(indexrole13);
		st.addBatch(indexrole14);
		st.addBatch(indexrole15);

		st.executeBatch();
		st.close();
		conn.commit();

	}

	@Override
	public void dropDBSchema() throws SQLException {
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();

		st.addBatch(drop_dll);

		st.addBatch(class_table_drop);
		st.addBatch(role_table_drop);
		st.executeBatch();
		st.close();
		conn.commit();
	}

	@Override
	public void insertData(Iterator<Assertion> data) throws SQLException {
		log.debug("Inserting data into DB");

		conn.setAutoCommit(false);

		PreparedStatement cls_stm = conn.prepareStatement(class_insert);
		PreparedStatement role_stm = conn.prepareStatement(role_insert);

		int insertscount = 0;

		int batchCount = 0;

		while (data.hasNext()) {

			Assertion ax = data.next();

			insertscount += 1;
			batchCount += 1;

			if (ax instanceof DataPropertyAssertion) {

				DataPropertyAssertion attributeABoxAssertion = (DataPropertyAssertion) ax;
				String prop = attributeABoxAssertion.getAttribute().getName().toString();
				String uri = attributeABoxAssertion.getObject().getURI().toString();
				String lit = attributeABoxAssertion.getValue().getValue();

				Predicate propPred = predicateFactory.getDataPropertyPredicate(URI.create(prop));
				Property propDesc = descFactory.createProperty(propPred);
				DAGNode node = pureIsa.getRoleNode(propDesc);
				
				int idx = node.getIndex();

				role_stm.setString(1, uri);
				role_stm.setString(2, lit);
				role_stm.setInt(3, idx);
				role_stm.addBatch();

			} else if (ax instanceof ObjectPropertyAssertion) {

				ObjectPropertyAssertion roleABoxAssertion = (ObjectPropertyAssertion) ax;
				String prop = roleABoxAssertion.getRole().getName().toString();
				String uri1 = roleABoxAssertion.getFirstObject().getURI().toString();
				String uri2 = roleABoxAssertion.getSecondObject().getURI().toString();

				Predicate propPred = predicateFactory.getObjectPropertyPredicate(URI.create(prop));
				Property propDesc = descFactory.createProperty(propPred);

				if (dag.equi_mappings.containsKey(propDesc)) {
					Property desc = (Property) dag.equi_mappings.get(propDesc);
					if (desc.isInverse()) {
						String tmp = uri1;
						uri1 = uri2;
						uri2 = tmp;
					}
				}

				int idx = -1;
				DAGNode node = pureIsa.getRoleNode(propDesc);
				if (node == null) {
					Property desc = (Property) dag.equi_mappings.get(propDesc);
					Property desinv = descFactory.createProperty(desc.getPredicate(), !desc.isInverse());
					DAGNode node2 = (pureIsa.getRoleNode(desinv));
					idx = node2.getIndex();
				} else {
					idx = node.getIndex();
				}

				role_stm.setString(1, uri1);
				role_stm.setString(2, uri2);
				role_stm.setInt(3, idx);
				role_stm.addBatch();

			} else if (ax instanceof ClassAssertion) {

				String cls = ((ClassAssertion) ax).getConcept().getName().toString();
				// XXX: strange behaviour - owlapi generates an extra
				// assertion of the form ClassAssertion(Thing, i)
				// if (!cls.equals(DAG.thingStr)) {
				String uri = ((ClassAssertion) ax).getObject().getURI().toString();

				Predicate clsPred = ((ClassAssertion) ax).getConcept();
				ClassDescription clsDesc = descFactory.createClass(clsPred);
				DAGNode node = pureIsa.getClassNode(clsDesc);
				int idx = node.getIndex();

				cls_stm.setString(1, uri);
				cls_stm.setInt(2, idx);
				cls_stm.addBatch();
				// }
			}

			if (batchCount == 50000) {
				role_stm.executeBatch();
				role_stm.clearBatch();

				cls_stm.executeBatch();
				cls_stm.clearBatch();
			}
		}

		role_stm.executeBatch();
		role_stm.clearBatch();
		role_stm.close();

		cls_stm.executeBatch();
		cls_stm.clearBatch();
		cls_stm.close();

		conn.commit();

		log.debug("Total tuples inserted: {}", insertscount);

	}

	@Override
	public Ontology getABoxDependencies() {
		return aboxDependencies;
	}

	@Override
	public void loadMetadata() throws SQLException {
		log.debug("Checking if SemanticIndex exists in DB");

		Map<Description, DAGNode> res_classes = new HashMap<Description, DAGNode>();
		Map<Description, DAGNode> res_roles = new HashMap<Description, DAGNode>();
		Map<Description, DAGNode> res_allnodes = new HashMap<Description, DAGNode>();

		ResultSet res_rows = conn.createStatement().executeQuery(select_query);
		while (res_rows.next()) {
			String uri = res_rows.getString(1);
			int idx = res_rows.getInt(2);
			int start_idx = res_rows.getInt(3);
			int end_idx = res_rows.getInt(4);
			int type = res_rows.getInt(5);

			Predicate p;
			if (type == CLASS_TYPE) {

				boolean exists = false;
				boolean inverse = false;

				// ExistentialNode
				if (uri.startsWith("E")) {
					exists = true;
					uri = uri.substring(1);
				}
				// Inverse
				if (uri.endsWith("-")) {
					uri = uri.substring(0, uri.length() - 2);
					inverse = true;
				}
				ClassDescription description;

				if (exists) {
					p = predicateFactory.getPredicate(URI.create(uri), 2);
					description = descFactory.getPropertySomeRestriction(p, inverse);
				} else {
					p = predicateFactory.getClassPredicate(URI.create(uri));
					description = descFactory.createClass(p);
				}

				if (res_classes.containsKey(description)) {
					res_classes.get(description).getRange().addInterval(start_idx, end_idx);
				} else {
					DAGNode node = new DAGNode(description);
					node.setIndex(idx);
					node.setRange(new SemanticIndexRange(start_idx, end_idx));
					res_classes.put(description, node);
					res_allnodes.put(description, node);
				}

			} else if (type == ROLE_TYPE) {

				Property description;
				boolean inverse = false;

				// Inverse
				if (uri.endsWith("-")) {
					uri = uri.substring(0, uri.length() - 2);
					inverse = true;
				}
				p = predicateFactory.getObjectPropertyPredicate(URI.create(uri));
				description = descFactory.createProperty(p, inverse);

				if (res_roles.containsKey(description)) {
					res_roles.get(description).getRange().addInterval(start_idx, end_idx);
				} else {
					DAGNode node = new DAGNode(description);
					node.setIndex(idx);
					node.setRange(new SemanticIndexRange(start_idx, end_idx));
					res_roles.put(description, node);
					res_allnodes.put(description, node);
				}
			}
		}

		dag = new DAG(res_classes, res_roles, new HashMap<Description, Description>(), res_allnodes);
		pureIsa = DAGConstructor.filterPureISA(dag);
	}

	@Override
	public boolean checkMetadata() throws SQLException {
		return true;
	}

	@Override
	public Collection<OBDAMappingAxiom> getMappings() {

		/*
		 * 
		 * PART 1: Collecting relevant nodes for mappings
		 */

		/*
		 * Collecting relevant nodes for each role. For a Role P, the relevant
		 * nodes are, the DAGNode for P, and the top most inverse children of P
		 */
		DAGOperations.buildDescendants(dag);
		
		try {
			GraphGenerator.dumpISA(dag,"sidag");
		} catch (IOException e) {
//			e.printStackTrace();
		}
		
		Set<DAGNode> roleNodes = new HashSet<DAGNode>();
		Map<DAGNode, List<DAGNode>> roleInverseMaps = new HashMap<DAGNode, List<DAGNode>>();

		Set<Predicate> roles = ontology.getRoles();
		for (Predicate rolepred : roles) {

			DAGNode node = dag.getRoleNode(descFactory.createProperty(rolepred));
			// We only map named roles
			if (!(node.getDescription() instanceof Property) || ((Property) node.getDescription()).isInverse()) {
				continue;
			}
			roleNodes.add(node);

			List<DAGNode> roleInverseChildren = roleInverseMaps.get(node);
			if (roleInverseChildren == null) {
				roleInverseChildren = new LinkedList<DAGNode>();
				roleInverseMaps.put(node, roleInverseChildren);
			}

			/*
			 * collecting the top most inverse children, we do a bredth first
			 * traversal, stopping a branch when we find an inverse child.
			 * 
			 * Collecting the top most allows us to avoid redundancy elimination
			 */
			Queue<DAGNode> childrenQueue = new LinkedList<DAGNode>();
			childrenQueue.addAll(node.getChildren());
			childrenQueue.addAll(node.getEquivalents());

			while (!childrenQueue.isEmpty()) {
				DAGNode child = childrenQueue.poll();
				if ((child.getDescription() instanceof Property) && ((Property) child.getDescription()).isInverse()) {
					roleInverseChildren.add(child);
				} else {
					childrenQueue.addAll(child.getChildren());
				}
			}

			/* Removing redundant nodes */

			HashSet<DAGNode> inverseRedundants = new HashSet<DAGNode>();
			for (DAGNode inverseNode : roleInverseChildren) {
				Property role = ((Property) inverseNode.getDescription());
				for (DAGNode possibleRedundantNode : roleInverseChildren) {
					Property possibleRedundantRole = ((Property) possibleRedundantNode.getDescription());
					if (dag.getRoleNode(role).getDescendants().contains(possibleRedundantRole))
						inverseRedundants.add(possibleRedundantNode);
				}
			}
			roleInverseChildren.removeAll(inverseRedundants);

		}

		/*
		 * Collecting relevant nodes for each class, that is, the Node itself,
		 * and each exists R such that there is no other exists P, such that R
		 * isa P
		 * 
		 * Here we cannot collect only the top most, so we do it in two passes.
		 * First we callect all exsts R children, then we remove redundant ones.
		 */

		// TODO this part can be optimized if we know some existing dependencies
		// (e.g., coming from given mappings)

		Set<DAGNode> classNodesMaps = new HashSet<DAGNode>();
		Map<DAGNode, Set<DAGNode>> classExistsMaps = new HashMap<DAGNode, Set<DAGNode>>();
		for (DAGNode node : dag.getClasses()) {
			// we only map named classes
			if (!(node.getDescription() instanceof OClass)) {
				continue;
			}
			classNodesMaps.add(node);

			Set<DAGNode> existChildren = classExistsMaps.get(node);
			if (existChildren == null) {
				existChildren = new HashSet<DAGNode>();
				classExistsMaps.put(node, existChildren);
			}

			/* Collecting Exists R children */
			for (DAGNode child : node.getDescendants()) {
				if (child.getDescription() instanceof PropertySomeRestrictionImpl) {
					existChildren.add(child);
				}
			}

			/*
			 * Cleaning exists children (removing any exists R implied by the
			 * role hierarchy )
			 */
			// Set<DAGNode> existChildren = classExistsMaps.get(node);
			Set<DAGNode> redundantNodes = new HashSet<DAGNode>();
			for (DAGNode existsnode : existChildren) {
				/* Here we have ES */
				PropertySomeRestriction existsDesc = (PropertySomeRestriction) existsnode.getDescription();
				Property role = descFactory.createProperty(existsDesc.getPredicate(), existsDesc.isInverse());
				DAGNode roleNode = dag.getRoleNode(role);

				for (DAGNode possiblyRedundantNode : existChildren) {
					/* Here we have ER */
					PropertySomeRestriction existsDesc2 = (PropertySomeRestriction) possiblyRedundantNode.getDescription();
					Property role2 = descFactory.createProperty(existsDesc2.getPredicate(), existsDesc2.isInverse());
					DAGNode roleNode2 = dag.getRoleNode(role2);

					if (roleNode.getDescendants().contains(roleNode2))
						/*
						 * The DAG implies that R ISA S, so we remove ER
						 */
						redundantNodes.add(possiblyRedundantNode);
				}
			}
			existChildren.removeAll(redundantNodes);
		}

		/*
		 * We collected all classes and properties that need mappings, and the
		 * nodes that are relevant for each of their mappings
		 */

		/*
		 * 
		 * PART 2: Creating the mappings
		 * 
		 * 
		 * Note, at every step we always use the pureIsa dag to get the indexes
		 * and ranges for each class.
		 */

		/* Creating the mappings for each role */

		Map<Predicate, List<OBDAMappingAxiom>> mappings = new HashMap<Predicate, List<OBDAMappingAxiom>>();

		for (DAGNode roleNode : roleNodes) {

			Predicate role = ((Property) roleNode.getDescription()).getPredicate();

			List<OBDAMappingAxiom> currentMappings = new LinkedList<OBDAMappingAxiom>();

			mappings.put(role, currentMappings);

			// Mapping head

			Atom head = predicateFactory.getAtom(predicateFactory.getPredicate(URI.create("m"), 2), predicateFactory.getVariable("X"),
					predicateFactory.getVariable("Y"));
			Atom body = predicateFactory.getAtom(role, predicateFactory.getVariable("X"), predicateFactory.getVariable("Y"));

			/*
			 * This target query is shared by all mappings for this role
			 */

			CQIE targetQuery = predicateFactory.getCQIE(head, body);

			/*
			 * Getting the indexed node (from the pureIsa dag)
			 */

			DAGNode indexedNode = pureIsa.getRoleNode((Property) roleNode.getDescription());

			/*
			 * First mapping: Getting the SQL for the *BASIC* mapping using
			 * ranges
			 */

			StringBuffer sql = new StringBuffer();
			sql.append(select_mapping_role);

			sql.append(" WHERE ");

			List<Interval> intervals = indexedNode.getRange().getIntervals();
			appendIntervalString(intervals.get(0), sql);

			for (int intervali = 1; intervali < intervals.size(); intervali++) {
				sql.append(" OR ");
				appendIntervalString(intervals.get(intervali), sql);
			}

			OBDAMappingAxiom basicmapping = predicateFactory.getRDBMSMappingAxiom(sql.toString(), targetQuery);
			currentMappings.add(basicmapping);

			/* Rest mappings: computing mappings for inverses */

			if (roleInverseMaps.get(roleNode).size() > 0) {
				sql = new StringBuffer();
				sql.append(select_mapping_role_inverse);
				sql.append(" WHERE ");

				boolean alreadyAppendedOne = false;

				for (DAGNode inverseSubNodes : roleInverseMaps.get(roleNode)) {

					/*
					 * Getting the indexed node (from the pureIsa dag)
					 */

					Property inverseRole = (Property) inverseSubNodes.getDescription();
					Property directRole = descFactory.createProperty(inverseRole.getPredicate());

					indexedNode = pureIsa.getRoleNode(directRole);

					if (indexedNode != null) {
						intervals = indexedNode.getRange().getIntervals();

						for (int intervali = 0; intervali < intervals.size(); intervali++) {
							if (alreadyAppendedOne)
								sql.append(" OR ");
							appendIntervalString(intervals.get(intervali), sql);
							alreadyAppendedOne = true;
						}
					}

				}
				if (alreadyAppendedOne) {
					OBDAMappingAxiom inverseMapping = predicateFactory.getRDBMSMappingAxiom(sql.toString(), targetQuery);
					currentMappings.add(inverseMapping);
				}
			}

			/*
			 * Generating mappings for the equivalent nodes
			 */

			for (DAGNode equivalent : roleNode.getEquivalents()) {

				Property equiproperty = (Property) equivalent.getDescription();

				if (equiproperty.isInverse()) {
					Property directEquiProperty = descFactory.createProperty(equiproperty.getPredicate(), false);
					if ((pureIsa.getRoleNode(directEquiProperty) != null) && (pureIsa.getRoleNode(directEquiProperty).getIndex() != -1))
						continue;
				}

				Atom headequi = predicateFactory.getAtom(predicateFactory.getPredicate(URI.create("m"), 2),
						predicateFactory.getVariable("X"), predicateFactory.getVariable("Y"));

				Atom bodyequi = null;
				if (!equiproperty.isInverse()) {
					bodyequi = predicateFactory.getAtom(equiproperty.getPredicate(), predicateFactory.getVariable("X"),
							predicateFactory.getVariable("Y"));
				} else {
					bodyequi = predicateFactory.getAtom(equiproperty.getPredicate(), predicateFactory.getVariable("Y"),
							predicateFactory.getVariable("X"));
				}

				CQIE targetQueryEqui = predicateFactory.getCQIE(headequi, bodyequi);

				List<OBDAMappingAxiom> equimappings = new LinkedList<OBDAMappingAxiom>();
				mappings.put(equiproperty.getPredicate(), equimappings);

				for (OBDAMappingAxiom mapping : currentMappings) {
					equimappings.add(predicateFactory.getRDBMSMappingAxiom(mapping.getSourceQuery().toString(), targetQueryEqui));
				}

			}
		}

		/*
		 * Creating mappings for each concept
		 */

		for (DAGNode classNode : classNodesMaps) {

			Predicate classuri = ((OClass) classNode.getDescription()).getPredicate();

			List<OBDAMappingAxiom> currentMappings = new LinkedList<OBDAMappingAxiom>();

			mappings.put(classuri, currentMappings);

			// Mapping head

			Atom head = predicateFactory.getAtom(predicateFactory.getPredicate(URI.create("m"), 1), predicateFactory.getVariable("X"));
			Atom body = predicateFactory.getAtom(classuri, predicateFactory.getVariable("X"));

			/*
			 * This target query is shared by all mappings for this role
			 */

			CQIE targetQuery = predicateFactory.getCQIE(head, body);

			/*
			 * First mapping: Getting the SQL for the *BASIC* mapping using
			 * ranges
			 */

			StringBuffer sql = new StringBuffer();
			sql.append(select_mapping_class);
			sql.append(" WHERE ");

			/*
			 * Getting the indexed node (from the pureIsa dag)
			 */
			DAGNode indexedNode = pureIsa.getClassNode((OClass) classNode.getDescription());
			List<Interval> intervals = indexedNode.getRange().getIntervals();
			appendIntervalString(intervals.get(0), sql);

			for (int intervali = 1; intervali < intervals.size(); intervali++) {
				sql.append(" OR ");
				appendIntervalString(intervals.get(intervali), sql);
			}

			OBDAMappingAxiom basicmapping = predicateFactory.getRDBMSMappingAxiom(sql.toString(), targetQuery);
			currentMappings.add(basicmapping);

			/* Rest mappings 1: computing mappings for all exists R children */

			// TODO this code is ugly because we are doing the direct and
			// inverse mapping at the same time
			// we need to split this into two blocks for readability

			StringBuffer sqlroledirect = new StringBuffer();
			StringBuffer sqlroleinverse = new StringBuffer();

			sqlroledirect.append(select_mapping_class_role_left);
			sqlroleinverse.append(select_mapping_class_role_right);

			sqlroledirect.append(" WHERE ");
			sqlroleinverse.append(" WHERE ");

			boolean alreadyAppendedOneDirect = false;
			boolean alreadyAppendedOneInverse = false;

			for (DAGNode existsSubNode : classExistsMaps.get(classNode)) {
				boolean direct = false;
				StringBuffer currentBuffer = null;
				if (!((PropertySomeRestriction) existsSubNode.getDescription()).isInverse()) {
					currentBuffer = sqlroledirect;
					direct = true;
				} else {
					currentBuffer = sqlroleinverse;
					direct = false;
				}

				/*
				 * Getting the indexed node (from the pureIsa dag)
				 */
				PropertySomeRestriction existsDesc = (PropertySomeRestriction) existsSubNode.getDescription();
				Property role = descFactory.createProperty(existsDesc.getPredicate(), false);

				indexedNode = pureIsa.getRoleNode(role);

				if (indexedNode != null) {
					intervals = indexedNode.getRange().getIntervals();

					for (int intervali = 0; intervali < intervals.size(); intervali++) {
						if ((direct && alreadyAppendedOneDirect) || ((!direct && alreadyAppendedOneInverse)))
							currentBuffer.append(" OR ");
						appendIntervalString(intervals.get(intervali), currentBuffer);
						if (direct)
							alreadyAppendedOneDirect = true;
						else
							alreadyAppendedOneInverse = true;

					}
				}

			}

			if (alreadyAppendedOneDirect) {
				OBDAMappingAxiom existsMapping = predicateFactory.getRDBMSMappingAxiom(sqlroledirect.toString(), targetQuery);
				currentMappings.add(existsMapping);
			}
			if (alreadyAppendedOneInverse) {
				OBDAMappingAxiom existsMapping = predicateFactory.getRDBMSMappingAxiom(sqlroleinverse.toString(), targetQuery);
				currentMappings.add(existsMapping);
			}

			/*
			 * Generating mappings for the equivalent nodes
			 */

			for (DAGNode equivalent : classNode.getEquivalents()) {
				if (!(equivalent.getDescription() instanceof OClass))
					continue;

				OClass equiclass = (OClass) equivalent.getDescription();
				Atom headequi = predicateFactory.getAtom(predicateFactory.getPredicate(URI.create("m"), 1),
						predicateFactory.getVariable("X"));
				Atom bodyequi = predicateFactory.getAtom(equiclass.getPredicate(), predicateFactory.getVariable("X"));

				CQIE targetQueryEqui = predicateFactory.getCQIE(headequi, bodyequi);

				List<OBDAMappingAxiom> equimappings = new LinkedList<OBDAMappingAxiom>();
				mappings.put(equiclass.getPredicate(), equimappings);

				for (OBDAMappingAxiom mapping : currentMappings) {
					equimappings.add(predicateFactory.getRDBMSMappingAxiom(mapping.getSourceQuery().toString(), targetQueryEqui));
				}
			}
		}

		/*
		 * PART 4: Optimizing.
		 */

		// Merging multiple mappings into 1 with UNION ALL to minimize the
		// number of the mappings.

		if (mergeUniions) {
			for (Predicate predicate : mappings.keySet()) {

				List<OBDAMappingAxiom> currentMappings = mappings.get(predicate);

				/* Getting the current head */
				CQIE targetQuery = (CQIE) currentMappings.get(0).getTargetQuery();

				/* Computing the merged SQL */
				StringBuffer newSQL = new StringBuffer();
				newSQL.append(((OBDASQLQuery) currentMappings.get(0).getSourceQuery()).toString());
				for (int mapi = 1; mapi < currentMappings.size(); mapi++) {
					newSQL.append(" UNION ALL ");
					newSQL.append(((OBDASQLQuery) currentMappings.get(mapi).getSourceQuery()).toString());
				}

				/* Replacing the old mappings */
				OBDAMappingAxiom mergedMapping = predicateFactory.getRDBMSMappingAxiom(newSQL.toString(), targetQuery);
				currentMappings.clear();
				currentMappings.add(mergedMapping);

			}
		}

		/*
		 * Collecting the result
		 */
		Collection<OBDAMappingAxiom> result = new LinkedList<OBDAMappingAxiom>();
		for (Predicate predicate : mappings.keySet()) {
			log.debug("Predicate: {} Mappings: {}", predicate, mappings.get(predicate).size());
			result.addAll(mappings.get(predicate));
		}

		log.debug("Total: {} mappings", result.size());

		return result;

	}

	private void appendIntervalString(Interval interval, StringBuffer out) {
		if (interval.getStart() == interval.getEnd()) {
			out.append(String.format(whereSingleCondition, interval.getStart()));
		} else {
			out.append(String.format(whereIntervalCondition, interval.getStart(), interval.getEnd()));
		}
	}

	@Override
	public void collectStatistics() throws SQLException {

		conn.setAutoCommit(false);
		Statement st = conn.createStatement();

		st.addBatch(analyze);

		st.executeBatch();
		st.close();
		conn.commit();
	}

	@Override
	public void getDropDDL(OutputStream out) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void getMetadataSQLInserts(OutputStream outstream) throws IOException {

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));

		String insert_query = this.insert_query.replace("?", "%s");

		for (DAGNode node : dag.getClasses()) {

			ClassDescription description = (ClassDescription) node.getDescription();

			/*
			 * we always prefer the pureISA node since it can have extra data
			 * (indexes)
			 */
			DAGNode node2 = pureIsa.getClassNode(description);
			if (node2 != null) {
				node = node2;
			}

			String uri = description.toString();

			for (Interval it : node.getRange().getIntervals()) {

				out.append(String.format(insert_query, getQuotedString(uri), node.getIndex(), it.getStart(), it.getEnd(), CLASS_TYPE));
				out.append(";\n");
			}
		}

		for (DAGNode node : dag.getRoles()) {
			Property description = (Property) node.getDescription();

			/*
			 * we always prefer the pureISA node since it can have extra data
			 * (indexes)
			 */
			DAGNode node2 = pureIsa.getRoleNode(description);
			if (node2 != null) {
				node = node2;
			}

			String uri = description.toString();

			for (Interval it : node.getRange().getIntervals()) {
				out.append(String.format(insert_query, getQuotedString(uri), node.getIndex(), it.getStart(), it.getEnd(), ROLE_TYPE));
				out.append(";\n");
			}
		}

		out.flush();
	}

	@Override
	public void insertMetadata() throws SQLException {
		conn.setAutoCommit(false);

		PreparedStatement stm = conn.prepareStatement(insert_query);
		for (DAGNode node : dag.getClasses()) {

			ClassDescription description = (ClassDescription) node.getDescription();

			/*
			 * we always prefer the pureISA node since it can have extra data
			 * (indexes)
			 */
			DAGNode node2 = pureIsa.getClassNode(description);
			if (node2 != null) {
				node = node2;
			}

			String uri = description.toString();

			for (Interval it : node.getRange().getIntervals()) {
				stm.setString(1, uri);
				stm.setInt(2, node.getIndex());
				stm.setInt(3, it.getStart());
				stm.setInt(4, it.getEnd());
				stm.setInt(5, CLASS_TYPE);
				stm.addBatch();
			}
		}
		stm.executeBatch();

		for (DAGNode node : dag.getRoles()) {
			Property description = (Property) node.getDescription();

			/*
			 * we always prefer the pureISA node since it can have extra data
			 * (indexes)
			 */
			DAGNode node2 = pureIsa.getRoleNode(description);
			if (node2 != null) {
				node = node2;
			}

			String uri = description.toString();

			for (Interval it : node.getRange().getIntervals()) {
				stm.setString(1, uri);
				stm.setInt(2, node.getIndex());
				stm.setInt(3, it.getStart());
				stm.setInt(4, it.getEnd());
				stm.setInt(5, ROLE_TYPE);
				stm.addBatch();
			}
		}
		stm.executeBatch();

		conn.commit();

	}

	@Override
	public void setVocabulary(Set<Predicate> vocabulary) throws PunningException {
		// TODO

		/* This method should initialize the vocabulary of the DAG */

	}

	/*
	 * Utilities
	 */

	private String getQuotedString(String str) {
		StringBuffer bf = new StringBuffer();
		bf.append("'");
		bf.append(str);
		bf.append("'");
		return bf.toString();
	}

	private String getQuotedString(URI str) {
		StringBuffer bf = new StringBuffer();
		bf.append("'");
		bf.append(str.toString());
		bf.append("'");
		return bf.toString();
	}
}
