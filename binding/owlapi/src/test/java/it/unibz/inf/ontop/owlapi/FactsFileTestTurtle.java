package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/***
 * A test querying triples provided by an external "facts" file.
 */
public class FactsFileTestTurtle extends FactsFileTest {

	@BeforeClass
	public static void setUp() throws Exception {
		init("/facts/facts.ttl", null);
	}
}
