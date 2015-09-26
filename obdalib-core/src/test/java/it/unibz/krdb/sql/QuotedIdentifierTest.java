package it.unibz.krdb.sql;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import it.unibz.krdb.obda.parser.SQLQueryParser;
import it.unibz.krdb.sql.api.ParsedSQLQuery;
import net.sf.jsqlparser.JSQLParserException;

import org.junit.Test;

public class QuotedIdentifierTest {

	QuotedIDFactory fac = new QuotedIDFactoryStandardSQL();
		
	@Test
	public void test1() {
		
		assertEquals(fac.createFromDatabaseRecord("A").getSQLRendering(), "\"A\"");

		assertEquals(fac.createFromDatabaseRecord("abc").getSQLRendering(), "\"abc\"");

		assertEquals(fac.createFromDatabaseRecord(null).getSQLRendering(), null);

		assertEquals(fac.createFromString("A").getSQLRendering(), "A");

		assertEquals(fac.createFromString("a").getSQLRendering(), "A"); // convert to upper case
		
		assertEquals(fac.createFromString("\"a\"").getSQLRendering(), "\"a\""); // leave as is

		assertEquals(fac.createFromString("\"A\"").getSQLRendering(), "\"A\"");

		assertEquals(fac.createFromString(null).getSQLRendering(), null);

		assertEquals(fac.createRelationFromDatabaseRecord(null, "A").getSQLRendering(), "\"A\"");
		
		assertEquals(fac.createRelationFromDatabaseRecord("S", "A").getSQLRendering(), "\"S\".\"A\"");
		
		assertEquals(fac.createRelationFromString("S.A").getSQLRendering(), "S.A");
		
		assertEquals(fac.createRelationFromString("s.\"A\"").getSQLRendering(), "S.\"A\"");
		
		assertEquals(fac.createRelationFromString("\"S\".\"A\"").getSQLRendering(), "\"S\".\"A\"");
		
		assertEquals(fac.createRelationFromString("A").getSQLRendering(), "A");
	}
	
	public void test1b() {
		Set<QuotedID> s = new HashSet<>();
		
		s.add(fac.createFromString("aaa"));
		s.add(fac.createFromString("\"AAA\""));
		
		assertEquals(s.size(), 1);
		
		QualifiedAttributeID a1 = new QualifiedAttributeID(null, fac.createFromString("aaa"));
		QualifiedAttributeID a2 = new QualifiedAttributeID(null, fac.createFromString("\"AAA\""));
		assertEquals(a1, a2);
	}
	
	@Test
	public void test2() {
		SQLQueryParser parser = new SQLQueryParser(fac);
		
		String s = "SELECT Able.\"id\", bB.Col4 AS c FROM TaBle1 able, (SELECT col4 FROM Bable) Bb, " +
					"c JOIN d ON c.id = d.Id " +
					"WHERE \"AblE\".Col = Able.col2";
		
		System.out.println(s);
		
		ParsedSQLQuery q = parser.parseDeeply(s);
		
		System.out.println(q.toString());
	}

	@Test
	public void test2b() throws Exception {
		SQLQueryParser parser = new SQLQueryParser(fac);
		
		String s = "SELECT a.id AS c FROM A";
		
		System.out.println(s);
		
		ParsedSQLQuery q = parser.parseDeeply(s);
		
		System.out.println(q.toString());
	}
	
	@Test
	public void test3() throws Exception {
		SQLQueryParser parser = new SQLQueryParser(fac);
		
		String s = "SELECT * FROM A JOIN B ON NOT (a.id <> b.id)";
		
		System.out.println(s);
		
		ParsedSQLQuery q = parser.parseDeeply(s);
		
		System.out.println(q.toString());
		System.out.println(q.getJoinConditions().toString());
	}
	
}
