package it.unibz.inf.ontop.model.term.functionsymbol.db;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;

import java.util.Optional;
import java.util.function.Function;

/**
 * Such a function symbol is specific to object identifier (IRI, bnode) template
 *
 * NB: a functional term using this symbol is producing a DB string or a NULL
 */
public interface ObjectStringTemplateFunctionSymbol extends DBFunctionSymbol {

    String getTemplate();

    ImmutableList<Template.Component> getTemplateComponents();

    /**
     * Returns no function if the functional term is not-injective
     * The returned decomposition function returns empty if the template does not the constant.
     */
    Optional<Function<DBConstant, Optional<ImmutableList<DBConstant>>>> getDecomposer(ImmutableList<? extends ImmutableTerm> terms,
                                                                                      TermFactory termFactory,
                                                                                      VariableNullability variableNullability);
}
