package it.unibz.krdb.sql;

import it.unibz.krdb.obda.parser.SQLQueryDeepParser;
import it.unibz.krdb.sql.api.ParsedSQLQuery;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class QuotedIdentifierTest {

	DBMetadata dbMetadata = DBMetadataExtractor.createDummyMetadata();
	QuotedIDFactory fac = dbMetadata.getQuotedIDFactory();
		
	@Test
	public void test1() {
		
		assertEquals(QuotedID.createIdFromDatabaseRecord("A").getSQLRendering(), "\"A\"");

		assertEquals(QuotedID.createIdFromDatabaseRecord("abc").getSQLRendering(), "\"abc\"");

		assertEquals(QuotedID.createIdFromDatabaseRecord(null).getSQLRendering(), null);

		assertEquals(fac.createAttributeID("A").getSQLRendering(), "A");

		assertEquals(fac.createAttributeID("a").getSQLRendering(), "A"); // convert to upper case
		
		assertEquals(fac.createAttributeID("\"a\"").getSQLRendering(), "\"a\""); // leave as is

		assertEquals(fac.createAttributeID("\"A\"").getSQLRendering(), "\"A\"");

		assertEquals(fac.createAttributeID(null).getSQLRendering(), null);

		assertEquals(RelationID.createRelationIdFromDatabaseRecord(null, "A").getSQLRendering(), "\"A\"");
		
		assertEquals(RelationID.createRelationIdFromDatabaseRecord("S", "A").getSQLRendering(), "\"S\".\"A\"");
		
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
		
		ParsedSQLQuery q = SQLQueryDeepParser.parse(dbMetadata, s);
		
		System.out.println(q.toString());
	}

	@Test
	public void test2b() throws Exception {
		String s = "SELECT a.id AS c FROM A";
		
		System.out.println(s);
		
		ParsedSQLQuery q = SQLQueryDeepParser.parse(dbMetadata, s);
		
		System.out.println(q.toString());
	}
	
	@Test
	public void test3() throws Exception {
		String s = "SELECT * FROM A JOIN B ON NOT (a.id <> b.id)";
		
		System.out.println(s);
		
		ParsedSQLQuery q = SQLQueryDeepParser.parse(dbMetadata, s);
		
		System.out.println(q.toString());
		System.out.println(q.getJoinConditions().toString());
	}
	
}
