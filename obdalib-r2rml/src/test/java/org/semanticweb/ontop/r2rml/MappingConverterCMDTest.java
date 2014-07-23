package org.semanticweb.ontop.r2rml;

import org.junit.Test;
import org.semanticweb.ontop.r2rml.MappingConverterCMD;

public class MappingConverterCMDTest {

	@Test
	public void testWithOntology() {
		MappingConverterCMD.main(new String[]{"src/test/resources/npd-v2-ql_a.obda", "src/test/resources/npd-v2-ql_a.owl"});
	}
	
	@Test
	public void testWithoutOntology() {
		MappingConverterCMD.main(new String[]{"src/test/resources/npd-v2-ql_a.obda"});
	}
	
	@Test
	public void testFromR2rml() {
		MappingConverterCMD.main(new String[]{"src/test/resources/npd-v2-ql_a_conversion.ttl"});
	}

}
