package it.unibz.inf.ontop.model.template.impl;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.StringUtils;

import java.util.stream.Collectors;

public class TemplateParser {
        /*
       https://www.w3.org/TR/r2rml/#from-template

       Curly braces that do not enclose column names must be escaped by a backslash character (“\”).
       This also applies to curly braces within column names.

       Backslash characters (“\”) must be escaped by preceding them with another backslash character,
       yielding “\\”. This also applies to backslashes within column names.
    */

    private static final ImmutableBiMap<Character, String> ESCAPE = ImmutableBiMap.of(
            '\\', "\\\\",
            '}', "\\}",
            '{', "\\{");

    private static String decode(String s) {
        return StringUtils.decode(s, '\\', 2, ESCAPE.inverse(), (code) -> {});
    }

    private static String encode(String s) {
        return StringUtils.encode(s, ESCAPE);
    }

    public static String termToTemplateComponentString(ImmutableTerm term) {
        if (term instanceof Variable)
            return "{" + encode(((Variable)term).getName()) + "}";

        if (term instanceof Constant)
            return encode(((Constant) term).getValue());

        throw new IllegalArgumentException("Unexpected term type (only Constant and Variable are allowed):" + term);
    }

    public static String getEncodedTemplateString(ImmutableList<Template.Component> components) {
        return components.stream()
                .map(c -> c.isColumnNameReference() ? "{}" : encode(c.getComponent()))
                .collect(Collectors.joining());
    }

    public static ImmutableList<Template.Component> getComponents(String template, boolean emptyPlaceholders) {
        Template.Builder builder = Template.builder();
        boolean escape = false;
        boolean insideCurlyBracket = false;
        int currentStart = 0;
        for (int i = 0; i < template.length(); i++) {
            if (escape)
                escape = false; // single-character escapes
            else {
                switch (template.charAt(i)) {
                    case '{':
                        if (insideCurlyBracket)
                            throw new IllegalArgumentException("Nested curly brackets are not allowed");
                        if (i > currentStart)
                            builder.addSeparator(decode(template.substring(currentStart, i)));
                        currentStart = i + 1;
                        insideCurlyBracket = true;
                        break;
                    case '}':
                        if (!insideCurlyBracket)
                            throw new IllegalArgumentException("No matching opening curly bracket");
                        if (i == currentStart != emptyPlaceholders)
                            throw new IllegalArgumentException("Empty column reference");
                        builder.addColumn(decode(template.substring(currentStart, i)));
                        currentStart = i + 1;
                        insideCurlyBracket = false;
                        break;
                    case '\\':
                        escape = true;
                }
            }
        }
        if (escape)
            throw new IllegalArgumentException("Incomplete escape");

        if (currentStart != template.length()) {
            if (insideCurlyBracket)
                throw new IllegalArgumentException("No matching closing curly bracket");
            builder.addSeparator(decode(template.substring(currentStart)));
        }
        return builder.build();
    }

}
