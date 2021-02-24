package it.unibz.inf.ontop.model.template;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.NonVariableTerm;


public interface TemplateFactory {

    NonVariableTerm getConstant(String constant);

    ImmutableFunctionalTerm getColumn(String column);

    NonVariableTerm getTemplateTerm(ImmutableList<Template.Component> components);

    ImmutableList<Template.Component> getComponents(String template);

    String serializeTemplateTerm(ImmutableFunctionalTerm functionalTerm);
}
