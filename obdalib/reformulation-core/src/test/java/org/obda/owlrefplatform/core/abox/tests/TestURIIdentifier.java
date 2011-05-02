package org.obda.owlrefplatform.core.abox.tests;
import java.net.URI;
import java.util.Hashtable;

import org.obda.owlrefplatform.core.abox.URIIdentyfier;
import org.obda.owlrefplatform.core.abox.URIType;

import junit.framework.TestCase;


public class TestURIIdentifier extends TestCase {

	
	public void test_1() throws Exception{
		URI uri = URI.create("http://obda.inf.unibz.it/ontologies/tests/dllitef/test_6_1_1.owl#r2");
		String tablename  = "table1";
		
		URIIdentyfier id1 = new URIIdentyfier(uri, URIType.CONCEPT);
		URIIdentyfier id2 = new URIIdentyfier(uri, URIType.CONCEPT);
		
		assertEquals(id1, id2);
		
		Hashtable<URIIdentyfier, String> hashtable = new Hashtable<URIIdentyfier, String>();
		hashtable.put(id1, tablename);
		
		assertEquals(tablename,hashtable.get(id2));
	}
}
