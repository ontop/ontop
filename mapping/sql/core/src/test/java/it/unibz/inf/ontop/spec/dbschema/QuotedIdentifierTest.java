package it.unibz.inf.ontop.spec.dbschema;

import static it.unibz.inf.ontop.utils.SQLMappingTestingTools.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import it.unibz.inf.ontop.dbschema.*;
import org.junit.Test;

public class QuotedIdentifierTest {

	RDBMetadata dbMetadata = createDummyMetadata();
	QuotedIDFactory fac = dbMetadata.getQuotedIDFactory();
		
	@Test
	public void test1() {
		
		assertEquals(QuotedID.createIdFromDatabaseRecord(fac, "A").getSQLRendering(), "\"A\"");

		assertEquals(QuotedID.createIdFromDatabaseRecord(fac, "abc").getSQLRendering(), "\"abc\"");

		assertEquals(QuotedID.createIdFromDatabaseRecord(fac, null).getSQLRendering(), null);

		assertEquals(fac.createAttributeID("A").getSQLRendering(), "A");

		assertEquals(fac.createAttributeID("a").getSQLRendering(), "A"); // convert to upper case
		
		assertEquals(fac.createAttributeID("\"a\"").getSQLRendering(), "\"a\""); // leave as is

		assertEquals(fac.createAttributeID("\"A\"").getSQLRendering(), "\"A\"");

		assertEquals(fac.createAttributeID(null).getSQLRendering(), null);

		assertEquals(RelationID.createRelationIdFromDatabaseRecord(fac, null, "A").getSQLRendering(), "\"A\"");
		
		assertEquals(RelationID.createRelationIdFromDatabaseRecord(fac, "S", "A").getSQLRendering(), "\"S\".\"A\"");
		
		//assertEquals(fac.createRelationFromString("S.A").getSQLRendering(), "S.A");
		
		//assertEquals(fac.createRelationFromString("s.\"A\"").getSQLRendering(), "S.\"A\"");
		
		//assertEquals(fac.createRelationFromString("\"S\".\"A\"").getSQLRendering(), "\"S\".\"A\"");
		
		//assertEquals(fac.createRelationFromString("A").getSQLRendering(), "A");
	}
	
	public void test1b() {
		Set<QuotedID> s = new HashSet<>();
		
		s.add(fac.createAttributeID("aaa"));
		s.add(fac.createAttributeID("\"AAA\""));
		
		assertEquals(s.size(), 1);
		
		QualifiedAttributeID a1 = new QualifiedAttributeID(null, fac.createAttributeID("aaa"));
		QualifiedAttributeID a2 = new QualifiedAttributeID(null, fac.createAttributeID("\"AAA\""));
		assertEquals(a1, a2);
	}
	
	@Test
	public void test2() {
		String s = "SELECT Able.\"id\", bB.Col4 AS c FROM TaBle1 able, (SELECT col4 FROM Bable) Bb, " +
					"c JOIN d ON c.id = d.Id " +
					"WHERE \"AblE\".Col = Able.col2";
		
		System.out.println(s);
		
	}

	@Test
	public void test2b() throws Exception {
		String s = "SELECT a.id AS c FROM A";
		
		System.out.println(s);
		
	}
	
	@Test
	public void test3() throws Exception {
		String s = "SELECT * FROM A JOIN B ON NOT (a.id <> b.id)";
		
		System.out.println(s);
		
	}
	
}
