package it.unibz.inf.ontop.model.term.functionsymbol.db;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;

/**
 * Such a function symbol is specific to object identifier (IRI, bnode) template
 *
 * NB: a functional term using this symbol is producing a DB string or a NULL
 */
public interface ObjectStringTemplateFunctionSymbol extends DBFunctionSymbol {

    String getTemplate();

    ImmutableList<Template.Component> getTemplateComponents();
}
