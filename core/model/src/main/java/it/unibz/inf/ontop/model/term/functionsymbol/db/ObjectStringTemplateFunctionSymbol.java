package it.unibz.inf.ontop.model.term.functionsymbol.db;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;

import java.util.Optional;

/**
 * Such a function symbol is specific to object identifier (IRI, bnode) template
 *
 * NB: a functional term using this symbol is producing a DB string or a NULL
 */
public interface ObjectStringTemplateFunctionSymbol extends DBFunctionSymbol {

    String getTemplate();

    ImmutableList<Template.Component> getTemplateComponents();

    /**
     * Returns empty if the decomposition cannot be done (non-injective functional term or not matching the template)
     */
    Optional<ImmutableList<DBConstant>> decompose(ImmutableList<? extends ImmutableTerm> terms, DBConstant constant,
                         TermFactory termFactory, VariableNullability variableNullability);
}
