package org.obda.owlrefplatform.core.unfolding;

import inf.unibz.it.obda.model.impl.TermFactoryImpl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.obda.query.domain.Function;
import org.obda.query.domain.Term;
import org.obda.query.domain.URIConstant;

public class URIToFunctionMatcher {

	private Map<String, Function>	functTermMap	= null;

	
	public URIToFunctionMatcher(Map<String, Function> functionTermMap) {
		this.functTermMap = functionTermMap;
	}

	/***
	 * Looks in the list of functional terms registred during the creation of
	 * the compilatino of the mappings and tries to see if there is afuncitonal
	 * term that could be a match for the given URI.
	 * 
	 * Given our current way to interpret functional terms. A possible match is
	 * a functional term that has a predicate that matches the content of the
	 * URI to the left of the first "-" symbols. For example:
	 * 
	 * http://www.obda.com/onto#individual-mariano-rodriguez
	 * 
	 * is a possible match for the functinonal term
	 * 
	 * http://www.obda.com/onto#individual('mariano','rodriguez')
	 * 
	 * Note that this will change in the future.
	 * 
	 * @param uri
	 * @return
	 */
	public Function getPossibleFunctionalTermMatch(URIConstant uri) {
		String uristr = uri.getURI().toString();
		int pos = uristr.lastIndexOf('#');
		int pos2 = uristr.substring(pos).indexOf('-');
		pos = pos + pos2;
		String base = uristr.substring(0, pos);
		TermFactoryImpl tFact = TermFactoryImpl.getInstance();

		String[] constanturis = uristr.substring(pos + 1, uristr.length()).split("-");
		
		Function existing = functTermMap.get(base);
		if (existing == null)
			return null;
		if (existing.getFunctionSymbol().getArity() != constanturis.length)
			return null;

		List<Term> constantTerms = new LinkedList<Term>();
		for (String constantstr: constanturis) {
			constantTerms.add(tFact.createValueConstant(constantstr));
		}
		return tFact.createFunctionalTerm(existing.getFunctionSymbol(), constantTerms);
		
	}
}
