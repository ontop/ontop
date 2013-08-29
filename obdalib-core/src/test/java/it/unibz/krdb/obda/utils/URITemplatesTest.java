package it.unibz.krdb.obda.utils;

import java.util.Arrays;

import junit.framework.TestCase;

public class URITemplatesTest extends TestCase {
	
	@SuppressWarnings("unchecked")
	public void testFormat(){
		assertEquals("http://example.org/A/1", URITemplates.format("http://example.org/{}/{}", "A", 1));
		
		assertEquals("http://example.org/A", URITemplates.format("http://example.org/{}", "A"));
		
		assertEquals("http://example.org/A/1", URITemplates.format("http://example.org/{}/{}", Arrays.asList("A", 1)));
		
		assertEquals("http://example.org/A", URITemplates.format("http://example.org/{}", Arrays.asList("A")));
	}

}
