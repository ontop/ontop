package it.unibz.inf.ontop.model.template.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Collectors;

public abstract class ObjectTemplateFactory extends AbstractTemplateFactory {
    protected ObjectTemplateFactory(TermFactory termFactory) {
        super(termFactory);
    }


    protected ImmutableList<ImmutableTerm> getTemplateTerms(ImmutableList<Template.Component> components) {
        return components.stream()
                .filter(Template.Component::isColumnNameReference)
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
                .map(TemplateParser::termToTemplateComponentString)
                .collect(ImmutableCollectors.toList());

        ObjectStringTemplateFunctionSymbol fs = (ObjectStringTemplateFunctionSymbol) functionalTerm.getFunctionSymbol();
        return  fs.getTemplateComponents().stream()
                .map(c -> c.isColumnNameReference()
                        ? varNames.get(c.getIndex())
                        : c.getComponent())
                .collect(Collectors.joining());
    }


    /**
     * This method instantiates the input uri template by arguments
     * <p>
     * Example:
     * <p>
     * If {@code args = ["A", 1]}, then
     * <p>
     * {@code  URITemplates.format("http://example.org/{}/{}", args)}
     * results {@code "http://example.org/A/1" }
     */
    public static String format(String iriOrBnodeTemplate, ImmutableList<?> args) {

        ImmutableList<Template.Component> components = TemplateParser.getComponents(iriOrBnodeTemplate, true);
        return components.stream()
                .map(c -> c.isColumnNameReference()
                ? args.get(c.getIndex()).toString()
                        : c.getComponent()).collect(Collectors.joining());
    }

}
