package it.unibz.inf.ontop.spec.dbschema;

import static it.unibz.inf.ontop.utils.SQLMappingTestingTools.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.QuotedIDImpl;
import it.unibz.inf.ontop.dbschema.impl.RelationIDImpl;
import org.junit.Test;

public class QuotedIdentifierTest {


	@Test
	public void test1() {
		QuotedIDFactory fac = DEFAULT_DUMMY_DB_METADATA.getDBParameters().getQuotedIDFactory();

		assertEquals(QuotedIDImpl.createIdFromDatabaseRecord(fac, "A").getSQLRendering(), "\"A\"");

		assertEquals(QuotedIDImpl.createIdFromDatabaseRecord(fac, "abc").getSQLRendering(), "\"abc\"");

		assertEquals(QuotedIDImpl.createIdFromDatabaseRecord(fac, null).getSQLRendering(), null);

		assertEquals(fac.createAttributeID("A").getSQLRendering(), "A");

		assertEquals(fac.createAttributeID("a").getSQLRendering(), "A"); // convert to upper case
		
		assertEquals(fac.createAttributeID("\"a\"").getSQLRendering(), "\"a\""); // leave as is

		assertEquals(fac.createAttributeID("\"A\"").getSQLRendering(), "\"A\"");

		assertEquals(fac.createAttributeID(null).getSQLRendering(), null);

		assertEquals(RelationIDImpl.createRelationIdFromDatabaseRecord(fac, null, "A").getSQLRendering(), "\"A\"");
		
		assertEquals(RelationIDImpl.createRelationIdFromDatabaseRecord(fac, "S", "A").getSQLRendering(), "\"S\".\"A\"");
		
		//assertEquals(fac.createRelationFromString("S.A").getSQLRendering(), "S.A");
		
		//assertEquals(fac.createRelationFromString("s.\"A\"").getSQLRendering(), "S.\"A\"");
		
		//assertEquals(fac.createRelationFromString("\"S\".\"A\"").getSQLRendering(), "\"S\".\"A\"");
		
		//assertEquals(fac.createRelationFromString("A").getSQLRendering(), "A");
	}

	@Test
	public void test1b() {
		QuotedIDFactory fac = DEFAULT_DUMMY_DB_METADATA.getDBParameters().getQuotedIDFactory();

		Set<QuotedID> s = ImmutableSet.of(
				fac.createAttributeID("aaa"),
				fac.createAttributeID("\"AAA\""));
		
		assertEquals(s.size(), 1);
	}

	@Test
	public void test1c() {
		QuotedIDFactory fac = DEFAULT_DUMMY_DB_METADATA.getDBParameters().getQuotedIDFactory();

		QualifiedAttributeID a1 = new QualifiedAttributeID(null, fac.createAttributeID("aaa"));
		QualifiedAttributeID a2 = new QualifiedAttributeID(null, fac.createAttributeID("\"AAA\""));
		assertEquals(a1, a2);
	}


	public void test2() {
		String s = "SELECT Able.\"id\", bB.Col4 AS c FROM TaBle1 able, (SELECT col4 FROM Bable) Bb, " +
					"c JOIN d ON c.id = d.Id " +
					"WHERE \"AblE\".Col = Able.col2";
		// TODO
	}

	public void test2b() throws Exception {
		String s = "SELECT a.id AS c FROM A";
		// TODO
	}
	
	public void test3() throws Exception {
		String s = "SELECT * FROM A JOIN B ON NOT (a.id <> b.id)";
		// TODO
	}
	
}
