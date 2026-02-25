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
        ImmutableTerm matrixPattern = termFactory.getDBStringConstant(getMatrixPatternString());

        DBTermType geometryType = termFactory.getTypeFactory().getDBTypeFactory().getDBGeometryType();

        ImmutableTerm input0 = termFactory.getConversionFromRDFLexical2DB(geometryType, v0.getGeometry());
        ImmutableTerm input1 = termFactory.getConversionFromRDFLexical2DB(geometryType, v1.getGeometry());

        return termFactory.getDBRelate(input0, input1, matrixPattern).simplify();

    }

}
