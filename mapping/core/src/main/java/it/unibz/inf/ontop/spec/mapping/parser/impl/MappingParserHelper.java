package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.stream.Collectors;

public class MappingParserHelper {
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;

    public MappingParserHelper(TermFactory termFactory, TypeFactory typeFactory) {
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

    public String getTemplateString(ImmutableList<TemplateComponent> components) {
        if (components.stream()
                .filter(c -> !c.isColumnNameReference())
                .anyMatch(TemplateComponent::containsEscapeSequence))
            throw new IllegalArgumentException("Illegal escape sequence in template " + components);

        return components.stream()
                .map(c -> c.isColumnNameReference()
                        ? "{}"
                        : c.getComponent())
                .collect(Collectors.joining());
    }

    public ImmutableList<ImmutableTerm> getTemplateTerms(ImmutableList<TemplateComponent> components) {
        return components.stream()
                .filter(TemplateComponent::isColumnNameReference)
                .map(c -> getVariable(c.getComponent()))
                .collect(ImmutableCollectors.toList());
    }

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

    public static String getLiteralTemplateString(ImmutableFunctionalTerm dbConcatFunctionalTerm) {
        if (dbConcatFunctionalTerm.getFunctionSymbol() instanceof DBConcatFunctionSymbol)
            return dbConcatFunctionalTerm.getTerms().stream()
                    .map(DBTypeConversionFunctionSymbol::uncast)
                    .map(MappingParserHelper::termToTemplateComponentString)
                    .collect(Collectors.joining());

        throw new IllegalArgumentException("Invalid term type (DBConcat is expected): " + dbConcatFunctionalTerm);
    }
}
