package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.dag.SemanticIndexRange;
import it.unibz.krdb.obda.owlrefplatform.core.dag.SemanticIndexRange.Interval;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Class;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleABoxAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2Translator;
import it.unibz.krdb.obda.owlrefplatform.exception.PunningException;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store ABox assertions in the DB
 * 
 * @author Sergejs Pugac
 */
public class RDBMSSIRepositoryManager implements RDBMSDataRepositoryManager {

	private final static Logger			log					= LoggerFactory.getLogger(RDBMSSIRepositoryManager.class);

	private Connection					conn				= null;

	private OBDADataSource					db					= null;

	public final static String			index_table			= "IDX";

	private final static String			create_ddl			= "CREATE TABLE " + index_table + " ( " + "URI VARCHAR(100), "
																	+ "IDX INTEGER, " + "IDX_FROM INTEGER, " + "IDX_TO INTEGER, "
																	+ "ENTITY_TYPE INTEGER" + ")";

	private final static String			drop_dll			= "DROP TABLE " + index_table + " IF EXISTS";

	private final static String			insert_query		= "INSERT INTO " + index_table
																	+ "(URI, IDX, IDX_FROM, IDX_TO, ENTITY_TYPE) VALUES(?, ?, ?, ?, ?)";

	private final static String			select_query		= "SELECT * FROM " + index_table;

	public static final String			class_table			= "class";

	public static final String			role_table			= "role";

	public static final String			class_table_create	= "CREATE TABLE " + class_table + " ( " + "URI VARCHAR(100)," + "IDX SMALLINT"
																	+ ")";

	public static final String			role_table_create	= "CREATE TABLE " + role_table + " ( " + "URI1 VARCHAR(100), "
																	+ "URI2 VARCHAR(100), " + "IDX SMALLINT" + ")";

	public static final String			class_table_drop	= "DROP TABLE IF EXISTS " + class_table;

	public static final String			role_table_drop		= "DROP TABLE IF EXISTS " + role_table;

	public static final String			class_insert		= "INSERT INTO " + class_table + " (URI, IDX) VALUES (?, ?)";

	public static final String			role_insert			= "INSERT INTO " + role_table + " (URI1, URI2, IDX) VALUES (?, ?, ?)";

	public static final String			indexclass1			= "CREATE INDEX idxclass1 ON " + class_table + "(URI)";

	public static final String			indexclass2			= "CREATE INDEX idxclass2 ON " + class_table + "(IDX)";

	public static final String			indexclass3			= "CREATE INDEX idxclass3 ON " + class_table + "(IDX, URI)";

	public static final String			indexclass4			= "CREATE INDEX idxclass4 ON " + class_table + "(URI, IDX)";

	public static final String			indexrole1			= "CREATE INDEX idxrole1 ON " + role_table + "(URI1)";

	public static final String			indexrole2			= "CREATE INDEX idxrole2 ON " + role_table + "(IDX)";

	public static final String			indexrole3			= "CREATE INDEX idxrole3 ON " + role_table + "(URI2)";

	public static final String			indexrole4			= "CREATE INDEX idxrole4 ON " + role_table + "(URI1, URI2)";

	public static final String			indexrole5			= "CREATE INDEX idxrole5 ON " + role_table + "(URI1, IDX)";

	public static final String			indexrole6			= "CREATE INDEX idxrole6 ON " + role_table + "(URI2, URI1)";

	public static final String			indexrole7			= "CREATE INDEX idxrole7 ON " + role_table + "(URI2, IDX)";

	public static final String			indexrole8			= "CREATE INDEX idxrole8 ON " + role_table + "(IDX, URI1)";

	public static final String			indexrole9			= "CREATE INDEX idxrole9 ON " + role_table + "(IDX, URI2)";

	public static final String			indexrole10			= "CREATE INDEX idxrole10 ON " + role_table + "(IDX, URI1, URI2)";

	public static final String			indexrole11			= "CREATE INDEX idxrole11 ON " + role_table + "(IDX, URI2, URI1)";

	public static final String			indexrole12			= "CREATE INDEX idxrole12 ON " + role_table + "(URI1, URI2, IDX)";

	public static final String			indexrole13			= "CREATE INDEX idxrole13 ON " + role_table + "(URI1, IDX, URI2)";

	public static final String			indexrole14			= "CREATE INDEX idxrole14 ON " + role_table + "(URI2, URI1, IDX)";

	public static final String			indexrole15			= "CREATE INDEX idxrole15 ON " + role_table + "(URI2, IDX, URI1)";

	public static final String			analyze				= "ANALYZE";

	private final OBDADataFactory		predicateFactory	= OBDADataFactoryImpl.getInstance();

	private final OntologyFactory	descFactory			= new OntologyFactoryImpl();

	private Properties					config				= null;

	private DAG							dag;

	private DAG							pureIsa;

	private DAG							sigmaDag;

	private Ontology					aboxDependencies;

	final static int					CLASS_TYPE			= 1;
	final static int					ROLE_TYPE			= 2;

	private static final boolean		mergeUniions		= true;

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
	public void setDatabase(OBDADataSource db) throws SQLException, ClassNotFoundException {
		this.db = db;
		conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(db);
	}

	@Override
	public void setTBox(Ontology ontology) {
		dag = DAGConstructor.getISADAG(ontology);
		pureIsa = DAGConstructor.filterPureISA(dag);
		aboxDependencies = DAGConstructor.getSigmaOntology(ontology);
		pureIsa.index();

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

				Predicate propPred = predicateFactory.getPredicate(URI.create(prop), 2);
				Property propDesc = descFactory.getProperty(propPred);
				DAGNode node = pureIsa.getRoleNode(propDesc);
				int idx = node.getIndex();

				out.append(String.format(role_insert_str, getQuotedString(uri), getQuotedString(lit), idx));

			} else if (ax instanceof RoleABoxAssertion) {

				RoleABoxAssertion roleABoxAssertion = (RoleABoxAssertion) ax;
				String prop = roleABoxAssertion.getRole().getName().toString();
				String uri1 = roleABoxAssertion.getFirstObject().getURI().toString();
				String uri2 = roleABoxAssertion.getSecondObject().getURI().toString();

				Predicate propPred = predicateFactory.getPredicate(URI.create(prop), 2);
				Property propDesc = descFactory.getProperty(propPred);

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
				if (!cls.equals(DAG.thingStr)) {
					String uri = ((ClassAssertion) ax).getObject().getURI().toString();

					Predicate clsPred = ((ClassAssertion) ax).getConcept();
					ClassDescription clsDesc = descFactory.getClass(clsPred);
					DAGNode node = pureIsa.getClassNode(clsDesc);
					int idx = node.getIndex();

					out.append(String.format(cls_insert_str, getQuotedString(uri), idx));

				}
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

				Predicate propPred = predicateFactory.getPredicate(URI.create(prop), 2);
				Property propDesc = descFactory.getProperty(propPred);
				DAGNode node = pureIsa.getRoleNode(propDesc);
				int idx = node.getIndex();

				role_stm.setString(1, uri);
				role_stm.setString(2, lit);
				role_stm.setInt(3, idx);
				role_stm.addBatch();

			} else if (ax instanceof RoleABoxAssertion) {

				RoleABoxAssertion roleABoxAssertion = (RoleABoxAssertion) ax;
				String prop = roleABoxAssertion.getRole().getName().toString();
				String uri1 = roleABoxAssertion.getFirstObject().getURI().toString();
				String uri2 = roleABoxAssertion.getSecondObject().getURI().toString();

				Predicate propPred = predicateFactory.getPredicate(URI.create(prop), 2);
				Property propDesc = descFactory.getProperty(propPred);

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

				role_stm.setString(1, uri1);
				role_stm.setString(2, uri2);
				role_stm.setInt(3, idx);
				role_stm.addBatch();

			} else if (ax instanceof ClassAssertion) {

				String cls = ((ClassAssertion) ax).getConcept().getName().toString();
				// XXX: strange behaviour - owlapi generates an extra
				// assertion of the form ClassAssertion(Thing, i)
				if (!cls.equals(DAG.thingStr)) {
					String uri = ((ClassAssertion) ax).getObject().getURI().toString();

					Predicate clsPred = ((ClassAssertion) ax).getConcept();
					ClassDescription clsDesc = descFactory.getClass(clsPred);
					DAGNode node = pureIsa.getClassNode(clsDesc);
					int idx = node.getIndex();

					cls_stm.setString(1, uri);
					cls_stm.setInt(2, idx);
					cls_stm.addBatch();
				}
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
					p = predicateFactory.getPredicate(URI.create(uri), 1);
					description = descFactory.getClass(p);
				}

				if (res_classes.containsKey(description)) {
					res_classes.get(description).getRange().addInterval(start_idx, end_idx);
				} else {
					DAGNode node = new DAGNode(description);
					node.setIndex(idx);
					node.setRange(new SemanticIndexRange(start_idx, end_idx));
					res_classes.put(description, node);
				}

			} else if (type == ROLE_TYPE) {

				Property description;
				boolean inverse = false;

				// Inverse
				if (uri.endsWith("-")) {
					uri = uri.substring(0, uri.length() - 2);
					inverse = true;
				}
				p = predicateFactory.getPredicate(URI.create(uri), 2);
				description = descFactory.getProperty(p, inverse);

				if (res_roles.containsKey(description)) {
					res_roles.get(description).getRange().addInterval(start_idx, end_idx);
				} else {
					DAGNode node = new DAGNode(description);
					node.setIndex(idx);
					node.setRange(new SemanticIndexRange(start_idx, end_idx));
					res_roles.put(description, node);
				}
			}
		}

		dag = new DAG(res_classes, res_roles, new HashMap<Description, Description>());
		pureIsa = DAGConstructor.filterPureISA(dag);
	}

	@Override
	public boolean checkMetadata() throws SQLException {
		return true;
	}

	@Override
	public Collection<OBDAMappingAxiom> getMappings() {
		try {
			List<MappingKey> keys = build(dag, pureIsa);
			return compile(keys);
		} catch (DuplicateMappingException e) {
			// TODO remove this Duplicate mapping exception form the API
			return null;
		}
	}

	/**
	 * Generate mappings for DAG
	 * 
	 * @throws DuplicateMappingException
	 *             error creating mappings
	 */
	private List<MappingKey> build(DAG dag, DAG pureIsa) throws DuplicateMappingException {
		log.debug("Generating mappings for DAG {}", pureIsa);

		List<MappingKey> mappings = new ArrayList<MappingKey>();

		for (DAGNode node : pureIsa.getClasses()) {

			if (!(node.getDescription() instanceof Class) || node.getDescription().equals(DAG.thingConcept)) {
				continue;
			}

			List<DAGNode> equiNodes = new ArrayList<DAGNode>(node.getEquivalents().size() + 1);
			equiNodes.add(node);
			equiNodes.addAll(node.getEquivalents());

			String tablename = RDBMSSIRepositoryManager.class_table;
			String projection = "URI as X";
			SemanticIndexRange range = node.getRange();

			for (DAGNode equiNode : equiNodes) {
				if (!(equiNode.getDescription() instanceof Class) || equiNode.getDescription().equals(DAG.thingConcept)) {
					continue;
				}
				Class equiDesc = (Class) equiNode.getDescription();
				String equiUri = equiDesc.getPredicate().getName().toString();

				mappings.add(new UnaryMappingKey(range, projection, tablename, equiUri));
			}

			// check if has child exists(R) in the general ISA DAG
			DAGNode genNode = dag.getClassNode((ClassDescription) node.getDescription());
			for (DAGNode descendant : genNode.descendans) {

				if (descendant.getDescription() instanceof PropertySomeRestriction) {
					SemanticIndexRange descRange;

					Predicate p = ((PropertySomeRestriction) descendant.getDescription()).getPredicate();
					if (p.getName().toString().startsWith(OWLAPI2Translator.AUXROLEURI)) {
						continue;
					}
					boolean isInverse = ((PropertySomeRestriction) descendant.getDescription()).isInverse();

					Property role = descFactory.getProperty(p, false);

					if (pureIsa.equi_mappings.containsKey(role)) {
						// XXX: Very dirty hack, needs to be redone
						continue;
					}

					if (isInverse) {
						projection = "URI2 as X";
					} else {
						projection = "URI1 as X";
					}

					descRange = pureIsa.getRoleNode(role).getRange();

					for (DAGNode equiNode : equiNodes) {
						if (!(equiNode.getDescription() instanceof Class)
								|| equiNode.getDescription().equals(DAG.thingConcept)) {
							continue;
						}
						Class equiDesc = (Class) equiNode.getDescription();
						String equiUri = equiDesc.getPredicate().getName().toString();

						if (isInverse) {
							mappings.add(new UnaryMappingKey(descRange, projection, RDBMSSIRepositoryManager.role_table, equiUri));
						} else {
							mappings.add(new UnaryMappingKey(descRange, projection, RDBMSSIRepositoryManager.role_table, equiUri));
						}
					}
				}
			}
		}
		for (DAGNode node : dag.getRoles()) {

			Property nodeDesc = (Property) node.getDescription();
			if (nodeDesc.getPredicate().getName().toString().startsWith(OWLAPI2Translator.AUXROLEURI)) {
				continue;
			}
			if (nodeDesc.isInverse()) {
				continue;
			}

			SemanticIndexRange range = pureIsa.getRoleNode(descFactory.getProperty(nodeDesc.getPredicate(), false)).getRange();
			String projection = "URI1 as X, URI2 as Y";
			mappings.add(new BinaryMappingKey(range, projection, RDBMSSIRepositoryManager.role_table, nodeDesc.getPredicate().getName()
					.toString()));

			for (DAGNode equiNode : node.getEquivalents()) {

				Property equiNodeDesc = (Property) equiNode.getDescription();

				if (equiNodeDesc.isInverse()) {
					projection = "URI1 as Y, URI2 as X";
				}
				mappings.add(new BinaryMappingKey(range, projection, RDBMSSIRepositoryManager.role_table, equiNodeDesc.getPredicate()
						.getName().toString()));
			}

			for (DAGNode child : node.getChildren()) {
				Property childDesc = (Property) child.getDescription();

				if (childDesc.getPredicate().getName().toString().startsWith(OWLAPI2Translator.AUXROLEURI)) {
					continue;
				}

				if (!childDesc.isInverse()) {
					continue;
				}
				Property posChildDesc = descFactory.getProperty(childDesc.getPredicate(), false);

				SemanticIndexRange childRange = pureIsa.getRoleNode(posChildDesc).getRange();

				mappings.add(new BinaryMappingKey(childRange, "URI1 as Y, URI2 as X", RDBMSSIRepositoryManager.role_table, nodeDesc
						.getPredicate().getName().toString()));

				for (DAGNode equiNode : node.getEquivalents()) {

					Property equiNodeDesc = (Property) equiNode.getDescription();
					String equiProj = "URI1 as Y, URI2 as X";
					if (equiNodeDesc.isInverse()) {
						equiProj = "URI1 as X, URI2 as Y";
					}

					mappings.add(new BinaryMappingKey(childRange, equiProj, RDBMSSIRepositoryManager.role_table, equiNodeDesc
							.getPredicate().getName().toString()));
				}
			}
		}

		return mappings;
		// return compile(mappings);
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

	private String genQuerySQL(MappingKey map) {
		// Generate the WHERE clause
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append(map.projection);
		sql.append(" FROM ");
		sql.append(map.table);
		sql.append(" WHERE ");

		for (SemanticIndexRange.Interval it : map.range.getIntervals()) {
			int st = it.getStart();
			int end = it.getEnd();
			String interval;
			if (st == end) {
				interval = String.format("(IDX = %d) OR ", st);
			} else {
				interval = String.format("((IDX >= %d) AND ( IDX <= %d)) OR ", st, end);
			}
			sql.append(interval);
		}
		if (map.range.getIntervals().size() != 0) {
			// remove the last AND
			sql.delete(sql.length() - 4, sql.length());
		}

		return sql.toString();
	}

	public List<OBDAMappingAxiom> compile(List<MappingKey> mappings) throws DuplicateMappingException {

		List<OBDAMappingAxiom> rv = new ArrayList<OBDAMappingAxiom>(128);
		List<MappingKey> mergedElements = new ArrayList<MappingKey>(128);
		Collections.sort(mappings);

		List<List<MappingKey>> mergedGroup = new ArrayList<List<MappingKey>>();
		List<MappingKey> firstGroup = new ArrayList<MappingKey>();
		firstGroup.add(mappings.get(0));
		mergedGroup.add(firstGroup);

		for (MappingKey map : mappings) {
			List<MappingKey> uriTabel = mergedGroup.get(mergedGroup.size() - 1);
			MappingKey lastElem = uriTabel.get(uriTabel.size() - 1);
			if (lastElem.uri.equals(map.uri) && lastElem.projection.equals(map.projection)) {
				uriTabel.add(map);
			} else {
				List<MappingKey> group = new ArrayList<MappingKey>();
				group.add(map);
				mergedGroup.add(group);
			}
		}
		for (List<MappingKey> group : mergedGroup) {
			MappingKey cur = group.get(0);
			SemanticIndexRange curRange = new SemanticIndexRange(cur.range);
			for (MappingKey map : group) {
				curRange.addRange(map.range);
			}
			if (mergeUniions) {
				if (cur instanceof UnaryMappingKey) {
					mergedElements.add(new UnaryMappingKey(curRange, cur.projection, cur.table, cur.uri));
				} else if (cur instanceof BinaryMappingKey) {
					mergedElements.add(new BinaryMappingKey(curRange, cur.projection, cur.table, cur.uri));
				}
			} else {
				if (cur instanceof UnaryMappingKey) {
					rv.add(makeUnaryMapp(cur.uri, genQuerySQL(new UnaryMappingKey(curRange, cur.projection, cur.table, cur.uri))));
				} else if (cur instanceof BinaryMappingKey) {
					rv.add(makeBinaryMapp(cur.uri, genQuerySQL(new BinaryMappingKey(curRange, cur.projection, cur.table, cur.uri))));
				}
			}
		}
		if (mergeUniions) {

			mergedGroup = new ArrayList<List<MappingKey>>();
			firstGroup = new ArrayList<MappingKey>();
			firstGroup.add(mergedElements.get(0));
			mergedGroup.add(firstGroup);

			for (int i = 1; i < mappings.size(); ++i) {
				MappingKey map = mappings.get(i);
				List<MappingKey> uriTabel = mergedGroup.get(mergedGroup.size() - 1);
				MappingKey lastElem = uriTabel.get(uriTabel.size() - 1);
				if (lastElem.uri.equals(map.uri)
						&& ((lastElem instanceof UnaryMappingKey && map instanceof UnaryMappingKey) || (lastElem instanceof BinaryMappingKey
								&& map instanceof BinaryMappingKey && lastElem.projection.equals(map.projection)))) {
					uriTabel.add(map);
				} else {
					List<MappingKey> group = new ArrayList<MappingKey>();
					group.add(map);
					mergedGroup.add(group);
				}
			}
			for (List<MappingKey> group : mergedGroup) {
				MappingKey cur = group.get(0);
				StringBuilder sql = new StringBuilder();
				sql.append(genQuerySQL(cur));

				for (int j = 1; j < group.size(); ++j) {
					MappingKey map = group.get(j);
					sql.append(" UNION ALL ");
					sql.append(genQuerySQL(map));
				}
				if (cur instanceof UnaryMappingKey) {
					rv.add(makeUnaryMapp(cur.uri, sql.toString()));
				} else if (cur instanceof BinaryMappingKey) {
					rv.add(makeBinaryMapp(cur.uri, sql.toString()));
				}
			}
		}
		return rv;
	}

	private OBDAMappingAxiom makeBinaryMapp(String uri, String sql) {
		Term qtx = predicateFactory.getVariable("X");
		Term qty = predicateFactory.getVariable("Y");
		Predicate predicate = predicateFactory.getPredicate(URI.create(uri), 2);
		Atom bodyAtom = predicateFactory.getAtom(predicate, qtx, qty);
		predicate = predicateFactory.getPredicate(URI.create("q"), 2);
		Atom head = predicateFactory.getAtom(predicate, qtx, qty);
		OBDAQuery cq = predicateFactory.getCQIE(head, bodyAtom);

		return predicateFactory.getRDBMSMappingAxiom(sql, cq);

	}

	private OBDAMappingAxiom makeUnaryMapp(String uri, String sql) {
		Term qt = predicateFactory.getVariable("x");
		Predicate predicate = predicateFactory.getPredicate(URI.create(uri), 1);
		Atom bodyAtom = predicateFactory.getAtom(predicate, qt);
		predicate = predicateFactory.getPredicate(URI.create("q"), 1);
		Atom head = predicateFactory.getAtom(predicate, qt);
		OBDAQuery cq = predicateFactory.getCQIE(head, bodyAtom);

		return predicateFactory.getRDBMSMappingAxiom(sql, cq);

	}

	public class MappingKey implements Comparable<MappingKey> {

		public final SemanticIndexRange	range;
		public final String				projection;
		public final String				table;
		public final String				uri;

		public MappingKey(SemanticIndexRange range, String projection, String table, String uri) {
			this.range = range;
			this.projection = projection;
			this.table = table;
			this.uri = uri;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			MappingKey that = (MappingKey) o;

			if (projection != null ? !projection.equals(that.projection) : that.projection != null)
				return false;
			if (range != null ? !range.equals(that.range) : that.range != null)
				return false;
			if (table != null ? !table.equals(that.table) : that.table != null)
				return false;
			if (uri != null ? !uri.equals(that.uri) : that.uri != null)
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = range != null ? range.hashCode() : 0;
			result = 31 * result + (projection != null ? projection.hashCode() : 0);
			result = 31 * result + (table != null ? table.hashCode() : 0);
			result = 31 * result + (uri != null ? uri.hashCode() : 0);
			return result;
		}

		@Override
		public int compareTo(MappingKey mappingKey) {
			int i = this.uri.compareTo(mappingKey.uri);
			if (i != 0) {
				return i;
			}
			return this.projection.compareTo(mappingKey.projection);
		}
	}

	private class UnaryMappingKey extends MappingKey {

		UnaryMappingKey(SemanticIndexRange range, String projection, String table, String uri) {
			super(range, projection, table, uri);
		}
	}

	private class BinaryMappingKey extends MappingKey {

		BinaryMappingKey(SemanticIndexRange range, String projection, String table, String uri) {
			super(range, projection, table, uri);
		}
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
