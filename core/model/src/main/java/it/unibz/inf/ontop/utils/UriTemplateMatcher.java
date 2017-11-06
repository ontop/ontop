package it.unibz.inf.ontop.utils;

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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class UriTemplateMatcher {

    private final TermFactory termFactory;

    private UriTemplateMatcher(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    private final Map<Pattern, ImmutableFunctionalTerm> uriTemplateMatcher = new HashMap<>();

    /**
     * TODO: refactor using streaming.
     */
    public static UriTemplateMatcher create(Stream<? extends ImmutableFunctionalTerm> targetAtomStream,
                                            TermFactory termFactory) {
        Set<String> templateStrings = new HashSet<>();

        UriTemplateMatcher uriTemplateMatcher = new UriTemplateMatcher(termFactory);

        ImmutableList<? extends ImmutableFunctionalTerm> targetAtoms = targetAtomStream.collect(ImmutableCollectors.toList());

        for (ImmutableFunctionalTerm fun : targetAtoms) {

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

                ImmutableFunctionalTerm templateFunction = termFactory.getImmutableUriTemplate(termFactory.getVariable("x"));
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

    public static UriTemplateMatcher merge(Stream<UriTemplateMatcher> uriTemplateMatchers, TermFactory termFactory) {

        ImmutableMap<Pattern, Collection<ImmutableFunctionalTerm>> pattern2Terms = uriTemplateMatchers
                .flatMap(m -> m.getMap().entrySet().stream())
                .collect(ImmutableCollectors.toMultimap())
                .asMap();

        ImmutableMap<Pattern, ImmutableFunctionalTerm> pattern2Term = pattern2Terms.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> e.getKey(),
                        e -> flatten(e.getKey(), e.getValue())
                ));
        UriTemplateMatcher uriTemplateMatcher = new UriTemplateMatcher(termFactory);
        uriTemplateMatcher.uriTemplateMatcher.putAll(pattern2Term);
        return uriTemplateMatcher;
    }

    private static ImmutableFunctionalTerm flatten(Pattern pattern, Collection<ImmutableFunctionalTerm> collection) {
        if (ImmutableSet.copyOf(collection).size() == 1) {
            return collection.iterator().next();
        }
        throw new IllegalArgumentException("Conflicting term for pattern" + pattern + ": " + collection);
    }

    /***
     * We will try to match the URI to one of our patterns, if this happens, we
     * have a corresponding function, and the parameters for this function. The
     * parameters are the values for the groups of the pattern.
     */
    public ImmutableFunctionalTerm generateURIFunction(String uriString) {
        ImmutableFunctionalTerm functionURI = null;

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
                return c2.pattern().length() - c1.pattern().length(); // use your logic
            }
        };

        Collections.sort(patternsMatched, comparator);
        for (Pattern pattern : patternsMatched) {
            ImmutableFunctionalTerm matchingFunction = uriTemplateMatcher.get(pattern);
            ImmutableTerm baseParameter = matchingFunction.getTerm(0);
            if (baseParameter instanceof Constant) {
				/*
				 * This is a general template function of the form
				 * uri("http://....", var1, var2,...) <p> we need to match var1,
				 * var2, etc with substrings from the subjectURI
				 */
                Matcher matcher = pattern.matcher(uriString);
                if (matcher.matches()) {
                    ImmutableList.Builder<ImmutableTerm> values = ImmutableList.builder();
                    values.add(baseParameter);
                    for (int i = 0; i < matcher.groupCount(); i++) {
                        String value = matcher.group(i + 1);
                        values.add(termFactory.getConstantLiteral(value));
                    }
                    functionURI = termFactory.getImmutableUriTemplate(values.build());
                }
            } else if (baseParameter instanceof Variable) {
				/*
				 * This is a direct mapping to a column, uri(x)
				 * we need to match x with the subjectURI
				 */
                functionURI = termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(uriString));
            }
            break;
        }
        if (functionURI == null) {
			/* If we cannot match against a template, we try to match against the most general template (which will
			 * generate empty queries later in the query answering process
			 */
            functionURI = termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(uriString));
        }

        return functionURI;
    }

    private ImmutableMap<Pattern, ImmutableFunctionalTerm> getMap() {
        return ImmutableMap.copyOf(uriTemplateMatcher);
    }
}
