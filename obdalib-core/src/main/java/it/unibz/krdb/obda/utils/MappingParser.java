package it.unibz.krdb.obda.utils;

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

import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.parser.SQLQueryShallowParser;
import it.unibz.krdb.sql.QuotedIDFactory;
import it.unibz.krdb.sql.RelationID;
import it.unibz.krdb.sql.api.ParsedSQLQuery;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.jsqlparser.JSQLParserException;



/**
 * This class is in charge of parsing the mappings and obtaining the list of schema names 
 * and tables.Afterwards from this list we obtain the metadata (not in this class).
 * @author Dag
 *
 */
public class MappingParser {
	

    /**
	 * Called by Quest when fetching non-full metadata
	 * Only metadata for the tables in this list is extracted by Quest
	 * 
	 * @return The tables (same as getTables)
	 * @throws JSQLParserException 
	 */
	public static Set<RelationID> getRealTables(QuotedIDFactory idfac, Collection<OBDAMappingAxiom> mappings) throws JSQLParserException{
		List<String> errorMessage = new LinkedList<>();
		Set<RelationID> tables = new HashSet<>();
		for (OBDAMappingAxiom axiom : mappings) {
			try {
				OBDASQLQuery sourceQuery = axiom.getSourceQuery();
				ParsedSQLQuery sourceQueryParsed = SQLQueryShallowParser.parse(idfac, sourceQuery.toString());
				List<RelationID> queryTables = sourceQueryParsed.getRelations();
				for (RelationID table : queryTables) 
					tables.add(table);
			} 
			catch (Exception e) {
				errorMessage.add("Error in mapping with id: " + axiom.getId() + " \n Description: "
						+ e.getMessage() + " \nMapping: [" + axiom.toString() + "]");
				
			}
		}
		if (errorMessage.size() > 0) {
			StringBuilder errors = new StringBuilder();
			for (String error: errorMessage) {
				errors.append(error + "\n");
			}
			final String msg = "There was an error parsing the following mappings. Please correct the issue(s) to continue.\n" + errors.toString();
			throw new RuntimeException(msg);
		}
		return tables;
		
//		Set<RelationID> realTables = getTables();
//			List<RelationJSQL> removeThese = new ArrayList<>();
//			for(ViewDefinition vd : sqlQueryParser.getViewDefinitions()){
//				for(RelationJSQL rel : realTables){
//					if(rel.getFullName().equals(vd.getName()))
//						removeThese.add(rel);
//				}
//			}
//			for(RelationJSQL remRel : removeThese){
//				realTables.remove(remRel);
//			}
//		return realTables;
	}
}
