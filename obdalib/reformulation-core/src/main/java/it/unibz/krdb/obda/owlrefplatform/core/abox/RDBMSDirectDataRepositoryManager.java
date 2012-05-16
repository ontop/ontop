package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.gui.swing.utils.OBDAProgressListener;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.PunningException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.reasoner.IllegalParameterException;

public class RDBMSDirectDataRepositoryManager implements RDBMSDataRepositoryManager, OBDAProgressListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6649716982554168637L;

	private transient Connection conn = null;
	// private APIController apic = null;
	private transient List<ABoxDumpListener> listener = null;

	private OBDADataSource db = null;
	// private int indexcounter = 1;
	//
	private boolean isCanceled = false;

	// TODO remove this
	private transient Statement statement = null;

	private static final Logger log = LoggerFactory.getLogger(RDBMSDirectDataRepositoryManager.class);

	private Map<Predicate, String> predicatetableMap = new HashMap<Predicate, String>();

	private Map<String, Predicate> uriPredicateMap = new HashMap<String, Predicate>();

	private Properties config = null;

	private static OBDADataFactory obdaFactory = OBDADataFactoryImpl.getInstance();

	private final String strtabledata = "QUEST_%s";

	final String strtablemetada = "QUEST_METADATA_DIRECT_MAPPING";

	final String strcreate_table_class = "CREATE TABLE " + strtabledata + " (TERM0 VARCHAR(1000))";

	final String strcreate_table_property = "CREATE TABLE " + strtabledata + " (TERM0 VARCHAR(1000), TERM1 VARCHAR(1000))";

	final String strcreate_index_class = "CREATE INDEX IDX%s ON " + strtabledata + " (TERM0)";

	final String strcreate_index_property_1 = "CREATE INDEX IDX1%s ON " + strtabledata + " (TERM0, TERM1)";

	final String strcreate_index_property_2 = "CREATE INDEX IDX2%s ON " + strtabledata + " (TERM1, TERM0)";

	final String strdrop_index_class = "DROP INDEX IDX%s";

	final String strdrop_index_property_1 = "DROP INDEX IDX1%s";

	final String strdrop_index_property_2 = "DROP INDEX IDX2%s";

	final String strcreate_meta_table = "CREATE TABLE " + strtablemetada
			+ " (URI VARCHAR(1000) NOT NULL, TYPE VARCHAR(1000) NOT NULL, TABLENAME VARCHAR(1000) NOT NULL)";

	final String strinsert_meta_table = "INSERT INTO " + strtablemetada + " VALUES ('%s', '%s', '%s')";

	final String strinsert_table_class = "INSERT INTO " + strtabledata + " VALUES ('%s')";

	final String strinsert_table_property = "INSERT INTO " + strtabledata + " VALUES ('%s', '%s')";

	final String strselect_table_class = "SELECT TERM0 FROM " + strtabledata + "";

	final String strselect_table_property = "SELECT TERM0, TERM1 FROM " + strtabledata + "";

	final String strdrop_table_class = "DROP TABLE " + strtabledata + "";

	final String strdrop_meta_table = "DROP TABLE " + strtablemetada + "";

	final String stranalyze = "ANALYZE";

	final String strselect_meta_table = "SELECT URI, TYPE, TABLENAME FROM " + strtablemetada + "";

	private Set<Predicate> vocabulary;

	private static OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	private boolean isIndexed;

	public RDBMSDirectDataRepositoryManager() throws PunningException {
		this(null);
	}

	public RDBMSDirectDataRepositoryManager(Set<Predicate> vocabulary) throws PunningException {
		if (vocabulary != null) {
			setVocabulary(vocabulary);
		}
		listener = new Vector<ABoxDumpListener>();
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

	@Override
	public void actionCanceled() throws SQLException {
		isCanceled = true;
		statement.cancel();
		statement.close();
	}

	@Override
	public void setConfig(Properties config) {
		this.config = config;
	}

	@Override
	public void setTBox(Ontology ontology) {
	}

	@Override
	public String getType() {
		return TYPE_DIRECT;
	}

	@Override
	public void getTablesDDL(OutputStream outstream) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));

		out.append(strcreate_meta_table);
		out.append(";\n");

		for (Predicate predicate : predicatetableMap.keySet()) {
			if (predicate.getArity() == 1) {
				out.append(String.format(strcreate_table_class, predicatetableMap.get(predicate)));
				out.append(";\n");
			} else if (predicate.getArity() == 2) {
				out.append(String.format(strcreate_table_property, predicatetableMap.get(predicate)));
				out.append(";\n");
			} else {
				throw new RuntimeException("Unsupported predicate: " + predicate);
			}
		}

		out.flush();
	}

	@Override
	public void getIndexDDL(OutputStream outstream) throws IOException {

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));

		for (Predicate predicate : predicatetableMap.keySet()) {
			if (predicate.getArity() == 1) {
				out.append(String.format(strcreate_index_class, predicatetableMap.get(predicate), predicatetableMap.get(predicate)));
				out.append(";\n");
			} else if (predicate.getArity() == 2) {
				out.append(String.format(strcreate_index_property_1, predicatetableMap.get(predicate), predicatetableMap.get(predicate)));
				out.append(";\n");
				out.append(String.format(strcreate_index_property_2, predicatetableMap.get(predicate), predicatetableMap.get(predicate)));
				out.append(";\n");
			} else {
				throw new RuntimeException("Unsupported predicate: " + predicate);
			}
		}

		out.flush();

	}

	@Override
	public void getDropDDL(OutputStream outstream) throws IOException {

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));

		for (Predicate predicate : predicatetableMap.keySet()) {
			out.append(String.format(strdrop_table_class, predicatetableMap.get(predicate)));
			out.append(";\n");
		}
		out.append(strdrop_meta_table);
		out.append(";\n");
		out.flush();

	}

	@Override
	public void getMetadataSQLInserts(OutputStream outstream) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));

		/*
		 * Generating the inserts for the metadata table
		 */
		for (Predicate predicate : predicatetableMap.keySet()) {
			if (predicate.getArity() == 1) {
				out.append(String.format(strinsert_meta_table, predicate.getName(), "CONCEPT", escaped(predicatetableMap.get(predicate))));
				out.append(";\n");
			} else if (predicate.getType(1) == COL_TYPE.OBJECT) {
				out.append(String.format(strinsert_meta_table, predicate.getName(), "OBJECTPROPERTY",
						escaped(predicatetableMap.get(predicate))));
				out.append(";\n");
			} else if (predicate.getType(1) == COL_TYPE.LITERAL) {
				out.append(String.format(strinsert_meta_table, predicate.getName(), "DATAPROPERTY",
						escaped(predicatetableMap.get(predicate))));
				out.append(";\n");
			} else {
				throw new RuntimeException("Unsupported predicate: " + predicate);
			}
		}

		out.flush();
	}

	@Override
	public void getSQLInserts(Iterator<Assertion> data, OutputStream outstream) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));

		/*
		 * Generating the inserts for the ABox data
		 */

		while (data.hasNext()) {
			Axiom assertion = data.next();
			if (assertion instanceof ClassAssertion) {
				ClassAssertion cassertion = (ClassAssertion) assertion;
				out.append(String.format(strinsert_table_class, predicatetableMap.get(cassertion.getConcept()), cassertion.getObject()
						.getURI()));
				out.append(";\n");
			} else if (assertion instanceof ObjectPropertyAssertion) {
				ObjectPropertyAssertion rassertion = (ObjectPropertyAssertion) assertion;
				out.append(String.format(strinsert_table_property, predicatetableMap.get(rassertion.getRole()), rassertion.getFirstObject()
						.getURI(), rassertion.getSecondObject().getURI()));
				out.append(";\n");
			} else if (assertion instanceof DataPropertyAssertion) {
				DataPropertyAssertion rassertion = (DataPropertyAssertion) assertion;
				out.append(String.format(strinsert_table_property, predicatetableMap.get(rassertion.getAttribute()), rassertion.getObject()
						.getURI(), escaped(rassertion.getValue().getValue())));
				out.append(";\n");
			}
		}

		out.flush();
	}

	// @Override
	// public void getCSVInserts(Iterator<Assertion> data, OutputStream out)
	// throws IOException {
	// // TODO Auto-generated method stub
	//
	// }

	@Override
	public void createDBSchema(Connection conn, boolean dropExisting) throws SQLException {

		if (isDBSchemaDefined(conn)) {
			log.debug("Schema already exists. Skipping creation");
			return;
		}

		if (dropExisting) {
			try {
				dropDBSchema(conn);
			} catch (SQLException e) {
				log.debug(e.getMessage());
			}
		}

		Statement st = conn.createStatement();

		st.addBatch(strcreate_meta_table);

		for (Predicate predicate : predicatetableMap.keySet()) {
			if (predicate.getArity() == 1) {
				st.addBatch(String.format(strcreate_table_class, predicatetableMap.get(predicate)));
			} else if (predicate.getArity() == 2) {
				st.addBatch(String.format(strcreate_table_property, predicatetableMap.get(predicate)));
			} else {
				throw new RuntimeException("Unsupported predicate: " + predicate);
			}
		}

		st.executeBatch();
		st.close();

	}

	@Override
	public void createIndexes(Connection conn) throws SQLException {
		Statement st = conn.createStatement();

		for (Predicate predicate : predicatetableMap.keySet()) {
			if (predicate.getArity() == 1) {
				st.addBatch(String.format(strcreate_index_class, predicatetableMap.get(predicate), predicatetableMap.get(predicate)));
			} else if (predicate.getArity() == 2) {
				st.addBatch(String.format(strcreate_index_property_1, predicatetableMap.get(predicate), predicatetableMap.get(predicate)));
				st.addBatch(String.format(strcreate_index_property_2, predicatetableMap.get(predicate), predicatetableMap.get(predicate)));
			} else {
				throw new RuntimeException("Unsupported predicate: " + predicate);
			}
		}

		st.executeBatch();
		st.close();

		isIndexed = true;

	}

	@Override
	public void dropDBSchema(Connection conn) throws SQLException {

		log.debug("Droping existing tables");
		Map<Predicate, String> tempPredicatetableMap = new HashMap<Predicate, String>();
		Map<String, Predicate> tempUriPredicateMap = new HashMap<String, Predicate>();

		this.loadMetadataFromDB(conn, tempPredicatetableMap, tempUriPredicateMap);

		Statement st = conn.createStatement();

		for (Predicate predicate : tempPredicatetableMap.keySet()) {
			st.addBatch(String.format(strdrop_table_class, tempPredicatetableMap.get(predicate)));
			log.debug("Droping: {}", tempPredicatetableMap.get(predicate));

		}
		log.debug("Droping: {}", strdrop_meta_table);

		st.addBatch(strdrop_meta_table);

		st.executeBatch();
		st.clearBatch();
		st.close();

	}

	@Override
	public void insertMetadata(Connection conn) throws SQLException {
		Statement st = conn.createStatement();

		/*
		 * Generating the inserts for the metadata table
		 */
		for (Predicate predicate : predicatetableMap.keySet()) {
			if (predicate.getArity() == 1) {
				st.addBatch(String.format(strinsert_meta_table, predicate.getName(), "CONCEPT", escaped(predicatetableMap.get(predicate))));
			} else if (predicate.getType(1) == COL_TYPE.OBJECT) {
				st.addBatch(String.format(strinsert_meta_table, predicate.getName(), "OBJECTPROPERTY",
						escaped(predicatetableMap.get(predicate))));
			} else if (predicate.getType(1) == COL_TYPE.LITERAL) {
				st.addBatch(String.format(strinsert_meta_table, predicate.getName(), "DATAPROPERTY",
						escaped(predicatetableMap.get(predicate))));
			} else {
				throw new RuntimeException("Unsupported predicate: " + predicate);
			}
		}
		st.executeBatch();
		st.close();

	}

	@Override
	public int insertData(Connection conn, Iterator<Assertion> data, int commit, int batch) throws SQLException {
		if (commit < 1) {
			commit = -1;
		}
		Statement st = conn.createStatement();
		/*
		 * Generating the inserts for the ABox data
		 */

		int totalcount = 0;

		int commitcount = 0;
		int batchCount = 0;
		while (data.hasNext()) {
			Axiom assertion = data.next();
			totalcount += 1;
			commitcount += 1;
			batchCount += 1;
			if (assertion instanceof ClassAssertion) {
				ClassAssertion cassertion = (ClassAssertion) assertion;
				if (predicatetableMap.get(cassertion.getConcept()) == null) {
					log.warn("WARNING: Found reference to an unknown Class/Property. We will ignore the assertion. Entity: {}",
							cassertion.getConcept());
					continue;
				}
				st.addBatch(String.format(strinsert_table_class, predicatetableMap.get(cassertion.getConcept()), cassertion.getObject()
						.getURI()));
			} else if (assertion instanceof ObjectPropertyAssertion) {
				ObjectPropertyAssertion rassertion = (ObjectPropertyAssertion) assertion;

				if (predicatetableMap.get(rassertion.getRole()) == null) {
					log.warn("WARNING: Found reference to an unknown Class/Property. We will ignore the assertion. Entity: {}",
							rassertion.getRole());
					continue;
				}

				st.addBatch(String.format(strinsert_table_property, predicatetableMap.get(rassertion.getRole()), rassertion
						.getFirstObject().getURI(), rassertion.getSecondObject().getURI()));

			} else if (assertion instanceof DataPropertyAssertion) {
				DataPropertyAssertion rassertion = (DataPropertyAssertion) assertion;

				if (predicatetableMap.get(rassertion.getAttribute()) == null) {
					log.warn("WARNING: Found reference to an unknown Class/Property. We will ignore the assertion. Entity: {}",
							rassertion.getAttribute());
					continue;
				}

				st.addBatch(String.format(strinsert_table_property, predicatetableMap.get(rassertion.getAttribute()), rassertion
						.getObject().getURI(), escaped(rassertion.getValue().getValue())));
			}

			if (batchCount == batch) {
				st.executeBatch();
				st.clearBatch();
				batchCount = 0;
			}

			
			if (commitcount == commit) {
				conn.commit();
				commitcount = 0;
			}
		}
		
		
		st.executeBatch();
		st.clearBatch();
		st.close();
		
		if (commit != -1)
			conn.commit();

		return totalcount;
	}

	@Override
	public Ontology getABoxDependencies() {
		return OntologyFactoryImpl.getInstance().createOntology(URI.create("fakeURI"));
	}

	@Override
	public Collection<OBDAMappingAxiom> getMappings() {

		List<OBDAMappingAxiom> mappings = new LinkedList<OBDAMappingAxiom>();
		int mappingcounter = 0;

		Predicate unaryq = obdaFactory.getPredicate(OBDALibConstants.QUERY_HEAD_URI, 1);
		Predicate binaryq = obdaFactory.getPredicate(OBDALibConstants.QUERY_HEAD_URI, 2);

		for (Predicate pred : predicatetableMap.keySet()) {

			mappingcounter = mappingcounter + 1;
			OBDAMappingAxiom map = null;

			if (pred.getArity() == 1) {
				Atom head = obdaFactory.getAtom(unaryq, obdaFactory.getVariable("TERM0"));
				Atom body = obdaFactory.getAtom(pred, obdaFactory.getVariable("TERM0"));
				OBDAQuery target = obdaFactory.getCQIE(head, body);
				String sqlquery = String.format(strselect_table_class, predicatetableMap.get(pred));
				map = obdaFactory.getRDBMSMappingAxiom(sqlquery, target);
			} else if (pred.getArity() == 2) {
				Atom head = obdaFactory.getAtom(binaryq, obdaFactory.getVariable("TERM0"), obdaFactory.getVariable("TERM1"));
				Atom body = obdaFactory.getAtom(pred, obdaFactory.getVariable("TERM0"), obdaFactory.getVariable("TERM1"));
				OBDAQuery target = obdaFactory.getCQIE(head, body);
				String sqlquery = String.format(strselect_table_property, predicatetableMap.get(pred));
				map = obdaFactory.getRDBMSMappingAxiom(sqlquery, target);
			} else {
				throw new RuntimeException("Unsupported predicate: " + pred);
			}

			mappings.add(map);
		}
		return mappings;
	}

	@Override
	public void collectStatistics(Connection conn) throws SQLException {

		Statement sqlst = conn.createStatement();
		sqlst.executeUpdate(stranalyze);

		sqlst.close();
	}

	public void setVocabulary(Set<Predicate> vocabulary) throws PunningException {
		this.vocabulary = vocabulary;
		predicatetableMap.clear();
		int classcounter = 0;
		int propertycounter = 0;

		/*
		 * Initializing the table map. The table map will be used in all other
		 * operations.
		 */
		for (Predicate predicate : vocabulary) {

			Predicate existingPredicate = uriPredicateMap.get(predicate.getName().toString());
			if (existingPredicate != null && existingPredicate.equals(predicate)) {
				/* The predicate has already been processed */
				continue;
			} else if (existingPredicate != null && !existingPredicate.equals(predicate)) {
				/*
				 * A predicate with the same URI has been already processed, but
				 * it has different arity or types, i.e., the URI has been
				 * PUNNED.
				 */
				log.warn("PUNNING DETECTED: {}", predicate.getName().toString());
				throw new PunningException(predicate, existingPredicate);
			}

			if (predicate.getArity() == 1) {
				predicatetableMap.put(predicate, "TCLASS" + classcounter);
				classcounter += 1;
			} else if (predicate.getArity() == 2) {
				predicatetableMap.put(predicate, "TPROPERTY" + propertycounter);
				propertycounter += 1;
			} else {
				throw new RuntimeException("Unsupported arity. Offending predicate: " + predicate);
			}
			uriPredicateMap.put(predicate.getName().toString(), predicate);
		}
	}

	@Override
	public void loadMetadata(Connection conn) throws SQLException {
		this.predicatetableMap.clear();
		this.uriPredicateMap.clear();
		/*
		 * Reconstructing a predicate-table mapping
		 */
		this.loadMetadataFromDB(conn, predicatetableMap, uriPredicateMap);
	}

	@Override
	public boolean checkMetadata(Connection conn) throws SQLException {

		/*
		 * Fetching the metadata from the DB
		 */
		Map<Predicate, String> dbPredicateTableMap = new HashMap<Predicate, String>();
		Map<String, Predicate> dbUriPredicateMap = new HashMap<String, Predicate>();

		loadMetadataFromDB(conn, dbPredicateTableMap, dbUriPredicateMap);

		/*
		 * Comparing with the current vocabulary
		 */

		Set<String> uris = uriPredicateMap.keySet();
		for (String uri : uris) {
			Predicate dbPredicate = dbUriPredicateMap.get(uri);
			Predicate localPredicate = uriPredicateMap.get(uri);
			if (!dbPredicate.equals(localPredicate))
				return false;
		}

		return true;
	}

	private void loadMetadataFromDB(Connection conn, Map<Predicate, String> predicateTableMap, Map<String, Predicate> uriPredicateMap)
			throws SQLException {
		Statement sqlst = conn.createStatement();
		ResultSet result = sqlst.executeQuery(strselect_meta_table);
		log.debug("Restoring metadata from DB");
		try {
			while (result.next()) {
				String predicatename = result.getString("URI");
				String type = result.getString("TYPE");
				String tablename = result.getString("TABLENAME");

				Predicate predicate = null;

				if (type.equals("CONCEPT")) {
					predicate = obdaFactory.getPredicate(URI.create(predicatename), 1);
				} else if (type.equals("OBJECTPROPERTY")) {
					predicate = obdaFactory.getPredicate(URI.create(predicatename), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
				} else if (type.equals("DATAPROPERTY")) {
					predicate = obdaFactory
							.getPredicate(URI.create(predicatename), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
				} else {
					IllegalParameterException ex = new IllegalParameterException("URI type: " + type);
					ex.fillInStackTrace();
					throw ex;
				}
				log.debug("Predicate: {} Table: {}", predicate.toString(), tablename);
				predicateTableMap.put(predicate, tablename);
				uriPredicateMap.put(predicatename, predicate);
			}
			log.debug("Done restoring metadata");
		} catch (SQLException e) {
			throw e;
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
			try {
				sqlst.close();
			} catch (Exception e) {
			}
		}
	}

	/*
	 * Utilities
	 */
	private static String escaped(String str) {
		str = str.replace("\'", "\'\'"); // H2 requires two single quotes as the
											// escaped character for a single
											// quote.
		return str;
	}

	@Override
	public void dropIndexes(Connection conn) throws SQLException {
		Statement st = conn.createStatement();

		for (Predicate predicate : predicatetableMap.keySet()) {
			if (predicate.getArity() == 1) {
				st.addBatch(String.format(strdrop_index_class, predicatetableMap.get(predicate)));
			} else if (predicate.getArity() == 2) {
				st.addBatch(String.format(strdrop_index_property_1, predicatetableMap.get(predicate)));
				st.addBatch(String.format(strdrop_index_property_2, predicatetableMap.get(predicate)));
			} else {
				throw new RuntimeException("Unsupported predicate: " + predicate);
			}
		}

		st.executeBatch();
		st.close();
		isIndexed = false;

	}

	@Override
	public boolean isIndexed(Connection conn) {
		return isIndexed;
	}

	@Override
	public boolean isDBSchemaDefined(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		boolean exists = true;
		try {

			for (Predicate predicate : predicatetableMap.keySet()) {
				if (predicate.getArity() == 1) {
					st.addBatch(String.format("SELECT 1 FROM " + strtabledata + " WHERE 1=0", predicatetableMap.get(predicate)));
				} else if (predicate.getArity() == 2) {
					st.addBatch(String.format("SELECT 1 FROM " + strtabledata + " WHERE 1=0", predicatetableMap.get(predicate)));
				} else {
					throw new RuntimeException("Unsupported predicate: " + predicate);
				}
			}
			st.executeBatch();
		} catch (SQLException e) {
			exists = false;
			log.debug(e.getMessage());
		} finally {
			try {
				st.close();
			} catch (SQLException e) {

			}
		}
		return exists;
	}

	@Override
	public long loadWithFile(Connection conn, Iterator<Assertion> data) throws SQLException, IOException {
		// TODO Auto-generated method stub
		return 0;
	}
}
