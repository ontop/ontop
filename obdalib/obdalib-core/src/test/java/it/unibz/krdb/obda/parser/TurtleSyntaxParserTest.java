package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import junit.framework.TestCase;

import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurtleSyntaxParserTest extends TestCase {

	final static Logger log = LoggerFactory.getLogger(TurtleSyntaxParserTest.class);
	
	public void test_1() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> a :Person .");
		assertTrue(result);
	}
	
	public void test_2() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> :hasFather <\"http://example.org/testcase#Person-{$id}\"> .");
		assertTrue(result);
	}
	
	public void test_3_1() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> :firstName $fname .");
		assertTrue(result);
	}
	
	public void test_3_2() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> :firstName $fname^^xsd:string .");
		assertTrue(result);
	}
	
	public void test_3_3() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> :firstName $fname@en-US .");
		assertTrue(result);
	}
	
	public void test_4_1() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> :firstName \"John\"^^xsd:string .");
		assertTrue(result);
	}
	
	public void test_4_2_1() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> :firstName \"John\"^^rdfs:Literal .");
		assertTrue(result);
	}
	
	public void test_4_2_2() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> :firstName \"John\"@en-US .");
		assertTrue(result);
	}
	
	public void test_5_1_1() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> a :Person; :firstName $fname .");
		assertTrue(result);
	}
	
	public void test_5_1_2() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> a :Person; :firstName $fname; :age $age .");
		assertTrue(result);
	}
	
	public void test_5_1_3() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> a :Person; :hasFather <\"http://example.org/testcase#Person-{$id}\">; :firstName $fname; :age $age .");
		assertTrue(result);
	}
	
	public void test_5_2_1() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> a :Person; :firstName $fname^^xsd:string .");
		assertTrue(result);
	}
	
	public void test_5_2_2() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> a :Person; :firstName $fname^^xsd:string; :age $age^^xsd:integer .");
		assertTrue(result);
	}
	
	public void test_5_2_3() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> a :Person; :hasFather <\"http://example.org/testcase#Person-{$id}\">; :firstName $fname^^xsd:string; :age $age^^xsd:integer .");
		assertTrue(result);
	}
	
	public void test_5_2_4() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> a :Person; :hasFather <\"http://example.org/testcase#Person-{$id}\">; :firstName $fname^^xsd:string; :age $age^^xsd:integer; :description $text@en-US .");
		assertTrue(result);
	}
	
	public void test_6_1() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> a :Person; :firstName $fname^^xsd:String .");
		assertFalse(result);
	}
	
	public void test_6_2() {
		final boolean result = parse("<\"http://example.org/testcase#Person-{$id}\"> a :Person; :firstName $fname^^ex:randomDatatype .");
		assertFalse(result);
	}
	
	public void test_7_1() {
		final boolean result = parse("<\"&:;Person-{$id}\"> a :Person .");
		assertTrue(result);
	}
	
	public void test_7_2() {
		final boolean result = parse("<\"&:;Person-{$id}\"> :hasFather <\"&:;Person-{$id}\"> .");
		assertTrue(result);
	}
	
	public void test_7_3() {
		final boolean result = parse("<\"&ex;Person-{$id}\"> :hasFather <\"&ex;Person-{$id}\"> .");
		assertFalse(result);
	}
	
	private boolean parse(String input) {
		TurtleSyntaxParser parser = new TurtleSyntaxParser();
		parser.setPrefixManager(getPrefixManager());
		
		try {
	    	parser.parse(input);
	    }
	    catch (RecognitionException e) {
	      log.debug(e.getMessage());
	      return false;
	    }
	    return true;
	}
	
	private PrefixManager getPrefixManager() {
		PrefixManager pm = new SimplePrefixManager();
		pm.addUri("http://example.org/testcase#", ":");
		pm.addUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:");
		pm.addUri("http://www.w3.org/2000/01/rdf-schema#", "rdfs:");
		pm.addUri("http://www.w3.org/2001/XMLSchema#", "xsd:");
		return pm;
	}
}
