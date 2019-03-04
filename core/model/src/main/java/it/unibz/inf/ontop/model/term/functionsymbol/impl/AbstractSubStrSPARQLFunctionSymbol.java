package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public abstract class AbstractSubStrSPARQLFunctionSymbol extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdStringDatatype;

    protected AbstractSubStrSPARQLFunctionSymbol(String functionSymbolName, IRI functionIRI,
                                                 RDFDatatype xsdStringDatatype,
                                                 ImmutableList<TermType> expectedBaseTypes) {
        super(functionSymbolName, functionIRI, expectedBaseTypes);
        this.xsdStringDatatype = xsdStringDatatype;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        ImmutableTerm subTerm = terms.get(0);

        return subTerm.inferType()
                .filter(i -> i.getTermType()
                        .filter(t -> t.isA(xsdStringDatatype))
                        .isPresent());
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, VariableNullability variableNullability) {
        return typeTerms.get(0);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * Could be supported in the future
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
