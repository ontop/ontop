package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.ObjectTemplates;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.stream.Collectors;

public class Templates {
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;

    public Templates(TermFactory termFactory, TypeFactory typeFactory) {
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
    }

    public ImmutableFunctionalTerm getVariable(String id) {
        if (id.contains("."))
            throw new IllegalArgumentException("Fully qualified columns as " + id + " are not accepted.\nPlease, use an alias instead.");

        return termFactory.getPartiallyDefinedToStringCast(termFactory.getVariable(id));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Optional<RDFDatatype> extractDatatype(Optional<String> lang, Optional<IRI> iri) {
        Optional<RDFDatatype> datatype = lang       // First try: language tag
                .filter(tag -> !tag.isEmpty())
                .map(typeFactory::getLangTermType);

        return datatype.isPresent()
                ? datatype
                : iri.map(typeFactory::getDatatype); // Second try: explicit datatype
    }

    /*
          IRI and Bnode templates are modelled by ImmutableFunctionalTerm
          with ObjectStringTemplateFunctionSymbol
     */

    public static String getTemplateString(ImmutableList<TemplateComponent> components) {
        if (components.stream()
                .filter(c -> !c.isColumnNameReference())
                .anyMatch(TemplateComponent::containsEscapeSequence))
            throw new IllegalArgumentException("Illegal escape sequence in template " + components);

        return components.stream()
                .map(c -> c.isColumnNameReference() ? "{}" : c.getComponent())
                .collect(Collectors.joining());
    }

    public ImmutableList<ImmutableTerm> getTemplateTerms(ImmutableList<TemplateComponent> components) {
        return components.stream()
                .filter(TemplateComponent::isColumnNameReference)
                .map(c -> getVariable(c.getComponent()))
                .collect(ImmutableCollectors.toList());
    }

    public ImmutableFunctionalTerm getIRIColumn(String column) {
        return termFactory.getIRIFunctionalTerm(getVariable(column));
    }

    public ImmutableFunctionalTerm getBnodeColumn(String column) {
        return termFactory.getBnodeFunctionalTerm(getVariable(column));
    }

    public NonVariableTerm getIRITemplate(ImmutableList<TemplateComponent> components) {
        int size = components.size();
        if (size == 0)
            return termFactory.getConstantIRI("");

        if (size == 1 && !components.get(0).isColumnNameReference())
            return termFactory.getConstantIRI(components.get(0).getComponent());

        return termFactory.getIRIFunctionalTerm(getTemplateString(components), getTemplateTerms(components));
    }

    public NonVariableTerm getBnodeTemplate(ImmutableList<TemplateComponent> components) {
        int size = components.size();
        if (size == 0)
            return termFactory.getConstantBNode("");

        if (size == 1 && !components.get(0).isColumnNameReference())
            return termFactory.getConstantBNode(components.get(0).getComponent());

        return termFactory.getBnodeFunctionalTerm(getTemplateString(components), getTemplateTerms(components));
    }

    /**
     * Converts a IRI or BNode template function into a template
     * <p>
     * For instance:
     * <pre>
     * {@code http://example.org/{}/{}/{}(X, Y, X) -> "http://example.org/{X}/{Y}/{X}"}
     * </pre>
     *
     * @param ift URI or BNode Function
     * @return a template with variable names inside the placeholders
     */

    public static String serializeObjectTemplate(ImmutableFunctionalTerm ift) {

        ImmutableList<String> varNames = ift.getTerms().stream()
                .map(DBTypeConversionFunctionSymbol::uncast)
                .filter(t -> t instanceof Variable)
                .map(t -> (Variable)t)
                .map(v -> "{" + v.getName() + "}")
                .collect(ImmutableCollectors.toList());

        if (!(ift.getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol))
            throw new IllegalArgumentException(
                    "The lexical term was expected to have a ObjectStringTemplateFunctionSymbol: "
                            + ift);

        ObjectStringTemplateFunctionSymbol fs = (ObjectStringTemplateFunctionSymbol) ift.getFunctionSymbol();
        return ObjectTemplates.format(fs.getTemplate(), varNames);
    }


    /*
        Literal templates are modelled by a database concatenation function
     */

    private NonVariableTerm templateComponentToTerm(TemplateComponent c) {
        return c.isColumnNameReference()
                ? getVariable(TemplateComponent.decode(c.getComponent()))
                : termFactory.getDBStringConstant(TemplateComponent.decode(c.getComponent()));
    }

    private static String termToTemplateComponentString(ImmutableTerm term) {
        if (term instanceof Constant)
            return TemplateComponent.encode(((Constant) term).getValue());

        if (term instanceof Variable)
            return "{" + TemplateComponent.encode(((Variable)term).getName()) + "}";

        throw new IllegalArgumentException("Unexpected term type (only Constant and Variable are allowed):" + term);
    }

    public ImmutableTerm getLiteralTemplateTerm(String template) {
        ImmutableList<TemplateComponent> components = TemplateComponent.getComponents(template);
        switch (components.size()) {
            case 0:
                return termFactory.getDBStringConstant("");
            case 1:
                return templateComponentToTerm(components.get(0));
            default:
                return termFactory.getNullRejectingDBConcatFunctionalTerm(components.stream()
                        .map(this::templateComponentToTerm)
                        .collect(ImmutableCollectors.toList()));
        }
    }

    public static String serializeLiteralTemplate(ImmutableFunctionalTerm dbConcatFunctionalTerm) {
        if (dbConcatFunctionalTerm.getFunctionSymbol() instanceof DBConcatFunctionSymbol)
            return dbConcatFunctionalTerm.getTerms().stream()
                    .map(DBTypeConversionFunctionSymbol::uncast)
                    .map(Templates::termToTemplateComponentString)
                    .collect(Collectors.joining());

        throw new IllegalArgumentException("Invalid term type (DBConcat is expected): " + dbConcatFunctionalTerm);
    }
}
