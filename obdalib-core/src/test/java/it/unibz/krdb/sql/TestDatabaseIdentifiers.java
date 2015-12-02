package it.unibz.krdb.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

import org.junit.Test;

public class TestDatabaseIdentifiers {

	@Test
	public void testIdentifiers() throws Exception {
		try {
			Connection conn = DriverManager.getConnection(
					"jdbc:postgresql://localhost/dbconstraints",
	                "postgres",
	                "postgres");
			DatabaseMetaData md = conn.getMetaData();
			System.out.println(md.getDatabaseProductName());
			System.out.println("storesLowerCaseIdentifiers: " + md.storesLowerCaseIdentifiers());
			System.out.println("storesUpperCaseIdentifiers: " + md.storesUpperCaseIdentifiers());
			System.out.println("storesMixedCaseIdentifiers: " + md.storesMixedCaseIdentifiers());
			System.out.println("supportsMixedCaseIdentifiers: " + md.supportsMixedCaseIdentifiers());
			System.out.println("storesLowerCaseQuotedIdentifiers: " + md.storesLowerCaseQuotedIdentifiers());
			System.out.println("storesUpperCaseQuotedIdentifiers: " + md.storesUpperCaseQuotedIdentifiers());
			System.out.println("storesMixedCaseQuotedIdentifiers: " + md.storesMixedCaseQuotedIdentifiers());
			System.out.println("supportsMixedCaseQuotedIdentifiers: " + md.supportsMixedCaseQuotedIdentifiers());
			System.out.println("getIdentifierQuoteString: " + md.getIdentifierQuoteString());		
		}
		catch (Exception e) {
		}	
	}
}
