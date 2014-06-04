package org.semanticweb.ontop.sql;

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

	    
	    //String q="select fldName as c1 from NPD_discovery";
	    
	    String q = "SELECT *\n" + 
	    		"FROM (\n" + 
	    		"SELECT DISTINCT \n" + 
	    		"   1 AS \"licenceURIQuestType\", NULL AS \"licenceURILang\", ('http://sws.ifi.uio.no/data/npd-v2/licence/' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"prlNpdidLicence\" AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"licenceURI\", \n" + 
	    		"   1 AS \"interestQuestType\", NULL AS \"interestLang\", ('http://example.com/base/' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"prlLicenseeInterest\" AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"interest\", \n" + 
	    		"   1 AS \"dateQuestType\", NULL AS \"dateLang\", ('http://example.com/base/' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"prlLicenseeDateValidFrom\" AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) AS \"date\"\n" + 
	    		" FROM \n" + 
	    		"NPD_licence_licensee_hst QVIEW1\n" + 
	    		"WHERE \n" + 
	    		"(('9999-12-31T00:00:00' <> QVIEW1.\"prlLicenseeDateValidFrom\") OR (('9999-12-31T00:00:00' <> QVIEW1.\"prlLicenseeDateValidFrom\") OR ('9999-12-31T00:00:00' <> QVIEW1.\"prlLicenseeDateValidFrom\"))) AND\n" + 
	    		"QVIEW1.\"prlNpdidLicence\" IS NOT NULL AND\n" + 
	    		"QVIEW1.\"cmpNpdidCompany\" IS NOT NULL AND\n" + 
	    		"QVIEW1.\"prlLicenseeDateValidFrom\" IS NOT NULL AND\n" + 
	    		"QVIEW1.\"prlLicenseeDateValidTo\" IS NOT NULL AND\n" + 
	    		"QVIEW1.\"prlLicenseeInterest\" IS NOT NULL AND\n" + 
	    		"(QVIEW1.\"prlNpdidLicence\" IS NOT NULL OR (QVIEW1.\"prlNpdidLicence\" IS NOT NULL OR QVIEW1.\"prlNpdidLicence\" IS NOT NULL)) AND\n" + 
	    		"(('http://example.com/base/' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CAST(QVIEW1.\"prlLicenseeDateValidFrom\" AS CHAR),' ', '%20'),'!', '%21'),'@', '%40'),'#', '%23'),'$', '%24'),'&', '%26'),'*', '%42'), '(', '%28'), ')', '%29'), '[', '%5B'), ']', '%5D'), ',', '%2C'), ';', '%3B'), ':', '%3A'), '?', '%3F'), '=', '%3D'), '+', '%2B'), '''', '%22'), '/', '%2F')) > '1979-12-31T00:00:00')\n" + 
	    		") SUB_QVIEW";
	    
	    
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
