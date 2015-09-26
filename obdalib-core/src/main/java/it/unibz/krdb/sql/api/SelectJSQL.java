package it.unibz.krdb.sql.api;

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

import it.unibz.krdb.sql.RelationID;

import java.io.Serializable;

import net.sf.jsqlparser.statement.select.SubSelect;

public class SelectJSQL implements Serializable{
		

	private static final long serialVersionUID = 6565489073454036936L;
		/**
		 * Class SelectJSQL used to store the information about the subselect in the query. We distinguish between givenName and Name.
		 * Since with Name we don't want to consider columns.
		 */
		
		private final String body;
		private final RelationID alias;
		
		
		public SelectJSQL(String subSelect, RelationID alias) {
			this.body = subSelect;
			this.alias = alias;
		}
		
		public RelationID getAlias() {
			return alias;
		}
		
		public String getBody() {
			return body;
		}
		
		@Override
		public String toString() {
			return body;
		}

		/**
		 * Called from the MappingParser:getTables. 
		 * Needed to remove duplicates from the list of tables
		 */
		@Override
		public boolean equals(Object t){
			if(t instanceof SelectJSQL){
				SelectJSQL tp = (SelectJSQL) t;
				return this.body.equals(tp.getBody())
						&& ((this.alias == null && tp.getAlias() == null)
								|| this.alias.equals(tp.getAlias())
								);
			}
			return false;
		}

		
	}
