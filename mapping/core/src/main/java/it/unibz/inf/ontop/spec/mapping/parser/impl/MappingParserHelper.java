package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
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
        return components.stream()
                .map(c -> c.isColumnNameReference()
                        ? "{}"
                        : c.getUnescapedComponent())
                .collect(Collectors.joining());
    }

    public ImmutableList<ImmutableFunctionalTerm> getTemplateTerms(ImmutableList<TemplateComponent> components) {
        return components.stream()
                .filter(TemplateComponent::isColumnNameReference)
                .map(c -> getVariable(c.getComponent()))
                .collect(ImmutableCollectors.toList());
    }

    public ImmutableList<NonVariableTerm> getLiteralTemplateTerms(ImmutableList<TemplateComponent> components) {
        return components.stream()
                .map(c -> c.isColumnNameReference()
                        ? getVariable(c.getComponent())
                        : termFactory.getDBStringConstant(c.getUnescapedComponent()))
                .collect(ImmutableCollectors.toList());
    }

    public ImmutableTerm getLiteralTemplateTerm(String template) {
        ImmutableList<TemplateComponent> components = TemplateComponent.getComponents(template);
        ImmutableList<NonVariableTerm> terms = getLiteralTemplateTerms(components);
        switch (terms.size()) {
            case 0:
                return termFactory.getDBStringConstant("");
            case 1:
                return terms.get(0);
            default:
                return termFactory.getNullRejectingDBConcatFunctionalTerm(terms);
        }
    }

}
