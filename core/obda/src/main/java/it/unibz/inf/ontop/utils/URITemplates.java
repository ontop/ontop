package it.unibz.inf.ontop.utils;

/*
 * #%L
 * ontop-obdalib-core
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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.InvalidPrefixWritingException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.model.term.impl.TermUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A utility class for URI templates
 *
 * @author xiao
 */
public class URITemplates {

    private static final String PLACE_HOLDER = "{}";
    private static final int PLACE_HOLDER_LENGTH = PLACE_HOLDER.length();

    /**
     * This method instantiates the input uri template by arguments
     * <p>
     * Example:
     * <p>
     * If {@code args = ["A", 1]}, then
     * <p>
     * {@code  URITemplates.format("http://example.org/{}/{}", args)}
     * results {@code "http://example.org/A/1" }
     *
     * @see #format(String, Object...)
     */
    public static String format(String uriTemplate, Collection<?> args) {

        StringBuilder sb = new StringBuilder();

        int beginIndex = 0;

        for (Object arg : args) {

            int endIndex = uriTemplate.indexOf(PLACE_HOLDER, beginIndex);

            sb.append(uriTemplate.subSequence(beginIndex, endIndex)).append(arg);

            beginIndex = endIndex + PLACE_HOLDER_LENGTH;
        }

        sb.append(uriTemplate.substring(beginIndex));

        return sb.toString();

    }

    /**
     * This method instantiates the input uri template by arguments
     * <p>
     * <p>
     * <p>
     * Example:
     * <p>
     * <p>
     * {@code  URITemplates.format("http://example.org/{}/{}", "A", 1)} results {@code "http://example.org/A/1" }
     *
     * @param uriTemplate String with placeholder
     * @param args        args
     * @return a formatted string
     */
    public static String format(String uriTemplate, Object... args) {
        return format(uriTemplate, Arrays.asList(args));
    }


    /**
     * Converts a URI function to a URI template
     * <p>
     * For instance:
     * <pre>
     * URI("http://example.org/{}/{}/{}", X, Y, X) -> "http://example.org/{X}/{Y}/{X}"
     * </pre>
     *
     * @param uriFunction URI Function
     * @return a URI template with variable names inside the placeholders
     */
    public static String getUriTemplateString(ImmutableFunctionalTerm uriFunction) {
        ValueConstant term = (ValueConstant) uriFunction.getTerm(0);
        final String template = term.getValue();
        ImmutableList<? extends ImmutableTerm> otherSubTerms = uriFunction.getTerms()
                .subList(1, uriFunction.getTerms().size());

        List<String> splitParts = Splitter.on(PLACE_HOLDER).splitToList(template);

        StringBuilder templateWithVars = new StringBuilder();

        int numVars = otherSubTerms.size();
        int numParts = splitParts.size();

        if (numParts != numVars + 1 && numParts != numVars) {
            throw new IllegalArgumentException("the number of place holders should be equal to the number of other terms.");
        }

        for (int i = 0; i < numVars; i++) {
            templateWithVars.append(splitParts.get(i))
                    .append("{")
                    .append(otherSubTerms.get(i))
                    .append("}");
        }

        if (numParts == numVars + 1) {
            templateWithVars.append(splitParts.get(numVars));
        }

        return templateWithVars.toString();
    }

    public static String getUriTemplateString(ImmutableFunctionalTerm uriTemplate, PrefixManager prefixmng) {
        String template = getUriTemplateString(uriTemplate);
        try {
            template = prefixmng.getExpandForm(template);
        } catch (InvalidPrefixWritingException ex) {
            // in this case, the we do not need to expand
        }
        return template;
    }


}
