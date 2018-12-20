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
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.rdf.api.IRI;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class UriTemplateMatcher {

    private final TermFactory termFactory;

    private UriTemplateMatcher(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    private final Map<Pattern, NonConstantTerm> uriTemplateMatcher = new HashMap<>();

    /**
     * TODO: refactor using streaming.
     */
    public static UriTemplateMatcher create(Stream<? extends ImmutableFunctionalTerm> targetTermStream,
                                            TermFactory termFactory, TypeFactory typeFactory) {
        Set<String> templateStrings = new HashSet<>();

        UriTemplateMatcher uriTemplateMatcher = new UriTemplateMatcher(termFactory);

        ImmutableList<ImmutableTerm> iriLexicalTerms = targetTermStream
                .filter(f -> f.getFunctionSymbol() instanceof RDFTermFunctionSymbol)
                .filter(f -> f.getTerm(1)
                        .equals(termFactory.getRDFTermTypeConstant(typeFactory.getIRITermType())))
                .map(f -> f.getTerm(0))
                .collect(ImmutableCollectors.toList());

        for (ImmutableTerm lexicalTerm : iriLexicalTerms) {

            if (lexicalTerm instanceof NonFunctionalTerm) {
				/*
				 * URI without template, we get it directly from the column
				 * of the table, and the function is only f(x)
				 */
                if (templateStrings.contains("(.+)")) {
                    continue;
                }
                Pattern matcher = Pattern.compile("(.+)");
                uriTemplateMatcher.uriTemplateMatcher.put(matcher, termFactory.getVariable("x"));
                templateStrings.add("(.+)");
            } else {
                ImmutableFunctionalTerm functionalLexicalTerm = ((ImmutableFunctionalTerm) lexicalTerm);
                FunctionSymbol functionSymbol = functionalLexicalTerm.getFunctionSymbol();
                if (!(functionSymbol instanceof IRIStringTemplateFunctionSymbol))
                    continue;
                IRIStringTemplateFunctionSymbol templateSymbol = (IRIStringTemplateFunctionSymbol) functionSymbol;
                String templateString = templateSymbol.getTemplate();
                templateString = templateString.replace("{}", "(.+)");

                if (templateStrings.contains(templateString)) {
                    continue;
                }

                Pattern matcher = Pattern.compile(templateString);
                uriTemplateMatcher.uriTemplateMatcher.put(matcher, functionalLexicalTerm);
                templateStrings.add(templateString);

            }
        }
        return uriTemplateMatcher;
    }

    public static UriTemplateMatcher merge(Stream<UriTemplateMatcher> uriTemplateMatchers, TermFactory termFactory) {

        ImmutableMap<Pattern, Collection<NonConstantTerm>> pattern2Terms = uriTemplateMatchers
                .flatMap(m -> m.uriTemplateMatcher.entrySet().stream())
                .collect(ImmutableCollectors.toMultimap())
                .asMap();

        ImmutableMap<Pattern, NonConstantTerm> pattern2Term = pattern2Terms.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> flatten(e.getKey(), e.getValue())
                ));
        UriTemplateMatcher uriTemplateMatcher = new UriTemplateMatcher(termFactory);
        uriTemplateMatcher.uriTemplateMatcher.putAll(pattern2Term);
        return uriTemplateMatcher;
    }

    private static NonConstantTerm flatten(Pattern pattern, Collection<NonConstantTerm> collection) {
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
    public ImmutableFunctionalTerm generateIRIFunctionalTerm(IRI iri) {
        Optional<Pattern> longestMatchingPattern = uriTemplateMatcher.keySet().stream()
                .filter(p -> p.matcher(iri.getIRIString()).matches())
                .max(Comparator.comparingInt(c -> c.pattern().length()));

        return longestMatchingPattern
                .map(p -> generateIRIFunctionalTerm(iri, p))
                .orElseGet(() -> termFactory.getIRIFunctionalTerm(iri));
    }

    private ImmutableFunctionalTerm generateIRIFunctionalTerm(IRI iri, Pattern pattern) {
        NonConstantTerm matchingTerm = uriTemplateMatcher.get(pattern);
        if (matchingTerm instanceof Variable)
            return termFactory.getIRIFunctionalTerm(iri);

        // TODO: refactor
        IRIStringTemplateFunctionSymbol matchingFunctionSymbol =
                (IRIStringTemplateFunctionSymbol)((ImmutableFunctionalTerm) matchingTerm).getFunctionSymbol();

        Matcher matcher = pattern.matcher(iri.getIRIString());
        matcher.matches();
        ImmutableList<DBConstant> arguments = IntStream.range(1, matcher.groupCount() + 1)
                .boxed()
                .map(matcher::group)
                .map(termFactory::getDBStringConstant)
                .collect(ImmutableCollectors.toList());

        return termFactory.getIRIFunctionalTerm(matchingFunctionSymbol, arguments);
    }
}
