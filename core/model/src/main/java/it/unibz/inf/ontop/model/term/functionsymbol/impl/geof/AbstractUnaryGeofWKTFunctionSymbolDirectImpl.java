package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
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

        DBTermType geometryType = termFactory.getTypeFactory().getDBTypeFactory().getDBGeometryType();

        ImmutableTerm input0 = termFactory.getConversionFromRDFLexical2DB(geometryType, v0.getGeometry());
        return termFactory.getDBAsText(getDBFunction(termFactory).apply(input0).simplify());
    }

    abstract public Function<ImmutableTerm, ImmutableTerm> getDBFunction(TermFactory termFactory);
}
