package it.unibz.inf.ontop.model.template.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.template.TemplateComponent;
import it.unibz.inf.ontop.model.template.TemplateFactory;
import it.unibz.inf.ontop.model.term.*;

import java.util.regex.Pattern;

public abstract class AbstractTemplateFactory implements TemplateFactory {
    protected final TermFactory termFactory;

    protected AbstractTemplateFactory(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    protected ImmutableFunctionalTerm getVariable(String id) {
        if (id.contains("."))
            throw new IllegalArgumentException("Fully qualified columns as " + id + " are not accepted.\nPlease, use an alias instead.");

        return termFactory.getPartiallyDefinedToStringCast(termFactory.getVariable(id));
    }


    protected NonVariableTerm templateComponentToTerm(TemplateComponent c) {
        return c.isColumnNameReference()
                ? getVariable(c.getComponent())
                : termFactory.getDBStringConstant(c.getComponent());
    }

    /*
       https://www.w3.org/TR/r2rml/#from-template

       Curly braces that do not enclose column names must be escaped by a backslash character (“\”).
       This also applies to curly braces within column names.

       Backslash characters (“\”) must be escaped by preceding them with another backslash character,
       yielding “\\”. This also applies to backslashes within column names.
    */

    private static String decode(String s) {
        if (s.indexOf('\\') == -1)
            return s;
        int length = s.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                char n = s.charAt(++i);
                if (n != '\\' && n != '}' && n != '{')
                    sb.append(c);
                sb.append(n);
            }
            else
                sb.append(c);
        }
        return sb.toString();
    }

    private static String encode(String s) {
        if (s.indexOf('\\') == -1 && s.indexOf('}') == -1 && s.indexOf('{') == -1)
            return s;
        int length = s.length();
        StringBuilder sb = new StringBuilder(length * 5 / 4);
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '}' || c == '{') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    protected String termToTemplateComponentString(ImmutableTerm term) {
        if (term instanceof Variable)
            return "{" + encode(((Variable)term).getName()) + "}";

        if (term instanceof Constant)
            return encode(((Constant) term).getValue());

        throw new IllegalArgumentException("Unexpected term type (only Constant and Variable are allowed):" + term);
    }


    @Override
    public ImmutableList<TemplateComponent> getComponents(String template) {
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
                        if (i == currentStart)
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
