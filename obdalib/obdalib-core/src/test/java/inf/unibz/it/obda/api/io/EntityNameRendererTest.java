package inf.unibz.it.obda.api.io;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.DatalogProgram;
import org.obda.query.domain.PredicateFactory;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.BasicPredicateFactoryImpl;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.DatalogProgramImpl;
import org.obda.query.domain.imp.FunctionalTermImpl;
import org.obda.query.domain.imp.TermFactoryImpl;

public class EntityNameRendererTest extends TestCase {

	PrefixManager		pm;
	DatalogProgram		query;
	CQIE				rule1;
//	EntityNameRenderer	rend;

	@Before
	public void setUp() throws Exception {
		pm = new SimplePrefixManager();
//		rend = new EntityNameRenderer(pm);
		query = new DatalogProgramImpl();
		PredicateFactory pfac = BasicPredicateFactoryImpl.getInstance();
		TermFactoryImpl tfac = TermFactoryImpl.getInstance();

		LinkedList<Term> innerterms = new LinkedList<Term>();
		innerterms.add(tfac.createVariable("id"));

		List<Term> terms = new LinkedList<Term>();
		terms.add(tfac.createFunctionalTerm(pfac.createPredicate(URI.create("http://obda.org/onto.owl#person-individual"), 1), innerterms));

		Atom body = new AtomImpl(pfac.createPredicate(URI.create("http://obda.org/onto.owl#Person"), 1), terms);

		terms = new LinkedList<Term>();
		terms.add(tfac.createVariable("id"));
		Atom head = new AtomImpl(pfac.createPredicate(URI.create("http://obda.org/predicates#q"), 1), terms);

		rule1 = new CQIEImpl(head, Collections.singletonList(body), false);
		query.appendRule(rule1);
	}

	/***
	 * Checking that the atoms that use the default namespace are renderered in
	 * short form and those who don't have it are renderered with the full uri
	 */
	public void testNamespace1() {
		pm.setDefaultNamespace("http://obda.org/onto.owl#");
		String name = pm.getShortForm(query.getRules().get(0).getHead().getPredicate().toString(),true);
		assertTrue(name, name.equals("http://obda.org/predicates#q"));
		
		name = pm.getShortForm(query.getRules().get(0).getBody().get(0).getPredicate().toString(),true);
		assertTrue(name,name.equals("Person"));
		
		name =  pm.getShortForm(((FunctionalTermImpl)query.getRules().get(0).getBody().get(0).getTerms().get(0)).getName().toString(),true);
		assertTrue(name,name.equals("person-individual"));
		
		pm.setDefaultNamespace("http://obda.org/predicates#");
		name = pm.getShortForm(query.getRules().get(0).getHead().getPredicate().toString(),true);
		assertTrue(name,name.equals("q"));
		
		name = pm.getShortForm(query.getRules().get(0).getBody().get(0).getPredicate().toString(),true);
		assertTrue(name,name.equals("http://obda.org/onto.owl#Person"));
		
		name =  pm.getShortForm(((FunctionalTermImpl)query.getRules().get(0).getBody().get(0).getTerms().get(0)).getName().toString(),true);
		assertTrue(name,name.equals("http://obda.org/onto.owl#person-individual"));
	}
	
	/***
	 * This test checks if the prefix are properly handled
	 */
	public void testPrefix1() {
		pm.setDefaultNamespace("http://obda.org/onto.owl#");
		pm.addUri("http://obda.org/predicates#", "obdap");
		
		String name = pm.getShortForm(query.getRules().get(0).getHead().getPredicate().toString(),true);
		assertTrue(name, name.equals("obdap:q"));
		
		name = pm.getShortForm(query.getRules().get(0).getBody().get(0).getPredicate().toString(),true);
		assertTrue(name,name.equals("Person"));
		
		name =  pm.getShortForm(((FunctionalTermImpl)query.getRules().get(0).getBody().get(0).getTerms().get(0)).getName().toString(),true);
		assertTrue(name,name.equals("person-individual"));
		
		pm.setDefaultNamespace("http://obda.org/predicates#");
		pm.addUri("http://obda.org/onto.owl#", "onto");
		name = pm.getShortForm(query.getRules().get(0).getHead().getPredicate().toString(),true);
		assertTrue(name,name.equals("q"));
		
		name = pm.getShortForm(query.getRules().get(0).getBody().get(0).getPredicate().toString(),true);
		assertTrue(name,name.equals("onto:Person"));
		
		name =  pm.getShortForm(((FunctionalTermImpl)query.getRules().get(0).getBody().get(0).getTerms().get(0)).getName().toString(),true);
		assertTrue(name,name.equals("onto:person-individual"));
		
	}

}
