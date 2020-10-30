package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;


public abstract class AbstractGeofBooleanFunctionSymbolImplUsingRelate extends AbstractGeofBooleanFunctionSymbolImpl{

    protected AbstractGeofBooleanFunctionSymbolImplUsingRelate(String functionSymbolName, IRI functionIRI, ImmutableList<TermType> inputTypes, RDFDatatype xsdBooleanType) {
        super(functionSymbolName, functionIRI, inputTypes, xsdBooleanType);
    }

    protected abstract ImmutableTerm setMatrixPattern(TermFactory termFactory);

    @Override
    protected ImmutableTerm computeDBBooleanTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        WKTLiteralValue v0 = GeoUtils.extractWKTLiteralValue(termFactory, subLexicalTerms.get(0));
        WKTLiteralValue v1 = GeoUtils.extractWKTLiteralValue(termFactory, subLexicalTerms.get(1));
        //String matrix_pattern = "";
        //ImmutableTerm v2 = setMatrixPattern(termFactory, matrix_pattern);
        ImmutableTerm v2 = setMatrixPattern(termFactory);

        if (!v0.getSRID().equals(v1.getSRID())) {
            throw new IllegalArgumentException(String.format("SRIDs do not match: %s and %s", v0.getSRID(), v1.getSRID()));
        }
        return getDBFunction(termFactory).apply(v0.getGeometry(), v1.getGeometry(), v2).simplify();
    }

    abstract public TriFunction<ImmutableTerm, ImmutableTerm, ImmutableTerm, ImmutableTerm> getDBFunction(TermFactory termFactory);
}
