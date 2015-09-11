package org.semanticweb.ontop.r2rml;

import org.junit.Test;

public class FromOBDAConverterTest {

	@Test
	public void testWithOntology() {
		MappingConverterCMD.main(new String[]{"src/test/resources/mapping-northwind.obda", "src/test/resources/mapping-northwind.owl"});
	}

}
