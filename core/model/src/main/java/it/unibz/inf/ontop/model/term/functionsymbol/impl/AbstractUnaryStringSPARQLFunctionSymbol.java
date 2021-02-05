package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class AbstractUnaryStringSPARQLFunctionSymbol extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdStringDatatype;

    protected AbstractUnaryStringSPARQLFunctionSymbol(@Nonnull String name, IRI functionIRI,
                                                      RDFDatatype xsdStringDatatype) {
        super(name, functionIRI, ImmutableList.of(xsdStringDatatype));
        this.xsdStringDatatype = xsdStringDatatype;
    }

    /**
     * If the child type is xsd:string or a language tag, then returns it.
     *
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        ImmutableTerm subTerm = terms.get(0);

        return subTerm.inferType()
                .filter(i -> i.getTermType()
                        .filter(t -> t.isA(xsdStringDatatype))
                        .isPresent());
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return typeTerms.get(0);
    }
}
