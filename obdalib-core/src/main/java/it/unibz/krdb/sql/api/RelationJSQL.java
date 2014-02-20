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

import java.io.Serializable;

/**
 * The Relation class is a wrapper class that make the
 * {@link TableJSQL} class compatible with the 
 * abstraction in the {@link VisitedQuery}.
 */
public class RelationJSQL implements Serializable, Cloneable  {

	private static final long serialVersionUID = 8464933976477745339L;
	
	private TableJSQL table;
	
	public RelationJSQL(TableJSQL table) {
		this.table = table;
	}
	
	public String getSchema() {
		return table.getSchema();
	}
	
	public String getName() {
		return table.getName();
	}
	
	public String getTableName() {
		return table.getTableName();
	}
	
	public String getGivenName() {
		return table.getGivenName();
	}
	public String getAlias() {
		if (table.getAlias() != null)
			return table.getAlias().getName();
		return null;
	}
	
	public boolean isTableQuoted(){
		return table.isTableQuoted();
	}
	
	public boolean isSchemaQuoted(){
		return table.isSchemaQuoted();
	}
	
	@Override
	public String toString() {
		return table.toString();
	}
	
	@Override
	public RelationJSQL clone() {
		return new RelationJSQL(table);
	}
	
	@Override
	public boolean equals(Object r){
		if(r instanceof RelationJSQL)
			return this.table.getGivenName().equals(((RelationJSQL)r).table.getGivenName());
		return false;
	}
}
