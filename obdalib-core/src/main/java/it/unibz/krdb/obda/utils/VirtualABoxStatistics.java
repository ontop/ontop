package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.exception.NoDatasourceSelectedException;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.impl.CQIEImpl;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class about the ABox materialization.
 */
public class VirtualABoxStatistics {

	private OBDAModel model;

	private HashMap<String, HashMap<String, Integer>> statistics = new HashMap<String, HashMap<String, Integer>>();

	private JDBCConnectionManager conn = JDBCConnectionManager.getJDBCConnectionManager();

	Logger log = LoggerFactory.getLogger(VirtualABoxStatistics.class);

	/**
	 * Inserts the OBDA model to this utility class.
	 * 
	 * @param model
	 *            The mandatory OBDA model.
	 */
	public VirtualABoxStatistics(OBDAModel model) {
		this.model = model;
	}

	/**
	 * Returns the complete statistics from the OBDA model.
	 * 
	 * @return The complete statistics.
	 */
	public HashMap<String, HashMap<String, Integer>> getStatistics() {
		return statistics;
	}

	/**
	 * Returns the triples counts from all the mappings that associate to a
	 * certain data source.
	 * 
	 * @param datasourceId
	 *            The data source identifier.
	 * @return A data statistics.
	 */
	public HashMap<String, Integer> getStatistics(String datasourceId) {
		return statistics.get(datasourceId);
	}

	/**
	 * Returns one triple count from a particular mapping.
	 * 
	 * @param datasourceId
	 *            The data source identifier.
	 * @param mappingId
	 *            The mapping identifier.
	 * @return The number of triples.
	 */
	public int getStatistics(String datasourceId, String mappingId) {
		final HashMap<String, Integer> mappingStat = getStatistics(datasourceId);
		int triplesCount = mappingStat.get(mappingId).intValue();

		return triplesCount;
	}

	/**
	 * Gets the total number of triples from all the data sources and mappings.
	 * 
	 * @return The total number of triples.
	 * @throws Exception
	 */
	public int getTotalTriples() throws Exception {
		int total = 0;
		for (HashMap<String, Integer> mappingStat : statistics.values()) {
			for (Integer triplesCount : mappingStat.values()) {
				int triples = triplesCount.intValue();
				if (triples == -1) {
					throw new Exception("An error was occurred in the counting process.");
				}
				total = total + triples;
			}
		}
		return total;
	}

	@Override
	public String toString() {
		String str = "";
		for (String datasourceId : statistics.keySet()) {
			str += "Data Source Name: " + datasourceId + "\n";
			str += "Mappings: \n";
			HashMap<String, Integer> mappingStat = statistics.get(datasourceId);
			for (String mappingId : mappingStat.keySet()) {
				int count = mappingStat.get(mappingId);
				str += String.format("- %s produces %s %s.\n", mappingId, count, (count == 1 ? "triple" : "triples"));
			}
			str += "\n";
		}
		return str;
	}

	public void refresh() {
		final List<OBDADataSource> sourceList = model.getSources();

		for (OBDADataSource database : sourceList) {
			URI sourceUri = database.getSourceID();
			ArrayList<OBDAMappingAxiom> mappingList = model.getMappings(sourceUri);

			HashMap<String, Integer> mappingStat = new HashMap<String, Integer>();
			for (OBDAMappingAxiom mapping : mappingList) {
				String mappingId = mapping.getId();
				int triplesCount = 0;
				try {
					OBDASQLQuery sourceQuery = (OBDASQLQuery) mapping.getSourceQuery();
					int tuples = getTuplesCount(database, sourceQuery);

					CQIEImpl targetQuery = (CQIEImpl) mapping.getTargetQuery();
					int atoms = getAtomCount(targetQuery);

					triplesCount = tuples * atoms;
				} catch (Exception e) {
					triplesCount = -1; // fails to count
					log.error(e.getMessage());
				}
				mappingStat.put(mappingId, triplesCount);
			}
			String sourceId = sourceUri.toString();
			statistics.put(sourceId, mappingStat);
		}
	}

	private int getTuplesCount(OBDADataSource sourceId, OBDASQLQuery query) throws NoDatasourceSelectedException, ClassNotFoundException, SQLException {
		Statement st = null;
		ResultSet rs = null;
		int count = -1;
		try {
			String sql = String.format("select COUNT(*) %s", getSelectionString(query));
			Connection c = conn.getConnection(sourceId);
			st = c.createStatement();

			rs = st.executeQuery(sql);

			count = 0;
			while (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
				// NO-OP
			}
			try {
				st.close();
			} catch (Exception e) {
				// NO-OP
			}
		}
		return count;
	}

	private int getAtomCount(CQIEImpl query) {
		return query.getBody().size();
	}

	private String getSelectionString(OBDASQLQuery query) {
		final String originalSql = query.toString();
		
		String sql = originalSql.toLowerCase(); // make it lower case to help identify a string.
		int start = sql.indexOf("from");
		int end = sql.length();
		
		return originalSql.substring(start, end);
	}
}
