package it.unibz.krdb.obda.r2rml;

import org.junit.Test;

public class FromR2rmlConverterTest {

	@Test
	public void testFromR2rml() {
		MappingConverterCMD.main(new String[]{"src/test/resources/mapping-northwind.ttl"});
	}

}
