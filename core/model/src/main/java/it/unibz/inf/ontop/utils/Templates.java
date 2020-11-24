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
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class for URI and BNode templates
 *
 * @author xiao
 */
public class Templates {

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

    public static int getArity(String template) {
        int count = 0;
        int currentIndex = template.indexOf(PLACE_HOLDER);
        while (currentIndex >= 0) {
            currentIndex = template.indexOf(PLACE_HOLDER, currentIndex + 1);
            count++;
        }
        return count;
    }

    /**
     * This method instantiates the input uri template by arguments
     * <p>
     * Example:
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
     * Converts a IRI or BNode template function into a template
     * <p>
     * For instance:
     * <pre>
     * {@code http://example.org/{}/{}/{}(X, Y, X) -> "http://example.org/{X}/{Y}/{X}"}
     * </pre>
     *
     * @param lexicalFunctionalTerm URI or BNode Function
     * @return a template with variable names inside the placeholders
     */
    public static String getTemplateString(ImmutableFunctionalTerm lexicalFunctionalTerm) {

        if (!(lexicalFunctionalTerm.getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol))
            throw new IllegalArgumentException(
                    "The lexical term was expected to have a ObjectStringTemplateFunctionSymbol: "
                            + lexicalFunctionalTerm);

        final String template = ((ObjectStringTemplateFunctionSymbol) lexicalFunctionalTerm.getFunctionSymbol()).getTemplate();
        ImmutableList<? extends ImmutableTerm> subTerms = lexicalFunctionalTerm.getTerms();

        List<String> splitParts = Splitter.on(PLACE_HOLDER).splitToList(template);

        StringBuilder templateWithVars = new StringBuilder();

        int numVars = subTerms.size();
        int numParts = splitParts.size();

        if (numParts != numVars + 1 && numParts != numVars) {
            throw new IllegalArgumentException("the number of place holders should be equal to the number of other terms.");
        }

        for (int i = 0; i < numVars; i++) {
            templateWithVars.append(splitParts.get(i))
                    .append("{")
                    .append(subTerms.get(i))
                    .append("}");
        }

        if (numParts == numVars + 1) {
            templateWithVars.append(splitParts.get(numVars));
        }

        return templateWithVars.toString();
    }


    public static String getTemplateString2(ImmutableFunctionalTerm ift) {

        ImmutableList<Variable> vars = ift.getTerms().stream()
                .map(DBTypeConversionFunctionSymbol::uncast)
                .filter(t -> t instanceof Variable)
                .map(t -> (Variable)t)
                .collect(ImmutableCollectors.toList());

        ObjectStringTemplateFunctionSymbol fs = (ObjectStringTemplateFunctionSymbol) ift.getFunctionSymbol();
        if (vars.size() != fs.getArity())
            throw new IllegalArgumentException("The number of placeholders does not match the arity: " + ift);

        Object[] varNames = vars.stream().map(v -> "{" + v + "}").toArray();
        return Templates.format(fs.getTemplate(), varNames);
    }

    public static String getDBConcatTemplateString(ImmutableFunctionalTerm ift) {
        if (ift.getFunctionSymbol() instanceof DBConcatFunctionSymbol)
            return ift.getTerms().stream()
                    .map(DBTypeConversionFunctionSymbol::uncast)
                    .map(Templates::concatArg2String)
                    .collect(Collectors.joining());

        throw new IllegalArgumentException("Invalid term type (DBConcat is expected): " + ift);
    }

    private static String concatArg2String(ImmutableTerm term) {
        if (term instanceof Constant) {
            return ((Constant) term).getValue()
                .replace("\\", "\\\\")
                .replace("{", "\\{")
                .replace("}", "\\}");
        }

        if (term instanceof Variable)
            return "{" + ((Variable)term).getName() + "}";

        throw new IllegalArgumentException("Unexpected term type (only Constant and Variable are allowed):" + term);
    }

}
