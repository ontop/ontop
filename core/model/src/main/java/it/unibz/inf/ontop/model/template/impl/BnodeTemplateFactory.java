package it.unibz.inf.ontop.model.template.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;

public class BnodeTemplateFactory extends ObjectTemplateFactory {

    public BnodeTemplateFactory(TermFactory termFactory) {
        super(termFactory);
    }

    @Override
    public NonVariableTerm getConstant(String constant) {
        return termFactory.getConstantBNode(constant);
    }

    @Override
    public ImmutableFunctionalTerm getColumn(String column) {
        return termFactory.getBnodeFunctionalTerm(getVariable(column));
    }

    @Override
    public NonVariableTerm getTemplateTerm(ImmutableList<Template.Component> components) {
        int size = components.size();
        if (size == 0)
            return getConstant("");

        if (size == 1 && !components.get(0).isColumnNameReference())
            return getConstant(components.get(0).getComponent());

        return termFactory.getBnodeFunctionalTerm(components, getTemplateTerms(components));
    }

    @Override
    public String serializeTemplateTerm(ImmutableFunctionalTerm functionalTerm) {
        if (!(functionalTerm.getFunctionSymbol() instanceof BnodeStringTemplateFunctionSymbol))
            throw new IllegalArgumentException(
                    "The lexical term was expected to have a BnodeStringTemplateFunctionSymbol: "
                            + functionalTerm);

        return super.serializeTemplateTerm(functionalTerm);
    }

}
