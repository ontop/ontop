/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.iri.IRI;

public class UriTemplateMatcher {

	private OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	private Map<Pattern, Function> uriTemplateMatcher = new HashMap<Pattern, Function>();
	
	public UriTemplateMatcher() {
		// NO-OP
	}
	
	
	public void clear() {
		uriTemplateMatcher.clear();
	}
	
	public UriTemplateMatcher(Map<Pattern, Function> existing) {
		uriTemplateMatcher.putAll(existing);
	}
	
	public void put(Pattern uriTemplatePattern, Function uriFunction) {
		uriTemplateMatcher.put(uriTemplatePattern, uriFunction);
	}
	
	/***
	 * We will try to match the URI to one of our patterns, if this happens, we
	 * have a corresponding function, and the paramters for this function. The
	 * parameters are the values for the groups of the pattern.
	 */
	public Function generateURIFunction(String uriString) {
		Function functionURI = null;

		for (Pattern pattern : uriTemplateMatcher.keySet()) {

			Matcher matcher = pattern.matcher(uriString);
			boolean match = matcher.matches();
			if (!match) {
				continue;
			}
			Function matchingFunction = uriTemplateMatcher.get(pattern);
			Term baseParameter = matchingFunction.getTerms().get(0);
			if (baseParameter instanceof Constant) {
				/*
				 * This is a general tempalte function of the form
				 * uri("http://....", var1, var2,...) <p> we need to match var1,
				 * var2, etc with substrings from the subjectURI
				 */
				List<Term> values = new LinkedList<Term>();
				values.add(baseParameter);
				for (int i = 0; i < matcher.groupCount(); i++) {
					String value = matcher.group(i + 1);
					values.add(ofac.getConstantLiteral(value));
				}
				functionURI = ofac.getFunction(ofac.getUriTemplatePredicate(values.size()), values);
			} else if (baseParameter instanceof Variable) {
				/*
				 * This is a direct mapping to a column, uri(x)
				 * we need to match x with the subjectURI
				 */
				functionURI = ofac.getFunction(ofac.getUriTemplatePredicate(1), 
						ofac.getConstantLiteral(uriString));
			}
			break;
		}
		if (functionURI == null) {
			/* If we cannot match againts a tempalte, we try to match againts the most general tempalte (which will 
			 * generate empty queires later in the query answering process
			 */
			functionURI = ofac.getFunction(ofac.getUriTemplatePredicate(1), ofac.getConstantURI(uriString));
		}
			
		return functionURI;
	}
}
