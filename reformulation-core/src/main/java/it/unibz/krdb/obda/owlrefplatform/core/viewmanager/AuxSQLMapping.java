package it.unibz.krdb.obda.owlrefplatform.core.viewmanager;

/*
 * #%L
 * ontop-reformulation-core
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

import java.io.Serializable;

/**
 * A class representing a mapping between the variables of the CQIE and 
 * SQL query of a mapping. It is only based on the position
 * 
 * @author Manfred Gerstgrasser
 *
 */

public class AuxSQLMapping implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8347044674081735636L;
	private String [] sqlVariables = null; 
	
	public AuxSQLMapping (String[] vars){
		sqlVariables = vars;
	}

	/**
	 * returns the sql variable at the given position
	 * @param pos the position
	 * @return the sql variable at the given position
	 * @throws Exception if the position is out of the bounds of the array
	 */
	public String getSQLVariableAt(int pos) {
		return sqlVariables[pos]; 
	}
	
	/**
	 * Returns the number of SQL variables
	 * @return
	 */
	public int getNrOfVariables(){
		return sqlVariables.length;
	}
	
	/**
	 * Returns the position of a given sql variable.
	 * @param varname the sql variable
	 * @return the position
	 */
	public int getPosOf(String varname){
		for(int i=0;i<sqlVariables.length;i++){
			if(varname.equals(sqlVariables[i])){
				return i;
			}
		}
		return -1;
	}
}
