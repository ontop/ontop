package it.unibz.inf.ontop.utils;

/*
 * #%L
 * ontop-obdalib-core
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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.utils.ConnectionTools;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class about the ABox materialization.
 */
public class VirtualABoxStatistics {

	private final OBDAModel obdaModel;

	private HashMap<String, HashMap<String, Integer>> statistics = new HashMap<>();

	private Logger log = LoggerFactory.getLogger(VirtualABoxStatistics.class);

	public VirtualABoxStatistics(OBDAModel obdaModel) {
		this.obdaModel = obdaModel;
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

		return mappingStat.get(mappingId);
	}

	/**
	 * Gets the total number of triples from all the data sources and mappings.
	 */
	public int getTotalTriples() throws Exception {
		int total = 0;
		for (HashMap<String, Integer> mappingStat : statistics.values()) {
			for (Integer triplesCount : mappingStat.values()) {
				int triples = triplesCount;
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
		OBDADataSource source = obdaModel.getDatasource();
		List<SQLPPTriplesMap> mappingList = obdaModel.generatePPMapping().getTripleMaps();


		HashMap<String, Integer> mappingStat = new HashMap<>();
		for (SQLPPTriplesMap mapping : mappingList) {
			String mappingId = mapping.getId();
			int triplesCount = 0;
			try {
				OBDASQLQuery sourceQuery = mapping.getSourceQuery();
				int tuples = getTuplesCount(sourceQuery, source);

				ImmutableList<TargetAtom> targetQuery = mapping.getTargetAtoms();
				int atoms = targetQuery.size();

				triplesCount = tuples * atoms;
			} catch (Exception e) {
				triplesCount = -1; // fails to count
				log.error("Exception while computing mapping statistics", e);
			}
			mappingStat.put(mappingId, triplesCount);
		}
		statistics.put(source.getSourceID().toString(), mappingStat);
	}

	private int getTuplesCount(OBDASQLQuery query, OBDADataSource source)
			throws Exception {
		Statement st = null;
		ResultSet rs = null;
		int count = -1;

		try {
            String sql = String.format("select COUNT(*) %s", getSelectionString(query));
			Connection c = ConnectionTools.getConnection(source);
			st = c.createStatement();

			rs = st.executeQuery(sql);

			count = 0;
			while (rs.next()) {
				count = rs.getInt(1);
			}
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

	private String getSelectionString(OBDASQLQuery query) throws Exception {
		final String originalSql = query.toString();

		String sql = originalSql.toLowerCase(); // make it lower case to help identify a string.
		Matcher m = Pattern.compile("[\\n\\s\\t]*from[\\n\\s\\t]*").matcher(sql);
		if (m.find()) {
			int start = m.start();
			int end = sql.length();
			return originalSql.substring(start, end);
		}
		throw new Exception("Could not find the \"from\" keyword in the source query\n"+ query);
	}
}
