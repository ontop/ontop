package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriTemplateMatcher {

	private static final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	private final Map<Pattern, Function> uriTemplateMatcher = new HashMap<>();
	
	/**
	 * creates a URI template matcher 
	 * 
	 * @param unfoldingProgram
	 * @return
	 */
	
	public static UriTemplateMatcher create(List<CQIE> unfoldingProgram) {

		Set<String> templateStrings = new HashSet<>();
		
		UriTemplateMatcher uriTemplateMatcher  = new UriTemplateMatcher();

		for (CQIE mapping : unfoldingProgram) { 
			
			Function head = mapping.getHead();

			 // Collecting URI templates and making pattern matchers for them.
			for (Term term : head.getTerms()) {
				if (!(term instanceof Function)) {
					continue;
				}
				Function fun = (Function) term;
				if (!(fun.getFunctionSymbol() instanceof URITemplatePredicate)) {
					continue;
				}
				/*
				 * This is a URI function, so it can generate pattern matchers
				 * for the URIs. We have two cases, one where the arity is 1,
				 * and there is a constant/variable. The second case is
				 * where the first element is a string template of the URI, and
				 * the rest of the terms are variables/constants
				 */
				if (fun.getTerms().size() == 1) {
					/*
					 * URI without template, we get it directly from the column
					 * of the table, and the function is only f(x)
					 */
					if (templateStrings.contains("(.+)")) {
						continue;
					}
					Function templateFunction = ofac.getUriTemplate(ofac.getVariable("x"));
					Pattern matcher = Pattern.compile("(.+)");
					uriTemplateMatcher.uriTemplateMatcher.put(matcher, templateFunction);
					templateStrings.add("(.+)");
				} 
				else {
					ValueConstant template = (ValueConstant) fun.getTerms().get(0);
					String templateString = template.getValue();
					templateString = templateString.replace("{}", "(.+)");

					if (templateStrings.contains(templateString)) {
						continue;
					}
					Pattern mattcher = Pattern.compile(templateString);
					uriTemplateMatcher.uriTemplateMatcher.put(mattcher, fun);
					templateStrings.add(templateString);
				}
			}
		}
		return uriTemplateMatcher;
	}
	
	
	/***
	 * We will try to match the URI to one of our patterns, if this happens, we
	 * have a corresponding function, and the parameters for this function. The
	 * parameters are the values for the groups of the pattern.
	 */
	public Function generateURIFunction(String uriString) {
		Function functionURI = null;

		List<Pattern> patternsMatched = new LinkedList<>();
		for (Pattern pattern : uriTemplateMatcher.keySet()) {

			Matcher matcher = pattern.matcher(uriString);
			boolean match = matcher.matches();
			if (!match) {
				continue;
			}
			patternsMatched.add(pattern);
		}
		Comparator<Pattern> comparator = new Comparator<Pattern>() {
		    public int compare(Pattern c1, Pattern c2) {
		        return c2.pattern().length() - c1.pattern().length() ; // use your logic
		    }
		};

		Collections.sort(patternsMatched, comparator); 
		for (Pattern pattern : patternsMatched) {
			Function matchingFunction = uriTemplateMatcher.get(pattern);
			Term baseParameter = matchingFunction.getTerms().get(0);
			if (baseParameter instanceof Constant) {
				/*
				 * This is a general template function of the form
				 * uri("http://....", var1, var2,...) <p> we need to match var1,
				 * var2, etc with substrings from the subjectURI
				 */
				Matcher matcher = pattern.matcher(uriString);
				if ( matcher.matches()) {
					List<Term> values = new ArrayList<>(matcher.groupCount());
					values.add(baseParameter);
					for (int i = 0; i < matcher.groupCount(); i++) {
						String value = matcher.group(i + 1);
						values.add(ofac.getConstantLiteral(value));
					}
					functionURI = ofac.getUriTemplate(values);
				}
			} 
			else if (baseParameter instanceof Variable) {
				/*
				 * This is a direct mapping to a column, uri(x)
				 * we need to match x with the subjectURI
				 */
				functionURI = ofac.getUriTemplate(ofac.getConstantLiteral(uriString));
			}
			break;
		}
		if (functionURI == null) {
			/* If we cannot match against a template, we try to match against the most general template (which will
			 * generate empty queries later in the query answering process
			 */
			functionURI = ofac.getUriTemplate(ofac.getConstantLiteral(uriString));
		}
			
		return functionURI;
	}
}
