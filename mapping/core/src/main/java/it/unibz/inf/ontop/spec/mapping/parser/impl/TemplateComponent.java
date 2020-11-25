package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;

/*
    https://www.w3.org/TR/r2rml/#from-template

    Curly braces that do not enclose column names must be escaped by a backslash character (“\”).
    This also applies to curly braces within column names.

    Backslash characters (“\”) must be escaped by preceding them with another backslash character,
    yielding “\\”. This also applies to backslashes within column names.
 */

public class TemplateComponent {
    private final boolean isColumnNameReference;
    private final String component;

    public TemplateComponent(boolean isColumnNameReference, String component) {
        this.isColumnNameReference = isColumnNameReference;
        this.component = component;
    }

    public boolean isColumnNameReference() {
        return isColumnNameReference;
    }

    public String getComponent() {
        return component;
    }

    public String getUnescapedComponent() {
        return component
                .replace("\\{", "{")
                .replace("\\}", "}")
                .replace("\\\\", "\\");
    }

    @Override
    public String toString() { return isColumnNameReference ? "_" + component + "_" : component; }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TemplateComponent) {
            TemplateComponent other = (TemplateComponent)o;
            return this.component.equals(other.component)
                    && this.isColumnNameReference == other.isColumnNameReference;
        }
        return false;
    }

    public static ImmutableList<TemplateComponent> getComponents(String template) {
        ImmutableList.Builder<TemplateComponent> builder = ImmutableList.builder();
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
                            builder.add(new TemplateComponent(false, template.substring(currentStart, i)));
                        currentStart = i + 1;
                        insideCurlyBracket = true;
                        break;
                    case '}':
                        if (!insideCurlyBracket)
                            throw new IllegalArgumentException("No matching opening curly bracket");
                        if (i == currentStart)
                            throw new IllegalArgumentException("Empty column reference");
                        builder.add(new TemplateComponent(true, template.substring(currentStart, i)));
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
            builder.add(new TemplateComponent(false, template.substring(currentStart)));
        }
        return builder.build();
    }
}
