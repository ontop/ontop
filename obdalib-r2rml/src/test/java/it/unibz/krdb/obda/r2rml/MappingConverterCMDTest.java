package it.unibz.krdb.obda.r2rml;

import static org.junit.Assert.*;
import it.unibz.krdb.obda.r2rml.MappingConverterCMD;

import org.junit.Test;

public class MappingConverterCMDTest {

	@Test
	public void test() {
		MappingConverterCMD.main(new String[]{"src/test/resources/npd-v2-ql_a.obda", "src/test/resources/npd-v2-ql_a.owl"});
	}

}
