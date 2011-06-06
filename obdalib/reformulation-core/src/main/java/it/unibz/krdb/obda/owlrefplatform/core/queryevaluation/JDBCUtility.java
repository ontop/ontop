package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The JDBC utility class implements the algorithm which is used to
 * over come the impedance mismatch problem, i.e. it manipulates the
 * select statement such that it creates object URIs out of the data values
 * in the way the mappings show it.
 * 
 * @author Manfred Gerstgrasser
 *
 */

public class JDBCUtility {
	
	Logger log = LoggerFactory.getLogger(JDBCUtility.class);

	private String driver = null;
	
	public JDBCUtility(String driver) throws Exception{
		
		if(driver.equals("org.postgresql.Driver") || driver.equals("org.h2.Driver")||driver.equals("com.mysql.jdbc.Driver")||driver.equals("oracle.jdbc.driver.OracleDriver"))
		{	
			this.driver = driver;
		}else{
			this.driver = driver;
			log.warn("WARNING: the specified driver doesn't correspond to any of the drivers officially supported by Quest.");
			log.warn("WARNING: If you database is not fully compliant with SQL 99 you might experience problems using Quest.");
			log.warn("WARNING: Contact the authors for further support.");
		}
	}
	
	/**
	 * Given the uribase and the list of parameters, it contracts the
	 * necessary sql manipulations depending on the used data source 
	 * to construct object URIs
	 * Note: Right now only postgres, mysql and H2 are supported. Others
	 * should follow in the future.
	 * 
	 * @param uribase the base uri specified in the mapping
	 * @param list the list of parametes
	 * @return the sql manipulations to construct a object URI
	 */
	public String getConcatination(String uribase, List<String> list){
		
		if(driver.equals("com.mysql.jdbc.Driver")){
			
			String str = "CONCAT('" + uribase + "-',";
			str += list.get(0);
			for (int i = 1; i < list.size(); i++)
				str += ",'-', " + list.get(i);
			return str.concat(")");
		} else {
			String result = "'"+uribase+"-' ||";
			String vars = "";
			for (int i = 0; i < list.size(); i++) {
				if(vars.length() > 0){
					vars = vars +"||"+"'-'||";
				}
				vars = vars + list.get(i);
			}
			result = result + vars;
			return result;
		}
//		return "";
	}
}
