package it.unibz.inf.ontop.model.template.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.stream.Collectors;

public class LiteralTemplateFactory extends AbstractTemplateFactory {
    private final TypeFactory typeFactory;

    public LiteralTemplateFactory(TermFactory termFactory, TypeFactory typeFactory) {
        super(termFactory);
        this.typeFactory = typeFactory;
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

    public RDFDatatype getDatatype(IRI datatype) {
        return typeFactory.getDatatype(datatype);
    }

    public RDFDatatype getAbstractRDFSLiteral() {
        return typeFactory.getAbstractRDFSLiteral();
    }

    @Override
    public NonVariableTerm getConstant(String constant) {
        return termFactory.getDBStringConstant(constant);
    }

    @Override
    public ImmutableFunctionalTerm getColumn(String column) {
        return getVariable(column);
    }

    @Override
    public NonVariableTerm getTemplateTerm(ImmutableList<Template.Component> components) {
        switch (components.size()) {
            case 0:
                return getConstant("");
            case 1:
                return templateComponentToTerm(components.get(0));
            default:
                return termFactory.getNullRejectingDBConcatFunctionalTerm(components.stream()
                        .map(this::templateComponentToTerm)
                        .collect(ImmutableCollectors.toList()));
        }
    }

    /*
        Literal templates are modelled by a database concatenation function
     */

    @Override
    public String serializeTemplateTerm(ImmutableFunctionalTerm functionalTerm) {
        if (functionalTerm.getFunctionSymbol() instanceof DBConcatFunctionSymbol)
            return functionalTerm.getTerms().stream()
                    .map(DBTypeConversionFunctionSymbol::uncast)
                    .map(TemplateParser::termToTemplateComponentString)
                    .collect(Collectors.joining());

        throw new IllegalArgumentException("Invalid term type (DBConcat is expected): " + functionalTerm);
    }

}
