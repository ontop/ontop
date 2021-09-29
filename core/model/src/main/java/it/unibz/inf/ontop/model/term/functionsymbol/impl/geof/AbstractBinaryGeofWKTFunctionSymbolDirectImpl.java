package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;

import java.util.function.BiFunction;

public abstract class AbstractBinaryGeofWKTFunctionSymbolDirectImpl extends AbstractGeofWKTFunctionSymbolImpl {
    protected AbstractBinaryGeofWKTFunctionSymbolDirectImpl(String functionSymbolName, IRI functionIRI, ImmutableList<TermType> inputTypes, RDFDatatype wktLiteralType) {
        super(functionSymbolName, functionIRI, inputTypes, wktLiteralType);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        WKTLiteralValue v0 = GeoUtils.extractWKTLiteralValue(termFactory, subLexicalTerms.get(0));
        WKTLiteralValue v1 = GeoUtils.extractWKTLiteralValue(termFactory, subLexicalTerms.get(1));

        if (!v0.getSRID().equals(v1.getSRID())) {
            throw new IllegalArgumentException(String.format("SRIDs do not match: %s and %s", v0.getSRID(), v1.getSRID()));
        }

        return termFactory.getDBAsText(getDBFunction(termFactory).apply(v0.getGeometry(), v1.getGeometry()).simplify());
    }

    abstract public BiFunction<ImmutableTerm, ImmutableTerm, ImmutableTerm> getDBFunction(TermFactory termFactory);
}
