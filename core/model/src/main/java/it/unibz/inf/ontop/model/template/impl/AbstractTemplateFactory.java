package it.unibz.inf.ontop.model.template.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.template.TemplateComponent;
import it.unibz.inf.ontop.model.template.TemplateFactory;
import it.unibz.inf.ontop.model.term.*;

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
        return s.replace("\\{", "{")
                .replace("\\}", "}")
                .replace("\\\\", "\\");
    }

    private static String encode(String s) {
        return s.replace("\\", "\\\\")
                .replace("{", "\\{")
                .replace("}", "\\}");
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
