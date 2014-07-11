package it.unibz.krdb.obda.r2rml;

import org.junit.Test;

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
		MappingConverterCMD.main(new String[]{"src/test/resources/mapping.ttl"});
	}

}
