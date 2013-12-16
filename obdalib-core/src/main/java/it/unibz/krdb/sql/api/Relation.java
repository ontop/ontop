package it.unibz.krdb.sql.api;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

/**
 * The Relation class is a wrapper class that make the
 * {@link TablePrimary} class compatible with the 
 * abstraction in the {@link QueryTree}.
 */
public class Relation extends RelationalAlgebra {

	private static final long serialVersionUID = 8464933976477745339L;
	
	private TablePrimary table;
	
	public Relation(TablePrimary table) {
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
		return table.getAlias();
	}
	
	@Override
	public String toString() {
		return table.toString();
	}
	
	@Override
	public Relation clone() {
		return new Relation(table);
	}
	
	@Override
	public boolean equals(Object r){
		if(r instanceof Relation)
			return this.table.equals(((Relation)r).table);
		return false;
	}
}
