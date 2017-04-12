package it.unibz.inf.ontop.model;

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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.mapping.TargetAtom;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class UriTemplateMatcher {

	private UriTemplateMatcher() {
	}

	private final Map<Pattern, Function> uriTemplateMatcher = new HashMap<>();

	public static UriTemplateMatcher createFromTargetAtoms(Stream<TargetAtom> targetAtoms) {
		return create(targetAtoms
				.map(TargetAtom::getSubstitution)
				// URI constructors can be found in the co-domain of the substitution
				.flatMap(s -> s.getImmutableMap().values().stream())
				.filter(t -> t instanceof Function)
				.map(t -> (Function) t));
	}

	/**
	 * TODO: refactor using streaming.
	 */
	public static UriTemplateMatcher create(Stream<? extends Function> targetAtomStream) {
		Set<String> templateStrings = new HashSet<>();
		
		UriTemplateMatcher uriTemplateMatcher  = new UriTemplateMatcher();

		ImmutableList<? extends Function> targetAtoms = targetAtomStream.collect(ImmutableCollectors.toList());

		for (Function fun : targetAtoms) {

		 // Collecting URI templates and making pattern matchers for them.
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

				Function templateFunction = DATA_FACTORY.getUriTemplate(DATA_FACTORY.getVariable("x"));
				Pattern matcher = Pattern.compile("(.+)");
				uriTemplateMatcher.uriTemplateMatcher.put(matcher, templateFunction);
				templateStrings.add("(.+)");
			} else {
				ValueConstant template = (ValueConstant) fun.getTerms().get(0);
				String templateString = template.getValue();
				templateString = templateString.replace("{}", "(.+)");

				if (templateStrings.contains(templateString)) {
					continue;
				}

				Pattern matcher = Pattern.compile(templateString);
				uriTemplateMatcher.uriTemplateMatcher.put(matcher, fun);
				templateStrings.add(templateString);

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
						values.add(DATA_FACTORY.getConstantLiteral(value));
					}
					functionURI = DATA_FACTORY.getUriTemplate(values);
				}
			} 
			else if (baseParameter instanceof Variable) {
				/*
				 * This is a direct mapping to a column, uri(x)
				 * we need to match x with the subjectURI
				 */
				functionURI = DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral(uriString));
			}
			break;
		}
		if (functionURI == null) {
			/* If we cannot match against a template, we try to match against the most general template (which will
			 * generate empty queries later in the query answering process
			 */
			functionURI = DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral(uriString));
		}
			
		return functionURI;
	}
}
