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

public abstract class SPARQLFunctionSymbolImpl extends SPARQLLikeFunctionSymbolImpl implements SPARQLFunctionSymbol {

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
}
