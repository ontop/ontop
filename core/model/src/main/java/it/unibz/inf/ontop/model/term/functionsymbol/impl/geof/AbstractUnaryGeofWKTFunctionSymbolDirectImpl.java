package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;

import java.util.function.Function;

public abstract class AbstractUnaryGeofWKTFunctionSymbolDirectImpl extends AbstractGeofWKTFunctionSymbolImpl {
    protected AbstractUnaryGeofWKTFunctionSymbolDirectImpl(String functionSymbolName, IRI functionIRI, ImmutableList<TermType> inputTypes, RDFDatatype wktLiteralType) {
        super(functionSymbolName, functionIRI, inputTypes, wktLiteralType);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        WKTLiteralValue v0 = GeoUtils.extractWKTLiteralValue(termFactory, subLexicalTerms.get(0));

        // TODO: do we need to put the SRID back ??
        // TODO: do we need to wrap into a ST_ASTEXT ??
        return termFactory.getDBAsText(getDBFunction(termFactory).apply(v0.getGeometry()).simplify());
    }

    abstract public Function<ImmutableTerm, ImmutableTerm> getDBFunction(TermFactory termFactory);
}
