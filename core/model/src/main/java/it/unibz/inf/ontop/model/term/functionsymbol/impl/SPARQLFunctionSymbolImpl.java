package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLFunctionSymbol;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public abstract class SPARQLFunctionSymbolImpl extends FunctionSymbolImpl implements SPARQLFunctionSymbol {

    @Nullable
    private final IRI functionIRI;
    private final String officialName;

    protected SPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull IRI functionIRI,
                                       @Nonnull ImmutableList<TermType> expectedBaseTypes) {
        super(functionSymbolName, expectedBaseTypes);
        this.functionIRI = functionIRI;
        this.officialName = functionIRI.getIRIString();
    }

    protected SPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull String officialName,
                                       @Nonnull ImmutableList<TermType> expectedBaseTypes) {
        super(functionSymbolName, expectedBaseTypes);
        this.functionIRI = null;
        this.officialName = officialName;
    }

    @Override
    public Optional<IRI> getIRI() {
        return Optional.ofNullable(functionIRI);
    }

    @Override
    public String getOfficialName() {
        return officialName;
    }

    /**
     * Default value for SPARQL functions as they may produce NULL due
     * to SPARQL errors
     */
    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }

    protected boolean isRDFFunctionalTerm(ImmutableTerm term) {
        return (term instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol);
    }

    protected ImmutableTerm extractRDFTermTypeTerm(ImmutableTerm rdfTerm, TermFactory termFactory) {
        if (isRDFFunctionalTerm(rdfTerm))
            return ((ImmutableFunctionalTerm)rdfTerm).getTerm(1);
        else if (rdfTerm instanceof RDFConstant)
            return termFactory.getRDFTermTypeConstant(((RDFConstant) rdfTerm).getType());
        else if ((rdfTerm instanceof Constant) && rdfTerm.isNull())
            return termFactory.getNullConstant();
        throw new IllegalArgumentException("Was expecting a isRDFFunctionalTerm or an RDFConstant or NULL");
    }

    protected ImmutableTerm extractLexicalTerm(ImmutableTerm rdfTerm, TermFactory termFactory) {
        if (isRDFFunctionalTerm(rdfTerm))
            return ((ImmutableFunctionalTerm)rdfTerm).getTerm(0);
        else if (rdfTerm instanceof RDFConstant)
            return termFactory.getDBStringConstant(((RDFConstant) rdfTerm).getValue());
        else if ((rdfTerm instanceof Constant) && rdfTerm.isNull())
            return termFactory.getNullConstant();
        throw new IllegalArgumentException("Was expecting a isRDFFunctionalTerm or an RDFConstant or NULL");
    }
}
