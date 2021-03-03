package it.unibz.inf.ontop.model.template.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.template.TemplateFactory;
import it.unibz.inf.ontop.model.term.*;

import java.util.stream.Collectors;

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


    protected NonVariableTerm templateComponentToTerm(Template.Component c) {
        return c.isColumnNameReference()
                ? getVariable(c.getComponent())
                : termFactory.getDBStringConstant(c.getComponent());
    }

    @Override
    public ImmutableList<Template.Component> getComponents(String template) {
        return TemplateParser.getComponents(template, false);
    }
}
