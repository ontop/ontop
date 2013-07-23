package it.unibz.krdb.obda.owlrefplatform.core.viewmanager;

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
