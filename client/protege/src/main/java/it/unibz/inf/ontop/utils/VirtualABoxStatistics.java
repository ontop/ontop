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
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.OBDAModel;
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

	private final OBDAModelManager obdaModelManager;

	private final HashMap<String, Integer> statistics = new HashMap<>();

	private final Logger log = LoggerFactory.getLogger(VirtualABoxStatistics.class);

	public VirtualABoxStatistics(OBDAModelManager obdaModelManager) {
		this.obdaModelManager = obdaModelManager;
	}

	/**
	 * Returns the complete statistics from the OBDA model.
	 * 
	 * @return The complete statistics.
	 */
	public HashMap<String, Integer> getStatistics() {
		return statistics;
	}

	/**
	 * Gets the total number of triples from all the data sources and mappings.
	 */
	public int getTotalTriples() throws Exception {
		int total = 0;
		for (Integer triplesCount : statistics.values()) {
			if (triplesCount == -1) {
				throw new Exception("An error was occurred in the counting process.");
			}
			total = total + triplesCount;
		}
		return total;
	}

	@Override
	public String toString() {
		String str = "";
		str += "Mappings: \n";
		for (String mappingId : statistics.keySet()) {
			int count = statistics.get(mappingId);
			str += String.format("- %s produces %s %s.\n", mappingId, count, (count == 1 ? "triple" : "triples"));
		}
		str += "\n";
		return str;
	}

	public void refresh() {
		OBDADataSource source = obdaModelManager.getDatasource();
		List<SQLPPTriplesMap> mappingList = obdaModelManager.getActiveOBDAModel().generatePPMapping().getTripleMaps();

		for (SQLPPTriplesMap mapping : mappingList) {
			String mappingId = mapping.getId();
			int triplesCount;
			try {
				SQLPPSourceQuery sourceQuery = mapping.getSourceQuery();
				int tuples = getTuplesCount(sourceQuery, source);

				ImmutableList<TargetAtom> targetQuery = mapping.getTargetAtoms();
				int atoms = targetQuery.size();

				triplesCount = tuples * atoms;
			}
			catch (Exception e) {
				triplesCount = -1; // fails to count
				log.error("Exception while computing mapping statistics", e);
			}
			statistics.put(mappingId, triplesCount);
		}
	}

	private int getTuplesCount(SQLPPSourceQuery query, OBDADataSource source) throws Exception {
		int count = 0;

		JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();

		try (Connection c = man.getConnection(source.getURL(), source.getUsername(), source.getPassword());
			 Statement st = c.createStatement()) {
            String sql = String.format("select COUNT(*) %s", getSelectionString(query));

			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				count = rs.getInt(1);
			}
		}
		return count;
	}

	private String getSelectionString(SQLPPSourceQuery query) throws Exception {
		String originalSql = query.getSQL();

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
