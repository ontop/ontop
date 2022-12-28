package it.unibz.inf.ontop.dbschema;

import static it.unibz.inf.ontop.OntopModelTestingTools.createMetadataProviderBuilder;
import static org.junit.Assert.*;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import org.junit.Test;

public class QuotedIdentifierTest {


	@Test
	public void test1() {
		OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
		QuotedIDFactory fac = builder.getQuotedIDFactory();
		QuotedIDFactory rawIdFactory = new RawQuotedIDFactory(fac);

		assertEquals("\"A\"", rawIdFactory.createAttributeID("A").getSQLRendering());

		assertEquals("\"abc\"", rawIdFactory.createAttributeID("abc").getSQLRendering());

//		assertEquals(null, rawIdFactory.createAttributeID(null).getSQLRendering());

		assertEquals("A", fac.createAttributeID("A").getSQLRendering());

		assertEquals("A", fac.createAttributeID("a").getSQLRendering()); // convert to upper case
		
		assertEquals("\"a\"", fac.createAttributeID("\"a\"").getSQLRendering()); // leave as is

		assertEquals("\"A\"", fac.createAttributeID("\"A\"").getSQLRendering());

//		assertEquals(null, fac.createAttributeID(null).getSQLRendering());

		assertEquals("\"A\"", rawIdFactory.createRelationID( "A").getSQLRendering());
		
		assertEquals("\"S\".\"A\"", rawIdFactory.createRelationID( "S", "A").getSQLRendering());
		
		//assertEquals(fac.createRelationFromString("S.A").getSQLRendering(), "S.A");
		
		//assertEquals(fac.createRelationFromString("s.\"A\"").getSQLRendering(), "S.\"A\"");
		
		//assertEquals(fac.createRelationFromString("\"S\".\"A\"").getSQLRendering(), "\"S\".\"A\"");
		
		//assertEquals(fac.createRelationFromString("A").getSQLRendering(), "A");
	}

	@Test
	public void test1b() {
		OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
		QuotedIDFactory fac = builder.getQuotedIDFactory();

		Set<QuotedID> s = ImmutableSet.of(
				fac.createAttributeID("aaa"),
				fac.createAttributeID("\"AAA\""));
		
		assertEquals(1, s.size());
	}

	@Test
	public void test1c() {
		OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
		QuotedIDFactory fac = builder.getQuotedIDFactory();

		QualifiedAttributeID a1 = new QualifiedAttributeID(null, fac.createAttributeID("aaa"));
		QualifiedAttributeID a2 = new QualifiedAttributeID(null, fac.createAttributeID("\"AAA\""));
		assertEquals(a1, a2);
	}
}
