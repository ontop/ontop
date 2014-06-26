package it.unibz.krdb.obda.sesame.r2rml;

import static org.junit.Assert.*;

import org.junit.Test;

public class MappingConverterCMDTest {

	@Test
	public void test() {
		MappingConverterCMD.main(new String[]{"src/test/resources/npd-v2-ql_a.obda"});
	}

}
