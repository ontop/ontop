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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ForeignKey {
	
	private String SchemaName;
	private String CoPKTableName;
	private String CoPKColumnName;

	public ForeignKey(){
		SchemaName = null;
	}
	
	public String getCoPKTable(){
		return this.CoPKTableName;
	}
	
	public String getCoPKColumn(){
		return this.CoPKColumnName;
	}
	
	public void setSchemaName(String sn){
		this.SchemaName = sn;
	}
	
	//find the table and column where a Foreign key comes from
	public ForeignKey(DatabaseMetaData md, String tableName, String FK){
		try{
			ResultSet rs = md.getImportedKeys(null, this.SchemaName, tableName);
			while (rs.next()){
				String getFK=rs.getString("FKCOLUMN_NAME");
				if(FK.equalsIgnoreCase(getFK)){
					this.CoPKTableName = rs.getString("PKTABLE_NAME");
					this.CoPKColumnName = rs.getString("PKCOLUMN_NAME");
				}
			}
		}
		catch (SQLException e){  
            e.printStackTrace();     
        } 
	}

}
