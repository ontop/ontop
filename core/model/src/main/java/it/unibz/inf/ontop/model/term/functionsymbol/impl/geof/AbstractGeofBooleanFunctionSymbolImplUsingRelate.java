package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;


public abstract class AbstractGeofBooleanFunctionSymbolImplUsingRelate extends AbstractGeofBooleanFunctionSymbolImpl {

    protected AbstractGeofBooleanFunctionSymbolImplUsingRelate(String functionSymbolName, IRI functionIRI, ImmutableList<TermType> inputTypes, RDFDatatype xsdBooleanType) {
        super(functionSymbolName, functionIRI, inputTypes, xsdBooleanType);
    }

    protected abstract String getMatrixPatternString();

    @Override
    protected ImmutableTerm computeDBBooleanTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        WKTLiteralValue v0 = GeoUtils.extractWKTLiteralValue(termFactory, subLexicalTerms.get(0));
        WKTLiteralValue v1 = GeoUtils.extractWKTLiteralValue(termFactory, subLexicalTerms.get(1));
        ImmutableTerm v2 = termFactory.getDBStringConstant(getMatrixPatternString());

        DBTermType geometryType = termFactory.getTypeFactory().getDBTypeFactory().getDBGeometryType();

        ImmutableTerm input0 = removeTextCast(v0.getGeometry(), geometryType, termFactory);
        ImmutableTerm input1 = removeTextCast(v1.getGeometry(), geometryType, termFactory);

        return termFactory.getDBRelate(input0, input1, v2).simplify();

    }

}
