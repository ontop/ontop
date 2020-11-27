package it.unibz.inf.ontop.model.template.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.TemplateComponent;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.ObjectTemplates;

import java.util.stream.Collectors;

public abstract class ObjectTemplateFactory extends AbstractTemplateFactory {
    protected ObjectTemplateFactory(TermFactory termFactory) {
        super(termFactory);
    }

    protected String getTemplateString(ImmutableList<TemplateComponent> components) {
        /*
        if (components.stream()
                .filter(c -> !c.isColumnNameReference())
                .anyMatch(TemplateComponent::containsEscapeSequence))
            throw new IllegalArgumentException("Illegal escape sequence in template " + components);
        */
        return components.stream()
                .map(c -> c.isColumnNameReference() ? "{}" : c.getComponent())
                .collect(Collectors.joining());
    }

    /*
    public boolean containsEscapeSequence() {
        return component.contains("\\\\") || component.contains("\\{") || component.contains("\\}");
    }
    */


    protected ImmutableList<ImmutableTerm> getTemplateTerms(ImmutableList<TemplateComponent> components) {
        return components.stream()
                .filter(TemplateComponent::isColumnNameReference)
                .map(c -> getVariable(c.getComponent()))
                .collect(ImmutableCollectors.toList());
    }

    /**
     * Converts a IRI or BNode template function into a template
     * <p>
     * For instance:
     * <pre>
     * {@code http://example.org/{}/{}/{}(X, Y, X) -> "http://example.org/{X}/{Y}/{X}"}
     * </pre>
     *
     * @param functionalTerm URI or BNode Function
     * @return a template with variable names inside the placeholders
     */

    @Override
    public String serializeTemplateTerm(ImmutableFunctionalTerm functionalTerm) {

        ImmutableList<String> varNames = functionalTerm.getTerms().stream()
                .map(DBTypeConversionFunctionSymbol::uncast)
                .filter(t -> t instanceof Variable)
                .map(this::termToTemplateComponentString)
                .collect(ImmutableCollectors.toList());

        ObjectStringTemplateFunctionSymbol fs = (ObjectStringTemplateFunctionSymbol) functionalTerm.getFunctionSymbol();
        return ObjectTemplates.format(fs.getTemplate(), varNames);
    }
}
