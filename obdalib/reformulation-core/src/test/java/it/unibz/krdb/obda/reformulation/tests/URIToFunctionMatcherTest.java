package it.unibz.krdb.obda.reformulation.tests;


import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.URIToFunctionMatcher;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class URIToFunctionMatcherTest extends TestCase {

	URIToFunctionMatcher matcher;
	
	
	public void setUp() throws Exception {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		List<NewLiteral> variables = new LinkedList<NewLiteral>();
		variables.add(fac.getVariable("x"));
		variables.add(fac.getVariable("y"));
		
		OBDADataFactory pfac = OBDADataFactoryImpl.getInstance();
		Predicate p = pfac.getPredicate(OBDADataFactoryImpl.getIRI("http://www.obda.com/onto#individual"), 2);
		
		NewLiteral fterm = fac.getFunctionalTerm(p, variables);
		
		Map<String,Function> termList = new HashMap<String, Function>();
		termList.put(p.getName().toString(), (Function)fterm);
		
		matcher = new URIToFunctionMatcher(termList);	
	}
	
	public void testMatchURI() {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		Function matchedTerm = matcher.getPossibleFunctionalTermMatch(fac.getURIConstant(OBDADataFactoryImpl.getIRI("http://www.obda.com/onto#individual-mariano-rodriguez")));
		assertTrue(matchedTerm != null);
		assertTrue(matchedTerm.toString(), matchedTerm.getFunctionSymbol().toString().equals("http://www.obda.com/onto#individual"));
		assertTrue(matchedTerm.toString(), matchedTerm.getTerms().get(0) instanceof ValueConstant);
		assertTrue(matchedTerm.toString(), matchedTerm.getTerms().get(1) instanceof ValueConstant);
		assertTrue(matchedTerm.getTerms().get(0).toString(), ((ValueConstant) matchedTerm.getTerms().get(0)).getValue().equals("mariano"));
		assertTrue(matchedTerm.getTerms().get(1).toString(), ((ValueConstant) matchedTerm.getTerms().get(1)).getValue().equals("rodriguez"));
	}

}
