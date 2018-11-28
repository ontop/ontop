package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class AbstractUnaryStringSPARQLFunctionSymbol extends SPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdStringDatatype;

    protected AbstractUnaryStringSPARQLFunctionSymbol(@Nonnull String name, IRI functionIRI, RDFDatatype xsdStringDatatype) {
        super(name, functionIRI, ImmutableList.of(xsdStringDatatype));
        this.xsdStringDatatype = xsdStringDatatype;
    }

    /**
     * If the child type is xsd:string or a language tag, then returns it.
     *
     * However, if the type is known but different, return a non-fatal error.
     *
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {

        // TODO: use a more generic method looking for base type violations
        ImmutableTerm subTerm = terms.get(0);
        Optional<TermTypeInference> childTypeInference = subTerm.inferType();

        if (childTypeInference
                .flatMap(TermTypeInference::getTermType)
                .filter(t -> !t.isA(xsdStringDatatype))
                .isPresent()) {
            return Optional.of(TermTypeInference.declareNonFatalError());
        }
        return childTypeInference;
    }
}
