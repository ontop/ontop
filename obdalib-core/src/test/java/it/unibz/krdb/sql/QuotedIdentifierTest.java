package it.unibz.krdb.sql;

import static org.junit.Assert.*;

import org.junit.Test;

public class QuotedIdentifierTest {

	@Test
	public void test1() {
		QuotedIDFactory fac = new QuotedIDFactoryStandardSQL();
		
		assertEquals(fac.createFromDatabaseRecord("A").getSQLRendering(), "\"A\"");

		assertEquals(fac.createFromDatabaseRecord("abc").getSQLRendering(), "\"abc\"");

		assertEquals(fac.createFromDatabaseRecord(null).getSQLRendering(), null);

		assertEquals(fac.createFromString("A").getSQLRendering(), "A");

		assertEquals(fac.createFromString("a").getSQLRendering(), "A"); // convert to upper case
		
		assertEquals(fac.createFromString("\"a\"").getSQLRendering(), "\"a\""); // leave as is

		assertEquals(fac.createFromString("\"A\"").getSQLRendering(), "\"A\"");

		assertEquals(fac.createFromString(null).getSQLRendering(), null);

		assertEquals(fac.createRelationFromDatabaseRecoard(null, "A").getSQLRendering(), "\"A\"");
		
		assertEquals(fac.createRelationFromDatabaseRecoard("S", "A").getSQLRendering(), "\"S\".\"A\"");
		
		assertEquals(fac.createRelationFromString("S.A").getSQLRendering(), "S.A");
		
		assertEquals(fac.createRelationFromString("s.\"A\"").getSQLRendering(), "S.\"A\"");
		
		assertEquals(fac.createRelationFromString("\"S\".\"A\"").getSQLRendering(), "\"S\".\"A\"");
		
		assertEquals(fac.createRelationFromString("A").getSQLRendering(), "A");
	}
	
}
