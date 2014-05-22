package it.unibz.krdb.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author dimitris
 */
public class ADPDemo {

  public static void main(String[] args) throws Exception {
	  
	  
	  
	  Class.forName("madgik.adp.federatedjdbc.AdpDriver");
	  
	  String conString="jdbc:adp:http://whale.di.uoa.gr:9090/datasets/npd-dataset";

	    Connection conn2 = DriverManager.getConnection(
	    		conString,
	            "adp",
	            "adp");

	    
	    Statement st=conn2.createStatement();
	    ResultSet rs2=st.executeQuery("addFederatedEndpoint(NPD, jdbc:mysql://whale.di.uoa.gr:3306/newnpd, com.mysql.jdbc.Driver, optique, gray769watt724)");

	    
	    String q="select fldName as c1 from NPD_discovery";
	    System.out.println(q);
	    rs2.close();
	    rs2=st.executeQuery(q);
	    System.out.println("Columns: " + rs2.getMetaData().getColumnCount());
	    int count = 0;
	    int size = 0;
	    for (int c = 0; c < rs2.getMetaData().getColumnCount(); ++c) {
	      System.out.println(rs2.getMetaData().getColumnName(c + 1));
	    }
	    while (rs2.next()) {
	      String[] next = new String[rs2.getMetaData().getColumnCount()];
	      for (int c = 0; c < next.length; ++c) {
	        next[c] = "" + rs2.getObject(c + 1);
	        size += next[c].length();
	      }
	      for (String v : next) {
	        System.out.print(v + "\t");
	      }
	      System.out.println("");
	      ++count;
	    }
	    System.out.println("Count: " + count + "\n\tSize: " + size);
	    rs2.close();
	    
  }
}
