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

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriTemplateMatcher {

	private final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	private final Map<Pattern, Function> uriTemplateMatcher = new HashMap<Pattern, Function>();
	
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

		List<Pattern> patternsMatched = new LinkedList<Pattern>();
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
				 * This is a general tempalte function of the form
				 * uri("http://....", var1, var2,...) <p> we need to match var1,
				 * var2, etc with substrings from the subjectURI
				 */
				Matcher matcher = pattern.matcher(uriString);
				if ( matcher.matches()) {
					List<Term> values = new LinkedList<Term>();
					values.add(baseParameter);
					for (int i = 0; i < matcher.groupCount(); i++) {
						String value = matcher.group(i + 1);
						values.add(ofac.getConstantLiteral(value));
					}
					functionURI = ofac.getUriTemplate(values);
				}
			} else if (baseParameter instanceof Variable) {
				/*
				 * This is a direct mapping to a column, uri(x)
				 * we need to match x with the subjectURI
				 */
				functionURI = ofac.getUriTemplate(ofac.getConstantLiteral(uriString));
			}
			break;
		}
		if (functionURI == null) {
			/* If we cannot match againts a tempalte, we try to match againts the most general tempalte (which will 
			 * generate empty queires later in the query answering process
			 */
			functionURI = ofac.getUriTemplate(ofac.getConstantLiteral(uriString));
		}
			
		return functionURI;
	}
}
